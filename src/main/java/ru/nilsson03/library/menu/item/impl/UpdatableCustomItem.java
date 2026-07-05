package ru.nilsson03.library.menu.item.impl;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.invui.item.builder.ItemBuilder;
import ru.nilsson03.library.invui.item.impl.AutoUpdateItem;
import ru.nilsson03.library.menu.command.MenuAction;
import ru.nilsson03.library.menu.command.factory.MenuActionFactory;
import ru.nilsson03.library.menu.item.CustomItem;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

public class UpdatableCustomItem extends AutoUpdateItem implements CustomItem {

    private final ConfigurationSection section;
    private final char c;
    private final Consumer<StaticCustomItem.ClickContext> clickHandler;
    private final List<MenuAction> actions;

    public UpdatableCustomItem(ConfigurationSection section, ReplaceData... replacesData) {
        this(section, null, replacesData);
    }

    public UpdatableCustomItem(ConfigurationSection section, Consumer<StaticCustomItem.ClickContext> clickHandler, ReplaceData... replacesData) {
        this(section, clickHandler, () -> replacesData);
    }

    public UpdatableCustomItem(ConfigurationSection section, Consumer<StaticCustomItem.ClickContext> clickHandler, Supplier<ReplaceData[]> replacesDataSupplier) {
        super(20, () -> {
            ReplaceData[] replacesData = replacesDataSupplier.get();
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

            String displayName = section.contains("display_name") ? section.getString("display_name") : section.getString("name");
            if (displayName == null) displayName = "";
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
        String positionStr = section.contains("char") ? section.getString("char") : section.getString("position");
        c = positionStr != null ? positionStr.charAt(0) : ' ';
        this.actions = MenuActionFactory.createFromSection(section);
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
        
        super.handleClick(clickType, player, event);
        if (clickHandler != null) {
            clickHandler.accept(new StaticCustomItem.ClickContext(clickType, player, event));
        }
        if (actions != null && !actions.isEmpty())  {
            for (MenuAction menuAction : actions) {
                menuAction.execute(player);
            }
        }
        notifyWindows();
    }

    public char getPosition() {
        return c;
    }
}
