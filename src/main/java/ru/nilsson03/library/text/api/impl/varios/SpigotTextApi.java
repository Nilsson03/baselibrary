package ru.nilsson03.library.text.api.impl.varios;

import net.md_5.bungee.api.ChatColor;
import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.text.api.TextApi;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpigotTextApi implements TextApi {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");

    @Override
    public String colorize(String text) {
        if (text == null) return null;
        String result = ChatColor.translateAlternateColorCodes('&', text);
        if (ServerVersionUtils.getServerVersion().isNewerOrEqual(ServerVersion.v1_16)) {
            result = translateHexColors(result);
        }
        return result;
    }

    @Override
    public String decolorize(String text) {
        return ChatColor.stripColor(text);
    }

    @Override
    public List<String> colorize(List<String> lines) {
        lines.replaceAll(this::colorize);
        return lines;
    }

    private String translateHexColors(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer,
                    ChatColor.of("#" + matcher.group(1)).toString());
        }
        return matcher.appendTail(buffer).toString();
    }

    @Override
    public String replacePlaceholders(String text, ReplaceData... replaces) {
        String result = text;
        for (ReplaceData replace : replaces) {
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

    @Override
    public List<String> replacePlaceholders(List<String> lines, ReplaceData... replaces) {
        lines.replaceAll(line -> replacePlaceholders(line, replaces));
        return lines;
    }
}
