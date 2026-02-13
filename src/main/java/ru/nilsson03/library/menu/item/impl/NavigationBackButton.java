package ru.nilsson03.library.menu.item.impl;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.menu.MenuHistoryManager;
import ru.nilsson03.library.text.api.UniversalTextApi;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;

import java.util.List;
import java.util.stream.Collectors;

public class NavigationBackButton extends SimpleItem {

    public NavigationBackButton(FileConfiguration config) {
        super(getBackButtonItemProvider(config));
    }

    @Override
    public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
        event.setCancelled(true);
        
        if (MenuHistoryManager.hasHistory(player)) {
            MenuHistoryManager.openPreviousMenu(player);
        } else {
            player.closeInventory();
        }
    }

    private static ItemProvider getBackButtonItemProvider(FileConfiguration config) {
        String type = config.getString("inventories.buttons.back-button.type", "material");
        String displayName = config.getString("inventories.buttons.back-button.name");
        if (displayName != null) {
            displayName = UniversalTextApi.colorize(displayName);
        }
        
        List<String> lore = config.getStringList("inventories.buttons.back-button.lore");
        if (lore != null && !lore.isEmpty()) {
            lore = lore.stream()
                    .map(UniversalTextApi::colorize)
                    .collect(Collectors.toList());
        }

        ItemStack itemStack;
        if (type.equalsIgnoreCase("head")) {
            String url = config.getString("inventories.buttons.back-button.head-id", "");
            itemStack = ItemUtil.createHead(url)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        } else {
            String materialName = config.getString("inventories.buttons.back-button.material", "ARROW");
            itemStack = new SpigotItemBuilder(Material.valueOf(materialName))
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        }

        return new ItemBuilder(itemStack);
    }
}
