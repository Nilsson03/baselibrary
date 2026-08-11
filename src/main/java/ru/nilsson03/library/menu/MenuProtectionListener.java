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

        if (topInventory == null || !isInvUiMenu(player, topInventory)) {
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
        WindowManager manager = WindowManager.getInstance();
        Window byInventory = manager.getWindow(topInventory);
        if (byInventory != null) {
            return true;
        }
        return manager.getOpenWindow(player) != null;
    }
}
