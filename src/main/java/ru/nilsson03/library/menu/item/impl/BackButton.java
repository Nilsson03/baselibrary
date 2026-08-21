package ru.nilsson03.library.menu.item.impl;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.invui.gui.PagedGui;
import ru.nilsson03.library.invui.item.ItemProvider;
import ru.nilsson03.library.invui.item.builder.ItemBuilder;
import ru.nilsson03.library.invui.item.impl.controlitem.PageItem;
import ru.nilsson03.library.text.api.UniversalTextApi;

import java.util.List;

public class BackButton extends PageItem {

    private static final String PATH = "menu.items.buttons.back-button";

    private final FileConfiguration config;

    public BackButton(FileConfiguration config) {
        super(false);
        this.config = config;
    }

    public ItemProvider getItemProvider(PagedGui<?> gui) {
        String type = config.getString(PATH + ".type", "material");
        String displayName = UniversalTextApi.colorize(config.getString(PATH + ".name", "&7◀ Назад"));
        List<String> lore = UniversalTextApi.colorize(config.getStringList(PATH + ".lore"));

        ItemStack itemStack;
        if (type.equalsIgnoreCase("head")) {
            String url = config.getString(PATH + ".head-id", "");
            itemStack = ItemUtil.createHead(url)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        } else {
            String materialName = config.getString(PATH + ".material", "ARROW");
            Material material = Material.matchMaterial(materialName);
            if (material == null) {
                material = Material.ARROW;
            }
            itemStack = new SpigotItemBuilder(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        }

        return new ItemBuilder(itemStack);
    }
}
