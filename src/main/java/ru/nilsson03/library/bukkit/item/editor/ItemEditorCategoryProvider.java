package ru.nilsson03.library.bukkit.item.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Describes categories shown before the item list. A category may be a world,
 * rarity, event phase, case name, or any other plugin-specific key.
 */
public interface ItemEditorCategoryProvider<C> {

    /** Creates a provider without an anonymous class for the common case. */
    static <C> ItemEditorCategoryProvider<C> of(
            Function<Player, ? extends Collection<? extends C>> categories,
            Function<C, String> names) {
        if (categories == null || names == null) throw new IllegalArgumentException("category functions cannot be null");
        return new ItemEditorCategoryProvider<C>() {
            @Override public Collection<? extends C> getCategories(Player player) { return categories.apply(player); }
            @Override public String getName(C category) { return names.apply(category); }
        };
    }

    Collection<? extends C> getCategories(Player player);

    String getName(C category);

    default ItemStack createIcon(C category, int itemCount) {
        return new SpigotItemBuilder(Material.GRASS_BLOCK)
                .setDisplayName("§b" + getName(category))
                .setLore(lore(
                        "§7Предметов: §f" + itemCount,
                        "",
                        "§8▸ §eОткрыть редактор"
                ))
                .build();
    }

    default String getItemsTitle(C category) {
        return "§8Предметы §7| §f" + getName(category);
    }

    default String getCategoryTitle() {
        return "§8Редактор предметов";
    }

    default List<String> lore(String... lines) {
        List<String> result = new ArrayList<>(lines.length);
        Collections.addAll(result, lines);
        return result;
    }
}
