package ru.nilsson03.library.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import ru.nilsson03.library.BaseLibrary;
import ru.nilsson03.library.invui.gui.Gui;
import ru.nilsson03.library.invui.gui.SlotElement;
import ru.nilsson03.library.invui.window.Window;
import ru.nilsson03.library.invui.window.WindowManager;

/**
 * Blocks item duplication / insertion exploits in InvUI menus only.
 * Regular inventories (chests, anvils, hoppers, etc.) must not be affected.
 */
public class MenuProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();

        Window window = getInvUiWindow(player, topInventory);
        if (topInventory == null || window == null) {
            return;
        }

        // InvUI validates input actions for its own VirtualInventory slots. Blocking those
        // actions here makes editor input slots unusable.
        if (hasEditableInventorySlots(window)) {
            return;
        }

        InventoryAction action = event.getAction();
        Inventory clickedInventory = event.getClickedInventory();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY
                || action == InventoryAction.COLLECT_TO_CURSOR
                || action == InventoryAction.HOTBAR_MOVE_AND_READD
                || action == InventoryAction.HOTBAR_SWAP
                || event.getClick().isShiftClick()
                || event.getClick().name().contains("NUMBER_KEY")) {

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            event.setCursor(null);
            Bukkit.getScheduler().runTask(BaseLibrary.getInstance(), player::updateInventory);
            Bukkit.getScheduler().runTaskLater(BaseLibrary.getInstance(), player::updateInventory, 2L);
            return;
        }

        if (clickedInventory != null && clickedInventory.equals(player.getInventory())) {
            if (event.getClick().isShiftClick()) {
                event.setCancelled(true);
                event.setCursor(null);
                Bukkit.getScheduler().runTask(BaseLibrary.getInstance(), player::updateInventory);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Bukkit.getScheduler().runTask(BaseLibrary.getInstance(), player::updateInventory);
    }

    /**
     * InvUI registers open windows in {@link WindowManager}.
     * Do not use {@code holder == null}: many vanilla/custom inventories have a null holder
     * and must not be treated as menus.
     */
    static boolean isInvUiMenu(Player player, Inventory topInventory) {
        return getInvUiWindow(player, topInventory) != null;
    }

    private static Window getInvUiWindow(Player player, Inventory topInventory) {
        if (topInventory == null) {
            return null;
        }
        WindowManager manager = WindowManager.getInstance();
        Window byInventory = manager.getWindow(topInventory);
        if (byInventory != null) {
            return byInventory;
        }
        return manager.getOpenWindow(player);
    }

    private static boolean hasEditableInventorySlots(Window window) {
        final Object guiObject;
        try {
            guiObject = window.getClass().getMethod("getGui").invoke(window);
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
        if (!(guiObject instanceof Gui)) return false;

        SlotElement[] elements = ((Gui) guiObject).getSlotElements();
        for (SlotElement element : elements) {
            if (element == null) continue;
            SlotElement holdingElement = element.getHoldingElement();
            if (holdingElement instanceof SlotElement.InventorySlotElement) {
                return true;
            }
        }
        return false;
    }
}
