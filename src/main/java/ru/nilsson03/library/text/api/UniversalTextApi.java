package ru.nilsson03.library.text.api;

import net.md_5.bungee.api.ChatColor;
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

    public static String colorize(String text) {
        if (text == null) return null;
        String result = ChatColor.translateAlternateColorCodes('&', text);
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
}
