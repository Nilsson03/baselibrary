package ru.nilsson03.library.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import ru.nilsson03.library.BaseLibrary;
import ru.nilsson03.library.invui.gui.AbstractGui;

public class MenuProtectionListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory topInventory = event.getView().getTopInventory();
        
        if (topInventory == null) {
            return;
        }

        InventoryHolder holder = topInventory.getHolder();

        boolean isCustomGui = holder == null || 
                              holder.getClass().getName().contains("invui") ||
                              holder.getClass().getName().contains("InvUI") ||
                              (holder instanceof AbstractGui);

        if (!isCustomGui) {
            return;
        }

        InventoryAction action = event.getAction();
        Inventory clickedInventory = event.getClickedInventory();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || 
            action == InventoryAction.COLLECT_TO_CURSOR ||
            action == InventoryAction.HOTBAR_MOVE_AND_READD ||
            action == InventoryAction.HOTBAR_SWAP ||
            event.getClick().isShiftClick() ||
            event.getClick().name().contains("NUMBER_KEY")) {

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
}
