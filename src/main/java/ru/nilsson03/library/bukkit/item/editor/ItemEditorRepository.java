package ru.nilsson03.library.bukkit.item.editor;

import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/** Persistence adapter for an item editor. The library never owns plugin data. */
public interface ItemEditorRepository<C> {

    Collection<? extends ItemEditorEntry> getItems(C category);

    void save(C category, ItemEditorEntry entry);

    void remove(C category, ItemEditorEntry entry);

    /** Adds a new record with the editor defaults (chance 100, rarity 1, amount 1..stack amount). */
    void add(C category, ItemStack item);
}
