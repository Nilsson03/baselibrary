package ru.nilsson03.library.text.api;

import net.md_5.bungee.api.ChatColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniversalTextApi {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern MINI_MESSAGE_PATTERN =
            Pattern.compile("<[!?/]?[a-zA-Z][^<>]*>");
    private static final Pattern LEGACY_HEX_PATTERN =
            Pattern.compile("(?i)§x(?:§[0-9A-F]){6}");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('§')
                    .hexColors()
                    .useUnusualXRepeatedCharacterHexFormat()
                    .build();

    public static String colorize(String text) {
        if (text == null) return null;
        String result = text;
        if (MINI_MESSAGE_PATTERN.matcher(text).find()) {
            try {
                Component component = MINI_MESSAGE.deserialize(text);
                result = LEGACY_SERIALIZER.serialize(component);
            } catch (RuntimeException ignored) {
                result = text;
            }
        }
        result = ChatColor.translateAlternateColorCodes('&', result);
        if (ServerVersionUtils.getServerVersion().isNewerOrEqual(ServerVersion.v1_16)) {
            result = translateHexColors(result);
        }
        return result;
    }

    private static String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer,
                    ChatColor.of("#" + matcher.group(1)).toString());
        }
        return matcher.appendTail(buffer).toString();
    }

    public static String decolorize(String text) {
        return ChatColor.stripColor(text);
    }

    /**
     * Reduces the number of legacy RGB color changes while preserving the complete visible text.
     * This is useful for entity custom names, where a MiniMessage gradient can exceed the legacy
     * string length limit because every character normally receives a 14-character RGB prefix.
     */
    public static String compactLegacyHexColors(String text, int maxLength) {
        if (text == null || text.length() <= maxLength || maxLength <= 0) return text;

        Matcher matcher = LEGACY_HEX_PATTERN.matcher(text);
        List<int[]> ranges = new ArrayList<>();
        while (matcher.find()) {
            ranges.add(new int[]{matcher.start(), matcher.end()});
        }
        if (ranges.isEmpty()) return text;

        int colorCodeLength = ranges.get(0)[1] - ranges.get(0)[0];
        int textWithoutHexColors = text.length() - ranges.size() * colorCodeLength;
        int colorsToKeep = Math.max(1, (maxLength - textWithoutHexColors) / colorCodeLength);
        if (colorsToKeep >= ranges.size()) return text;

        boolean[] keep = new boolean[ranges.size()];
        if (colorsToKeep == 1) {
            keep[0] = true;
        } else {
            for (int i = 0; i < colorsToKeep; i++) {
                int index = Math.round(i * (ranges.size() - 1f) / (colorsToKeep - 1f));
                keep[index] = true;
            }
        }

        StringBuilder compacted = new StringBuilder(Math.min(text.length(), maxLength));
        int previousEnd = 0;
        for (int i = 0; i < ranges.size(); i++) {
            int[] range = ranges.get(i);
            compacted.append(text, previousEnd, range[0]);
            if (keep[i]) compacted.append(text, range[0], range[1]);
            previousEnd = range[1];
        }
        compacted.append(text, previousEnd, text.length());
        return compacted.toString();
    }

    public static List<String> colorize(List<String> lines) {
        lines.replaceAll(UniversalTextApi::colorize);
        return lines;
    }

    public static String replacePlaceholders(String text, ReplaceData... replaceData) {
        String result = text;
        for (ReplaceData replace : replaceData) {
            if (replace != null && replace.getKey() != null) {
                result = result.replace(
                        replace.getKey(),
                        ChatColor.translateAlternateColorCodes('&',
                                String.valueOf(replace.getObject()))
                );
            }
        }
        return result;
    }

    public static List<String> replacePlaceholders(List<String> lore, ReplaceData... replaceData) {
        List<String> result = new ArrayList<>();
        for (String str : lore) {
            String replaced = replacePlaceholders(str, replaceData);
            replaced = replaced.replace("[", "").replace("]", "");
            String[] lines = replaced.split("\n");
            result.addAll(Arrays.asList(lines));
        }
        return result;
    }

    public static List<String> getColoredSelectLore(List<String> switchables, 
    int selectedIndex,
    String activeSymbol,
    String inactiveSymbol) {
        List<String> coloredLore = new ArrayList<>();
        for (int i = 0; i < switchables.size(); i++) {
            if (i == selectedIndex) {
                coloredLore.add(activeSymbol + " " + switchables.get(i));
            }
            else {
                coloredLore.add(inactiveSymbol + " " + switchables.get(i));
            }
        }
        return coloredLore;
    }
}
