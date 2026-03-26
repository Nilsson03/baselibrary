package ru.nilsson03.library.menu.item.impl;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.file.configuration.ParameterFile;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.text.api.UniversalTextApi;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

import java.util.List;

public class BackButton extends PageItem {

    private final FileConfiguration config;

    public BackButton(ParameterFile config) {
        super(false);
        this.config = config.getFileConfiguration();
    }

    public BackButton(BukkitConfig config) {
        super(false);
        this.config = config.getFileConfiguration();
    }

    public ItemProvider getItemProvider(PagedGui<?> gui) {
        String type = config.getString("inventories.buttons.back-button.type");
        String displayName = UniversalTextApi.colorize(config.getString("inventories.buttons.back-button.name"));
        List<String> lore = UniversalTextApi.colorize(config.getStringList("inventories.buttons.back-button.lore"));

        ItemStack itemStack;
        if (type.equalsIgnoreCase("head")) {
            String url = config.getString("inventories.buttons.back-button.head-id");
            itemStack = ItemUtil.createHead(url)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        } else {
            String materialName = config.getString("inventories.buttons.back-button.material");
             itemStack = new SpigotItemBuilder(Material.valueOf(materialName))
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        }

        return new ItemBuilder(itemStack);
    }
}
