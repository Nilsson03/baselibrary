package ru.nilsson03.library.bukkit.item.editor;

import org.bukkit.inventory.ItemStack;

/**
 * Mutable item record used by {@link ItemEditorMenu}. Implementations normally
 * delegate these methods to the plugin's own loot/case/event model.
 */
public interface ItemEditorEntry {

    /** Stable identifier used by the backing store. */
    String getId();

    /** A clone is expected to be returned so an open menu cannot mutate storage. */
    ItemStack getItem();

    void setItem(ItemStack item);

    /** Optional per-item rarity. Old integrations safely default to level 1. */
    default int getRarity() { return 1; }

    /** Optional per-item rarity. Old integrations can ignore this value. */
    default void setRarity(int rarity) { }

    double getChance();

    void setChance(double chance);

    int getMinAmount();

    int getMaxAmount();

    void setAmounts(int minAmount, int maxAmount);
}
