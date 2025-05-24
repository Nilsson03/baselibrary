package ru.nilsson03.library.bukkit.item.builder;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * @author Nicholas Alexandrov 25.05.2023
 */
public interface ItemBuilder {

    ItemBuilder update(ItemStack itemStack);

    ItemBuilder setDurability(short durability);
    
    ItemBuilder setItem(ItemStack itemstack);
    
    ItemBuilder addLine(String line);
    
    ItemBuilder setLore(List<String> lines);
    
    ItemBuilder setLeatherColor(Color color);
    
    ItemBuilder setDyeColor(DyeColor dyeColor);

    ItemBuilder addEnchant(Enchantment enchantment, int level);
    
    ItemBuilder removeEnchant(Enchantment enchantment);
    
    ItemBuilder addFlag(ItemFlag flag);
    
    ItemBuilder setUnbreakable(boolean bool);

    ItemBuilder setAmount(long count);
    
    ItemBuilder setCustomModelData(int data);
    
    ItemBuilder setDisplayName(String name);
    
    ItemBuilder setType(Material material);

    ItemBuilder setType(String material);

    ItemBuilder setMeta(ItemMeta meta);
    
    ItemBuilder glowing();
    
    ItemStack build();
}
