package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import ru.nilsson03.library.bukkit.item.builder.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public final class PotionBuilder implements ItemBuilder {
    
    private final List<PotionEffect> effects = new ArrayList<>();
    private Color color;
    private PotionData basePotionData;
    private final ItemStack itemStack;
    
    public PotionBuilder(@NotNull PotionType type) {
        this.itemStack = new ItemStack(type.getMaterial());
    }
    
    public PotionBuilder(@NotNull ItemStack base) {
        this.itemStack = base;
    }

    public @NotNull PotionBuilder setColor(@NotNull Color color) {
        this.color = color;
        return this;
    }

    public @NotNull PotionBuilder setColor(@NotNull java.awt.Color color) {
        this.color = Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
        return this;
    }

    public @NotNull PotionBuilder setBasePotionData(@NotNull PotionData basePotionData) {
        this.basePotionData = basePotionData;
        return this;
    }

    public @NotNull PotionBuilder addEffect(@NotNull PotionEffect effect) {
        effects.add(effect);
        return this;
    }

    @Override
    public @NotNull ItemStack build() {
        PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
        
        meta.clearCustomEffects();
        if (color != null) meta.setColor(color);
        if (basePotionData != null) meta.setBasePotionData(basePotionData);
        effects.forEach(effect -> meta.addCustomEffect(effect, true));

        itemStack.setItemMeta(meta);
        return itemStack;
    }
    
    public enum PotionType {
        
        NORMAL(Material.POTION),
        SPLASH(Material.SPLASH_POTION),
        LINGERING(Material.LINGERING_POTION);
        
        private final @NotNull Material material;
        
        PotionType(@NotNull Material material) {
            this.material = material;
        }
        
        public @NotNull Material getMaterial() {
            return material;
        }
        
    }
    
}
