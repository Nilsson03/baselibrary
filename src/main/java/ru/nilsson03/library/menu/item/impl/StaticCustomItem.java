package ru.nilsson03.library.menu.item.impl;

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
import ru.nilsson03.library.invui.item.ItemProvider;
import ru.nilsson03.library.invui.item.builder.ItemBuilder;
import ru.nilsson03.library.invui.item.impl.AbstractItem;
import ru.nilsson03.library.menu.command.MenuAction;
import ru.nilsson03.library.menu.command.factory.MenuActionFactory;
import ru.nilsson03.library.menu.item.CustomItem;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.List;
import java.util.function.Consumer;

public class StaticCustomItem extends AbstractItem implements CustomItem {

    private final ConfigurationSection section;
    private final ItemProvider itemProvider;
    private final char c;
    private final Consumer<ClickContext> clickHandler;
    private final List<MenuAction> actions;

    public StaticCustomItem(ConfigurationSection section, ReplaceData... replacesData) {
        this(section, null, replacesData);
    }

    public StaticCustomItem(ConfigurationSection section, Consumer<ClickContext> clickHandler, ReplaceData... replacesData) {
        this.section = section;
        this.clickHandler = clickHandler;
        ItemStack itemStack = buildItem(replacesData);
        this.itemProvider = new ItemBuilder(itemStack);
        c = section.getString("position").charAt(0);
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
    public ItemProvider getItemProvider() {
        return itemProvider;
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
        
        if (actions != null && !actions.isEmpty())  {
            for (MenuAction menuAction : actions) {
                menuAction.execute(player);
            }
        }
        if (clickHandler != null) {
            clickHandler.accept(new ClickContext(clickType, player, event));
        }
        notifyWindows();
    }

    public char getPosition() {
        return c;
    }

    private ItemStack buildItem(ReplaceData... replacesData) {
        ItemStack item = createBaseItem();
        ItemMeta meta = item.getItemMeta();

        SpigotItemBuilder builder = new SpigotItemBuilder(item)
                .setMeta(meta);

        builder.setDisplayName(UniversalTextApi.replacePlaceholders(section.getString("name"), replacesData));

        if (section.contains("lore")) {
            List<String> lore = section.getStringList("lore");
            builder.setLore(UniversalTextApi.replacePlaceholders(lore, replacesData));
        }

        return builder.build();
    }

    private ItemStack createBaseItem() {
        if (section.getString("type", "material").equalsIgnoreCase("head")) {
            return createHeadItem(section.getString("head-id"));
        }
        return new ItemStack(Material.valueOf(section.getString("material")));
    }

    private ItemStack createHeadItem(String texture) {
        return ItemUtil.createHead(texture)
                .build();
    }

    public static class ClickContext {
        private final ClickType clickType;
        private final Player player;
        private final InventoryClickEvent event;

        public ClickContext(ClickType clickType, Player player, InventoryClickEvent event) {
            this.clickType = clickType;
            this.player = player;
            this.event = event;
        }

        public ClickType getClickType() {
            return clickType;
        }

        public Player getPlayer() {
            return player;
        }

        public InventoryClickEvent getEvent() {
            return event;
        }
    }
}
