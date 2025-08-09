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

    public SpigotItemBuilder update(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack).clone();
        this.itemMeta = Optional.ofNullable(itemStack.getItemMeta())
                .orElse(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
        return this;
    }

    public SpigotItemBuilder setDurability(short durability) {
        if (itemStack.getType().getMaxDurability() > 0) {
            itemStack.setDurability(durability);
        }
        return this;
    }

    public SpigotItemBuilder setItem(ItemStack itemStack) {
        return update(itemStack);
    }

    public SpigotItemBuilder addLine(String line) {
        List<String> lore = Optional.ofNullable(itemMeta.getLore())
                .orElse(new ArrayList<>());
        lore.add(UniversalTextApi.colorize(line));
        itemMeta.setLore(lore);
        return this;
    }

    public SpigotItemBuilder setLore(List<String> lines) {
        itemMeta.setLore(UniversalTextApi.colorize(lines));
        return this;
    }

    public SpigotItemBuilder setLeatherColor(Color color) {
        if (LEATHER_ARMOR.contains(itemStack.getType())) {
            if (itemMeta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) itemMeta).setColor(color);
            }
        }
        return this;
    }

    public SpigotItemBuilder setDyeColor(DyeColor dyeColor) {
        try {
            Material dyeMaterial = Material.valueOf(dyeColor.name() + "_DYE");
            itemStack.setType(dyeMaterial);
        } catch (IllegalArgumentException e) {
            itemStack.setType(Material.WHITE_DYE);
        }
        return this;
    }

    public SpigotItemBuilder addEnchant(Enchantment enchantment, int level) {
        itemMeta.addEnchant(enchantment, level, true);
        return this;
    }

    public SpigotItemBuilder removeEnchant(Enchantment enchantment) {
        itemMeta.removeEnchant(enchantment);
        return this;
    }

    public SpigotItemBuilder addFlag(ItemFlag flag) {
        itemMeta.addItemFlags(flag);
        return this;
    }

    public SpigotItemBuilder setUnbreakable(boolean unbreakable) {
        itemMeta.setUnbreakable(unbreakable);
        return this;
    }

    public SpigotItemBuilder setAmount(long count) {
        if (count > 0 && count <= Integer.MAX_VALUE) {
            itemStack.setAmount((int) count);
        }
        return this;
    }

    public SpigotItemBuilder setCustomModelData(int data) {
        if (data >= 0) {
            itemMeta.setCustomModelData(data);
        }
        return this;
    }

    public SpigotItemBuilder setDisplayName(String name) {
        itemMeta.setDisplayName(UniversalTextApi.colorize(name));
        return this;
    }

    public SpigotItemBuilder setType(Material material) {
        itemStack.setType(material);
        updateMeta();
        return this;
    }

    public SpigotItemBuilder setType(String materialName) {
        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            setType(material);
        } catch (IllegalArgumentException e) {
            setType(Material.STONE);
        }
        return this;
    }

    public SpigotItemBuilder setMeta(ItemMeta meta) {
        this.itemMeta = Objects.requireNonNull(meta);
        return this;
    }

    public SpigotItemBuilder glowing() {
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
