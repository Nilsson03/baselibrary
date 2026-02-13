package ru.nilsson03.library.menu.item.impl;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.menu.item.CustomItem;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AutoUpdateItem;

import java.util.List;
import java.util.function.Consumer;

public class UpdatableCustomItem extends AutoUpdateItem implements CustomItem {

    private final ConfigurationSection section;
    private final char c;
    private final Consumer<StaticCustomItem.ClickContext> clickHandler;

    public UpdatableCustomItem(ConfigurationSection section, ReplaceData... replacesData) {
        this(section, null, replacesData);
    }

    public UpdatableCustomItem(ConfigurationSection section, Consumer<StaticCustomItem.ClickContext> clickHandler, ReplaceData... replacesData) {
        super(20, () -> {
            ItemStack itemStack;
            if (section.getString("type", "material").equalsIgnoreCase("head")) {
                itemStack =  ItemUtil.createHead(section.getString("head-id"))
                        .build();
            } else {
                itemStack = new ItemStack(Material.valueOf(section.getString("material")));
            }

            ItemMeta meta = itemStack.getItemMeta();

            SpigotItemBuilder builder = new SpigotItemBuilder(itemStack)
                    .setMeta(meta);

            String displayName = section.getString("name");
            if (replacesData.length != 0) {
                displayName = UniversalTextApi.replacePlaceholders(displayName, replacesData);
            }
            builder.setDisplayName(displayName);

            if (section.contains("lore")) {
                List<String> lore = section.getStringList("lore");
                if (replacesData.length != 0) {
                    lore = UniversalTextApi.replacePlaceholders(lore, replacesData);
                }
                builder.setLore(lore);
            }

            return new ItemBuilder(builder.build());
        });
        this.clickHandler = clickHandler;
        this.section = section;
        c = section.getString("position").charAt(0);
    }

    @Override
    public ConfigurationSection section() {
        return section;
    }

    @Override
    public char getChar() {
        return c;
    }

    @Override
    public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
        super.handleClick(clickType, player, event);
        if (clickHandler != null) {
            clickHandler.accept(new StaticCustomItem.ClickContext(clickType, player, event));
        }
    }

    public char getPosition() {
        return c;
    }
}
