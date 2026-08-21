package ru.nilsson03.library.bukkit.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import ru.nilsson03.library.bukkit.item.builder.SkullItemBuilder;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.item.builder.impl.UniversalSkullBuilder;
import ru.nilsson03.library.bukkit.item.skull.SkullTextureHandler;
import ru.nilsson03.library.bukkit.item.skull.factory.SkullHandlerFactory;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

public class ItemUtil {

    private static final SkullTextureHandler handler;
    private static final SkullItemBuilder builder;

    static {
        handler = SkullHandlerFactory.createHandler();
        builder = new UniversalSkullBuilder(handler);
    }

    public static boolean itemHasEnchantments(ItemStack itemStack, Enchantment... enchantments) {
        Objects.requireNonNull(itemStack, "ItemStack can't  be null");
        Objects.requireNonNull(enchantments, "Enchantments can't be null");

        int enchantmentsCount = enchantments.length;
        if (enchantmentsCount == 0) {
            ConsoleLogger.debug("baselibrary",
                    "The enchantment array passed to the itemHasEnchantments() method is empty, returned false (length %s)",
                    enchantmentsCount);
            return false;
        }

        if (!itemStack.getEnchantments().isEmpty())
            return false;

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

        if (!itemStack.getEnchantments().isEmpty())
            return false;

        return itemStack.getEnchantments()
                .keySet()
                .stream()
                .anyMatch(itemEnchantment -> itemEnchantment.equals(enchantment));
    }

    public static ItemStack createItem(FileConfiguration configuration,
            String path,
            ReplaceData... replaceData) {
        Objects.requireNonNull(configuration, "configuration can't be null");
        Objects.requireNonNull(path, "path can't be null");

        String type = configuration.getString(path + ".type", "material");
        List<String> lore = new ArrayList<>();
        if (configuration.contains(path + ".lore")) {
            lore = UniversalTextApi.colorize(
                    UniversalTextApi.replacePlaceholders(configuration.getStringList(path + ".lore"), replaceData)
            );
        }
        String displayName = "";
        if (configuration.contains(path + ".displayName")) {
            String rawName = configuration.getString(path + ".displayName", "");
            displayName = UniversalTextApi.colorize(
                    UniversalTextApi.replacePlaceholders(rawName, replaceData)
            );
        }

        if (type.equalsIgnoreCase("head")) {
            String texture = configuration.getString(path + ".head-id", "");
            return createHead(texture)
                    .setLore(lore)
                    .setDisplayName(displayName)
                    .build();
        } else {
            String material = configuration.getString(path + ".material", "STONE");
            return new SpigotItemBuilder()
                    .setType(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        }
    }

    public static SkullItemBuilder createHead(String url) {
        return builder.setSkinTexture(url);
    }
}
