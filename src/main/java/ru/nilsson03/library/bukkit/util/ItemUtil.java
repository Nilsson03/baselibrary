package ru.nilsson03.library.bukkit.util;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Objects;
import java.util.Set;

public class ItemUtil {

    public static boolean itemHasEnchantments(ItemStack itemStack, Enchantment... enchantments) {
        Objects.requireNonNull(itemStack, "ItemStack can't  be null");
        Objects.requireNonNull(enchantments, "Enchantments can't be null");

        int enchantmentsCount = enchantments.length;
        if (enchantmentsCount == 0) {
            ConsoleLogger.debug("baselibrary", "The enchantment array passed to the itemHasEnchantments() method is empty, returned false (length %s)", enchantmentsCount);
            return false;
        }

        if (!itemStack.getEnchantments().isEmpty()) return false;

        Set<Enchantment> itemEnchantments = itemStack.getEnchantments()
                .keySet();

        for (Enchantment enchantment : enchantments) {
            if (!itemEnchantments.contains(enchantment))
                return false;
        }

        return true;
    }

    public static boolean containsEnchantment(ItemStack itemStack, Enchantment enchantment) {
        Objects.requireNonNull(itemStack, "ItemStack can't be null");
        Objects.requireNonNull(enchantment, "Enchantment can't be null");

        if (!itemStack.getEnchantments().isEmpty()) return false;

        return itemStack.getEnchantments()
                .keySet()
                .stream()
                .anyMatch(itemEnchantment -> itemEnchantment.equals(enchantment));
    }
}
