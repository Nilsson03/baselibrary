package ru.nilsson03.library.menu.item;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import xyz.xenondevs.invui.item.Item;

public interface CustomItem extends Item {
    
    ConfigurationSection section();
    
    char getChar();
    
    default void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
        event.setCancelled(true);
    }
}
