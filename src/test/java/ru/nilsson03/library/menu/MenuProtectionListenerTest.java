package ru.nilsson03.library.menu;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import ru.nilsson03.library.BaseLibrary;
import ru.nilsson03.library.invui.window.Window;
import ru.nilsson03.library.invui.window.WindowManager;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuProtectionListenerTest {

    private MenuProtectionListener listener;

    @Mock
    private Player player;
    @Mock
    private InventoryView view;
    @Mock
    private Inventory topInventory;
    @Mock
    private PlayerInventory playerInventory;
    @Mock
    private InventoryClickEvent event;
    @Mock
    private WindowManager windowManager;
    @Mock
    private Window invUiWindow;

    @BeforeEach
    void setUp() {
        listener = new MenuProtectionListener();
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getView()).thenReturn(view);
        when(view.getTopInventory()).thenReturn(topInventory);
        when(player.getInventory()).thenReturn(playerInventory);
    }

    @Test
    @DisplayName("Сундук: shift-click не отменяется")
    void doesNotProtectChestOnShiftClick() {
        stubTopHolder(new ChestInventoryHolder());
        stubDangerousClick(InventoryAction.MOVE_TO_OTHER_INVENTORY, ClickType.SHIFT_LEFT);

        withWindowManager(null, null, () -> {
            listener.onInventoryClick(event);
            assertProtectionNotApplied();
        });
    }

    @Test
    @DisplayName("Сундук: COLLECT_TO_CURSOR не отменяется")
    void doesNotProtectChestOnCollectToCursor() {
        stubTopHolder(new ChestInventoryHolder());
        stubDangerousClick(InventoryAction.COLLECT_TO_CURSOR, ClickType.DOUBLE_CLICK);

        withWindowManager(null, null, () -> {
            listener.onInventoryClick(event);
            assertProtectionNotApplied();
        });
    }

    @Test
    @DisplayName("Наковальня: shift-click не отменяется")
    void doesNotProtectAnvilOnShiftClick() {
        stubTopHolder(new AnvilInventoryHolder());
        stubDangerousClick(InventoryAction.MOVE_TO_OTHER_INVENTORY, ClickType.SHIFT_LEFT);

        withWindowManager(null, null, () -> {
            listener.onInventoryClick(event);
            assertProtectionNotApplied();
        });
    }

    @Test
    @DisplayName("Наковальня: HOTBAR_SWAP не отменяется")
    void doesNotProtectAnvilOnHotbarSwap() {
        stubTopHolder(new AnvilInventoryHolder());
        stubDangerousClick(InventoryAction.HOTBAR_SWAP, ClickType.NUMBER_KEY);

        withWindowManager(null, null, () -> {
            listener.onInventoryClick(event);
            assertProtectionNotApplied();
        });
    }

    @Test
    @DisplayName("holder == null без InvUI Window: не отменяется (регресс бага)")
    void doesNotProtectNullHolderWithoutInvUiWindow() {
        stubTopHolder(null);
        stubDangerousClick(InventoryAction.MOVE_TO_OTHER_INVENTORY, ClickType.SHIFT_LEFT);

        withWindowManager(null, null, () -> {
            listener.onInventoryClick(event);
            assertProtectionNotApplied();
        });
    }

    @Test
    @DisplayName("InvUI Window по inventory: MOVE_TO_OTHER_INVENTORY отменяется")
    void protectsInvUiWindowByInventory() {
        stubTopHolder(null);
        stubDangerousClick(InventoryAction.MOVE_TO_OTHER_INVENTORY, ClickType.SHIFT_LEFT);

        withWindowManager(invUiWindow, null, () -> withSchedulerMocks(() -> {
            listener.onInventoryClick(event);
            assertProtectionApplied();
        }));
    }

    @Test
    @DisplayName("InvUI Window по игроку: NUMBER_KEY отменяется")
    void protectsInvUiWindowByPlayer() {
        stubTopHolder(new ChestInventoryHolder());
        stubDangerousClick(InventoryAction.HOTBAR_SWAP, ClickType.NUMBER_KEY);

        withWindowManager(null, invUiWindow, () -> withSchedulerMocks(() -> {
            listener.onInventoryClick(event);
            assertProtectionApplied();
        }));
    }

    @Test
    @DisplayName("InvUI: shift-click из инвентаря игрока отменяется")
    void protectsInvUiWhenShiftClickingFromPlayerInventory() {
        stubTopHolder(null);
        when(event.getAction()).thenReturn(InventoryAction.PICKUP_ALL);
        when(event.getClick()).thenReturn(ClickType.SHIFT_LEFT);
        when(event.getClickedInventory()).thenReturn(playerInventory);

        withWindowManager(invUiWindow, null, () -> withSchedulerMocks(() -> {
            listener.onInventoryClick(event);
            assertProtectionApplied();
        }));
    }

    @Test
    @DisplayName("Не-игрок: обработчик ничего не делает")
    void ignoresNonPlayerClicker() {
        when(event.getWhoClicked()).thenReturn(mock(org.bukkit.entity.HumanEntity.class));

        listener.onInventoryClick(event);

        verify(event, never()).getView();
        assertProtectionNotApplied();
    }

    private void stubTopHolder(InventoryHolder holder) {
        when(topInventory.getHolder()).thenReturn(holder);
    }

    private void stubDangerousClick(InventoryAction action, ClickType clickType) {
        when(event.getAction()).thenReturn(action);
        when(event.getClick()).thenReturn(clickType);
        when(event.getClickedInventory()).thenReturn(topInventory);
    }

    private void assertProtectionApplied() {
        verify(event).setCancelled(true);
        verify(event).setResult(Event.Result.DENY);
    }

    private void assertProtectionNotApplied() {
        verify(event, never()).setCancelled(true);
        verify(event, never()).setResult(any());
        verify(event, never()).setCursor(any());
    }

    private void withWindowManager(Window byInventory, Window byPlayer, Runnable action) {
        try (MockedStatic<WindowManager> windowManagerStatic = mockStatic(WindowManager.class)) {
            windowManagerStatic.when(WindowManager::getInstance).thenReturn(windowManager);
            when(windowManager.getWindow(topInventory)).thenReturn(byInventory);
            when(windowManager.getOpenWindow(player)).thenReturn(byPlayer);
            action.run();
        }
    }

    private void withSchedulerMocks(Runnable action) {
        try (MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
                MockedStatic<BaseLibrary> baseLibrary = mockStatic(BaseLibrary.class)) {
            BukkitScheduler scheduler = mock(BukkitScheduler.class);
            BaseLibrary plugin = mock(BaseLibrary.class);

            baseLibrary.when(BaseLibrary::getInstance).thenReturn(plugin);
            bukkit.when(Bukkit::getScheduler).thenReturn(scheduler);
            when(scheduler.runTask(any(), any(Runnable.class))).thenReturn(null);
            when(scheduler.runTaskLater(any(), any(Runnable.class), anyLong())).thenReturn(null);

            action.run();
        }
    }

    static final class ChestInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }

    static final class AnvilInventoryHolder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
