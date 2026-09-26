package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import ru.nilsson03.library.bukkit.item.builder.ItemBuilder;
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
    private boolean itemMetaChanged;

    public SpigotItemBuilder() {
        this(Material.STONE);
    }

    public SpigotItemBuilder(Material material) {
        this(new ItemStack(Objects.requireNonNull(material)));
    }

    public SpigotItemBuilder(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack.clone());
    }

    public SpigotItemBuilder update(ItemStack itemStack) {
        this.itemStack = Objects.requireNonNull(itemStack).clone();
        this.itemMeta = null;
        this.itemMetaChanged = false;
        return this;
    }

    @Deprecated
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
        ItemMeta meta = getOrCreateMeta();
        List<String> lore = Optional.ofNullable(meta.getLore())
                .orElse(new ArrayList<>());
        lore.add(UniversalTextApi.colorize(line));
        meta.setLore(lore);
        itemMetaChanged = true;
        return this;
    }

    public SpigotItemBuilder setLore(List<String> lines) {
        getOrCreateMeta().setLore(UniversalTextApi.colorize(lines));
        itemMetaChanged = true;
        return this;
    }

    public SpigotItemBuilder setLeatherColor(Color color) {
        if (LEATHER_ARMOR.contains(itemStack.getType())) {
            ItemMeta meta = getOrCreateMeta();
            if (meta instanceof LeatherArmorMeta) {
                ((LeatherArmorMeta) meta).setColor(color);
                itemMetaChanged = true;
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
        getOrCreateMeta().addEnchant(enchantment, level, true);
        itemMetaChanged = true;
        return this;
    }

    public SpigotItemBuilder removeEnchant(Enchantment enchantment) {
        getOrCreateMeta().removeEnchant(enchantment);
        itemMetaChanged = true;
        return this;
    }

    public SpigotItemBuilder addFlag(ItemFlag flag) {
        getOrCreateMeta().addItemFlags(flag);
        itemMetaChanged = true;
        return this;
    }

    public SpigotItemBuilder setUnbreakable(boolean unbreakable) {
        getOrCreateMeta().setUnbreakable(unbreakable);
        itemMetaChanged = true;
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
            getOrCreateMeta().setCustomModelData(data);
            itemMetaChanged = true;
        }
        return this;
    }

    public SpigotItemBuilder setDisplayName(String name) {
        getOrCreateMeta().setDisplayName(UniversalTextApi.colorize(name));
        itemMetaChanged = true;
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
        this.itemMetaChanged = true;
        return this;
    }

    public SpigotItemBuilder glowing() {
        ItemMeta meta = getOrCreateMeta();
        if (meta.hasEnchant(Enchantment.LUCK)) {
            meta.removeEnchant(Enchantment.LUCK);
        } else {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            addFlag(ItemFlag.HIDE_ENCHANTS);
        }
        itemMetaChanged = true;
        return this;
    }

    @Override
    public ItemStack build() {
        // On legacy Bukkit/Purpur versions, writing an unchanged ItemMeta back can
        // discard raw NBT tags unknown to Bukkit. Keep an untouched source stack
        // byte-for-byte equivalent unless this builder actually changed its meta.
        if (itemMetaChanged) {
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack.clone();
    }

    public ItemBuilder apply(Consumer<ItemMeta> metaConsumer) {
        metaConsumer.accept(getOrCreateMeta());
        itemMetaChanged = true;
        return this;
    }

    private void updateMeta() {
        this.itemMeta = null;
        this.itemMetaChanged = false;
    }

    private ItemMeta getOrCreateMeta() {
        if (itemMeta == null) {
            itemMeta = Optional.ofNullable(itemStack.getItemMeta())
                    .orElse(Bukkit.getItemFactory().getItemMeta(itemStack.getType()));
        }
        return itemMeta;
    }
}
