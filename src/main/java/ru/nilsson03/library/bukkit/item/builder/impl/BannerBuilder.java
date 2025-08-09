package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.jetbrains.annotations.NotNull;
import ru.nilsson03.library.bukkit.item.builder.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public final class BannerBuilder implements ItemBuilder {
    
    private List<Pattern> patterns = new ArrayList<>();
    private final ItemStack itemStack;

    public BannerBuilder(Material material) {
        this.itemStack = new ItemStack(material);
    }
    
    public BannerBuilder(Material material, int amount) {
        this.itemStack = new ItemStack(material, amount);
    }
    
    public BannerBuilder(@NotNull ItemStack base) {
        this.itemStack = new ItemStack(base);
    }

    public @NotNull BannerBuilder addPattern(@NotNull Pattern pattern) {
        patterns.add(pattern);
        return this;
    }

    public @NotNull BannerBuilder addPattern(@NotNull DyeColor color, @NotNull PatternType type) {
        patterns.add(new Pattern(color, type));
        return this;
    }

    public @NotNull BannerBuilder setPatterns(@NotNull List<@NotNull Pattern> patterns) {
        this.patterns = patterns;
        return this;
    }

    public @NotNull BannerBuilder clearPatterns() {
        patterns.clear();
        return this;
    }

    @Override
    public @NotNull ItemStack build() {
        BannerMeta meta = (BannerMeta) itemStack.getItemMeta();
        meta.setPatterns(patterns);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
