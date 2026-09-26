package ru.nilsson03.library.bukkit.item.editor;

import org.bukkit.inventory.ItemStack;

/** Small ready-to-use implementation for plugins that do not have a model yet. */
public final class SimpleItemEditorEntry implements ItemEditorEntry {
    private final String id;
    private ItemStack item;
    private int rarity;
    private double chance;
    private int minAmount;
    private int maxAmount;

    public SimpleItemEditorEntry(String id, ItemStack item, int rarity, double chance,
                                 int minAmount, int maxAmount) {
        if (id == null || id.trim().isEmpty()) throw new IllegalArgumentException("id cannot be blank");
        if (item == null || item.getType().isAir()) throw new IllegalArgumentException("item cannot be empty");
        this.id = id;
        this.item = item.clone();
        setRarity(rarity);
        setChance(chance);
        setAmounts(minAmount, maxAmount);
    }

    @Override public String getId() { return id; }
    @Override public ItemStack getItem() { return item.clone(); }
    @Override public void setItem(ItemStack item) {
        if (item == null || item.getType().isAir()) throw new IllegalArgumentException("item cannot be empty");
        this.item = item.clone();
    }
    @Override public int getRarity() { return rarity; }
    @Override public void setRarity(int rarity) { this.rarity = Math.max(1, rarity); }
    @Override public double getChance() { return chance; }
    @Override public void setChance(double chance) { this.chance = Math.max(0, Math.min(100, chance)); }
    @Override public int getMinAmount() { return minAmount; }
    @Override public int getMaxAmount() { return maxAmount; }
    @Override public void setAmounts(int minAmount, int maxAmount) {
        this.minAmount = Math.max(1, minAmount);
        this.maxAmount = Math.max(this.minAmount, maxAmount);
    }
}
