package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ru.nilsson03.library.bukkit.item.builder.ItemBuilder;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.text.api.UniversalTextApi;

import java.util.*;
import java.util.function.Consumer;

public class SpigotItemBuilder implements ItemBuilder {

    private static final Set<Material> LEATHER_ARMOR = EnumSet.of(
            Material.LEATHER_HELMET,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_LEGGINGS,
            Material.LEATHER_BOOTS
    );

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public SpigotItemBuilder() {
        this(Material.STONE);
    }

    public SpigotItemBuilder(Material material) {
        this(new ItemStack(Objects.requireNonNull(material)));
    }

    public SpigotItemBuilder(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack.clone());
        this.itemMeta = Optional.ofNullable(itemStack.getItemMeta())
                .orElse(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
    }

    @Override
    public ItemBuilder update(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack).clone();
        this.itemMeta = Optional.ofNullable(itemStack.getItemMeta())
                .orElse(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
        return this;
    }

    @Override
    public ItemBuilder setDurability(short durability) {
        if (itemStack.getType().getMaxDurability() > 0) {
            itemStack.setDurability(durability);
        }
        return this;
    }

    @Override
    public ItemBuilder setItem(ItemStack itemStack) {
        return update(itemStack);
    }

    @Override
    public ItemBuilder addLine(String line) {
        List<String> lore = Optional.ofNullable(itemMeta.getLore())
                .orElse(new ArrayList<>());
        lore.add(UniversalTextApi.colorize(line));
        itemMeta.setLore(lore);
        return this;
    }

    @Override
    public ItemBuilder setLore(List<String> lines) {
        ConsoleLogger.debug("baselibrary", "Setting lore %s", lines);
        itemMeta.setLore(UniversalTextApi.colorize(new ArrayList<>(lines)));
        return this;
    }

    @Override
    public ItemBuilder setLeatherColor(Color color) {
        if (LEATHER_ARMOR.contains(itemStack.getType())) {
            if (itemMeta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) itemMeta).setColor(color);
            }
        }
        return this;
    }

    @Override
    public ItemBuilder setDyeColor(DyeColor dyeColor) {
        try {
            Material dyeMaterial = Material.valueOf(dyeColor.name() + "_DYE");
            itemStack.setType(dyeMaterial);
        } catch (IllegalArgumentException e) {
            itemStack.setType(Material.WHITE_DYE);
        }
        return this;
    }

    @Override
    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    @Override
    public ItemBuilder removeEnchant(Enchantment enchantment) {
        itemMeta.removeEnchant(enchantment);
        return this;
    }

    @Override
    public ItemBuilder addFlag(ItemFlag flag) {
        itemMeta.addItemFlags(flag);
        return this;
    }

    @Override
    public ItemBuilder setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    @Override
    public ItemBuilder setAmount(long count) {
        if (count > 0 && count <= Integer.MAX_VALUE) {
            itemStack.setAmount((int) count);
        }
        return this;
    }

    @Override
    public ItemBuilder setCustomModelData(int data) {
        if (data >= 0) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    @Override
    public ItemBuilder setDisplayName(String name) {
        ConsoleLogger.debug("baselibrary","Setting displayName %s", name);
        itemMeta.setDisplayName(UniversalTextApi.colorize(name));
        return this;
    }

    @Override
    public ItemBuilder setType(Material material) {
        itemStack.setType(material);
        updateMeta();
        return this;
    }

    @Override
    public ItemBuilder setType(String materialName) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            setType(material);
        } catch (IllegalArgumentException e) {
            // Fallback to default material
            setType(Material.STONE);
        }
        return this;
    }

    @Override
    public ItemBuilder setMeta(ItemMeta meta) {
        this.itemMeta = Objects.requireNonNull(meta);
        return this;
    }

    @Override
    public ItemBuilder glowing() {
        if (itemMeta.hasEnchant(Enchantment.LUCK)) {
            itemMeta.removeEnchant(Enchantment.LUCK);
        } else {
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            addFlag(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }

    @Override
    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        ConsoleLogger.debug("baselibrary", "Create ItemStack (name %s, lore %s, type %s, amount %s)",
                itemMeta.getDisplayName(),
                itemMeta.getLore(),
                itemStack.getType().name(),
                itemStack.getAmount());
        return itemStack.clone();
    }

    public ItemBuilder apply(Consumer<ItemMeta> metaConsumer) {
        metaConsumer.accept(itemMeta);
        return this;
    }

    private void updateMeta() {
        this.itemMeta = Optional.ofNullable(itemStack.getItemMeta())
                .orElse(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
    }
}
