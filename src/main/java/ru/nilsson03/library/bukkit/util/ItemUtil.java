package ru.nilsson03.library.bukkit.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.file.configuration.ConfigOperations;
import ru.nilsson03.library.bukkit.item.builder.SkullItemBuilder;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.bukkit.item.builder.impl.UniversalSkullBuilder;
import ru.nilsson03.library.bukkit.item.skull.SkullTextureHandler;
import ru.nilsson03.library.bukkit.item.skull.factory.SkullHandlerFactory;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.*;

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

    public static ItemStack createItem(BukkitConfig config,
                                       String path,
                                       ReplaceData... replaceData) {
        ConfigOperations configOperations = config.operations();
        String type = configOperations.getString(path + ".type", "material");
        String displayName = UniversalTextApi.colorize(configOperations.getString(path + ".name", replaceData));
        List<String> lore = UniversalTextApi.colorize(configOperations.getList(path + ".lore", replaceData));

        if (type.equalsIgnoreCase("head")) {
            String texture = configOperations.getString(path + ".head-id");
            return createHead(texture)
                    .setLore(lore)
                    .setDisplayName(displayName)
                    .build();
        } else {
            String material = configOperations.getString(path + ".material");
            return new SpigotItemBuilder()
                    .setType(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .build();
        }
    }

    public static ItemStack fromMap(Map<String, Object> parameters) {
        if (parameters == null || !parameters.containsKey("material")) {
            return null;
        }

        Object itemObj = parameters.get("item");
        if (itemObj instanceof ItemStack) {
            return (ItemStack) itemObj;
        }

        String materialName = parameters.get("material").toString();
        Material material;
        try {
            material = Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }

        int amount = 1;
        if (parameters.containsKey("amount")) {
            amount = Integer.parseInt(parameters.get("amount").toString());
        }

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (parameters.containsKey("name")) {
                meta.setDisplayName(UniversalTextApi.colorize(parameters.get("name").toString()));
            }

            if (parameters.containsKey("lore")) {
                Object loreObj = parameters.get("lore");
                if (loreObj instanceof List) {
                    List<String> lore = new ArrayList<>();
                    for (Object line : (List<?>) loreObj) {
                        lore.add(UniversalTextApi.colorize(line.toString()));
                    }
                    meta.setLore(lore);
                }
            }

            if (parameters.containsKey("enchantments")) {
                Object enchantsObj = parameters.get("enchantments");
                if (enchantsObj instanceof Map) {
                    Map<String, Object> enchants = (Map<String, Object>) enchantsObj;
                    for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                        Enchantment enchantment = getEnchantment(entry.getKey());
                        if (enchantment != null) {
                            int level = Integer.parseInt(entry.getValue().toString());
                            meta.addEnchant(enchantment, level, true);
                        }
                    }
                }
            }

            if (parameters.containsKey("customModelData")) {
                meta.setCustomModelData(Integer.parseInt(parameters.get("customModelData").toString()));
            }

            if (parameters.containsKey("unbreakable")) {
                meta.setUnbreakable(Boolean.parseBoolean(parameters.get("unbreakable").toString()));
            }

            item.setItemMeta(meta);
        }

        if (parameters.containsKey("potionData") &&
                (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION)) {

            Object potionDataObj = parameters.get("potionData");
            Map<String, Object> potionDataMap = null;

            if (potionDataObj instanceof org.bukkit.configuration.MemorySection memorySection) {
                potionDataMap = memorySection.getValues(false);
            } else if (potionDataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tempMap = (Map<String, Object>) potionDataObj;
                potionDataMap = tempMap;
            }

            if (potionDataMap != null) {
                ItemMeta itemMeta = item.getItemMeta();
                if (itemMeta instanceof PotionMeta potionMeta) {
                    Object typeObj = potionDataMap.get("type");
                    PotionType potionType;
                    if (typeObj instanceof String) {
                        potionType = PotionType.valueOf((String) typeObj);
                    } else if (typeObj instanceof PotionType) {
                        potionType = (PotionType) typeObj;
                    } else {
                        throw new IllegalArgumentException("Invalid potion type: " + typeObj);
                    }

                    boolean extended = false;
                    boolean upgraded = false;

                    if (potionDataMap.containsKey("extended")) {
                        Object extendedObj = potionDataMap.get("extended");
                        if (extendedObj instanceof Boolean) {
                            extended = (Boolean) extendedObj;
                        } else if (extendedObj instanceof String) {
                            extended = Boolean.parseBoolean((String) extendedObj);
                        }
                    }

                    if (potionDataMap.containsKey("upgraded")) {
                        Object upgradedObj = potionDataMap.get("upgraded");
                        if (upgradedObj instanceof Boolean) {
                            upgraded = (Boolean) upgradedObj;
                        } else if (upgradedObj instanceof String) {
                            upgraded = Boolean.parseBoolean((String) upgradedObj);
                        }
                    }

                    PotionData potionData = new PotionData(potionType, extended, upgraded);
                    potionMeta.setBasePotionData(potionData);
                    item.setItemMeta(potionMeta);
                }
            }
        }

        return item;
    }

    private static Enchantment getEnchantment(String name) {
        String normalized = name.toLowerCase().replace("_", "").replace(" ", "");

        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
        if (enchant != null) {
            return enchant;
        }

        for (Enchantment e : Enchantment.values()) {
            String enchantName = e.getKey().getKey().toLowerCase().replace("_", "");
            if (enchantName.equals(normalized)) {
                return e;
            }
        }

        return null;
    }

    public static SkullItemBuilder createHead(String url) {
        return builder.setSkinTexture(url);
    }
}
