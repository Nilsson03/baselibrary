package ru.nilsson03.library.bukkit.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemStackSerialize {

    private static final String DELIMITER = ":";
    private static final String EFFECT_DELIMITER = ";";

    private static final ServerVersion currentVersion = ServerVersionUtils.CURRENT_VERSION;

    public static String serialize(ItemStack item) {
        StringBuilder sb = new StringBuilder()
                .append(item.getType()).append(DELIMITER)
                .append(item.getAmount()).append(DELIMITER);

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta instanceof Damageable) {
                sb.append(((Damageable) meta).getDamage())
                  .append(DELIMITER);
            }

            if (currentVersion.isNewerThan(ServerVersion.v1_14)) {
                if (meta.hasCustomModelData()) {
                    sb.append(meta.getCustomModelData())
                      .append(DELIMITER);
                }
            }

            if (meta instanceof PotionMeta) {
                serializePotionEffects((PotionMeta) meta, sb);
            }
        }

        if (!item.getEnchantments().isEmpty()) {
            sb.append(item.getEnchantments().entrySet().stream()
                          .map(e -> e.getKey().getKey() + DELIMITER + e.getValue())
                          .collect(Collectors.joining(DELIMITER)));
        }

        return sb.toString();
    }

    public static void serializePotionEffects(PotionMeta meta, StringBuilder sb) {
        try {
            PotionType baseType = meta.getBasePotionData()
                                      .getType();
            if (baseType != PotionType.AWKWARD && baseType != PotionType.WATER) {
                PotionEffectType effectType = baseType.getEffectType();
                int duration = meta.getBasePotionData()
                                   .isExtended()
                               ? 9600
                               : 3600;
                int amplifier = meta.getBasePotionData()
                                    .isUpgraded()
                                ? 1
                                : 0;
                sb.append(effectType.getName())
                  .append(DELIMITER)
                  .append(duration / 20)
                  .append(DELIMITER)
                  .append(amplifier)
                  .append(EFFECT_DELIMITER);
            }

            if (meta.hasCustomEffects()) {
                meta.getCustomEffects()
                    .forEach(effect -> sb.append(effect.getType()
                                                       .getName())
                                         .append(DELIMITER)
                                         .append(effect.getDuration())
                                         .append(DELIMITER)
                                         .append(effect.getAmplifier())
                                         .append(EFFECT_DELIMITER));
            }
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "An error occurred when applying the result of parsing the %s string to PotionMeta, the reason is %s.", sb.toString(), e.getMessage());
        }
    }


    public static Optional<ItemStack> deserialize(String data) {
        String[] parts = data.split(DELIMITER);
        if (parts.length < 2) {
            ConsoleLogger.warn("baselibrary", "Invalid %s string for parsing in the ItemStack item (data.split(%s) <2).", data, DELIMITER);
            return Optional.empty();
        }

        try {
            Material type = Material.valueOf(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            ItemStack item = new ItemStack(type, amount);

            if (parts.length > 2) {
                ItemMeta meta = item.getItemMeta();
                int index = 2;

                if (meta instanceof Damageable) {
                    ((Damageable) meta).setDamage(Integer.parseInt(parts[index++]));
                }

                if (currentVersion.isNewerThan(ServerVersion.v1_14)
                        && parts.length > index && !parts[index].isEmpty()) {
                    meta.setCustomModelData(Integer.parseInt(parts[index++]));
                }

                if (meta instanceof PotionMeta && parts.length > index) {
                    deserializePotionEffects((PotionMeta) meta, Arrays.copyOfRange(parts, index, parts.length));
                }

                item.setItemMeta(meta);
            }

            return Optional.of(item);
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "An error occurred when parsing the %s string into the ItemStack item, the reason is %s.", data, e.getMessage());
            return Optional.empty();
        }
    }

    public static void deserializePotionEffects(PotionMeta meta, String[] effectData) {
        for (String effectPart : String.join(DELIMITER, effectData).split(EFFECT_DELIMITER)) {
            if (effectPart.isEmpty()) {
                ConsoleLogger.warn("baselibrary", "Couldn't apply result of string parsing to PotionMeta (effectPart is empty)");
                continue;
            }

            String[] effectParts = effectPart.split(DELIMITER);
            if (effectParts.length < 3) {
                ConsoleLogger.warn("baselibrary", "Couldn't apply result of %s string parsing to PotionMeta (effectPart.split(%s) < 3)", effectPart, DELIMITER);
                continue;
            }

            try {
                PotionEffectType type = PotionEffectType.getByName(effectParts[0]);
                int duration = Integer.parseInt(effectParts[1]);
                int amplifier = Integer.parseInt(effectParts[2]);
                meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
            } catch (Exception e) {
                ConsoleLogger.error("baselibrary", "An error occurred when applying the result of parsing the %s string to PotionMeta, the reason is %s.", effectParts.toString(), e.getMessage());
            }
        }
    }
}
