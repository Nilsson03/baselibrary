package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;
import ru.nilsson03.library.bukkit.item.builder.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public final class FireworkBuilder implements ItemBuilder {
    
    private int power = -1;
    private List<FireworkEffect> effects = new ArrayList<>();
    private final ItemStack itemStack;
    
    public FireworkBuilder() {
        this.itemStack = new ItemStack(Material.FIREWORK_ROCKET);
    }
    
    public FireworkBuilder(int amount) {
        this.itemStack = new ItemStack(Material.FIREWORK_ROCKET, amount);
    }
    
    public FireworkBuilder(@NotNull ItemStack base) {
        this.itemStack = base;
    }

    public @NotNull FireworkBuilder setPower(int power) {
        this.power = power;
        return this;
    }

    public @NotNull FireworkBuilder addFireworkEffect(@NotNull FireworkEffect effect) {
        effects.add(effect);
        return this;
    }

    public @NotNull FireworkBuilder addFireworkEffect(@NotNull FireworkEffect.Builder builder) {
        effects.add(builder.build());
        return this;
    }

    public @NotNull FireworkBuilder setFireworkEffects(@NotNull List<@NotNull FireworkEffect> effects) {
        this.effects = effects;
        return this;
    }

    public @NotNull FireworkBuilder clearFireworkEffects() {
        effects.clear();
        return this;
    }

    @Override
    public @NotNull ItemStack build() {
        FireworkMeta meta = (FireworkMeta) itemStack.getItemMeta();
        if (power != -1) meta.setPower(power);
        meta.clearEffects();
        meta.addEffects(effects);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
}
