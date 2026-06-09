package ru.nilsson03.library.menu.item.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.invui.item.ItemProvider;
import ru.nilsson03.library.invui.item.builder.ItemBuilder;
import ru.nilsson03.library.invui.item.impl.SimpleItem;
import ru.nilsson03.library.menu.MenuHistoryManager;
import ru.nilsson03.library.text.api.UniversalTextApi;

import java.util.List;
import java.util.stream.Collectors;

public class NavigationBackButton extends SimpleItem {

    public NavigationBackButton(FileConfiguration config) {
        super(getBackButtonItemProvider(config));
    }

    @Override
    public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        
        InventoryAction action = event.getAction();
        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || 
            action == InventoryAction.COLLECT_TO_CURSOR ||
            clickType.isShiftClick()) {
            event.setCurrentItem(null);
            event.setCursor(null);
            Bukkit.getScheduler().runTask(ru.nilsson03.library.BaseLibrary.getInstance(), () -> {
                player.updateInventory();
            });
        }
        
        if (MenuHistoryManager.hasHistory(player)) {
            MenuHistoryManager.openPreviousMenu(player);
        } else {
            player.closeInventory();
        }
    }

    private static ItemProvider getBackButtonItemProvider(FileConfiguration config) {
        String type = config.getString("inventories.buttons.previous-button.type", "material");
        String displayName = config.getString("inventories.buttons.previous-button.name");
        if (displayName != null) {
            displayName = UniversalTextApi.colorize(displayName);
        }
        
        List<String> lore = config.getStringList("inventories.buttons.previous-button.lore");
        if (lore != null && !lore.isEmpty()) {
            lore = lore.stream()
                    .map(UniversalTextApi::colorize)
                    .collect(Collectors.toList());
        }

        ItemStack itemStack;
        if (type.equalsIgnoreCase("head")) {
            String url = config.getString("inventories.buttons.previous-button.head-id", "");
            itemStack = ItemUtil.createHead(url)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        } else {
            String materialName = config.getString("inventories.buttons.previous-button.material", "ARROW");
            itemStack = new SpigotItemBuilder(Material.valueOf(materialName))
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        }

        return new ItemBuilder(itemStack);
    }
}
