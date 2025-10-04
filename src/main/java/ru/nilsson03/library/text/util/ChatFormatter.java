package ru.nilsson03.library.text.util;

import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class ChatFormatter {

    private static final int CHAT_WIDTH;
    private static final int MAX_LINE_LENGTH;

    static {
        ServerVersion serverVersion = ServerVersionUtils.getServerVersion();
        if (serverVersion.isNewerOrEqual(ServerVersion.v1_17)) {
            CHAT_WIDTH = 180;
            MAX_LINE_LENGTH = 60;
            ConsoleLogger.debug("baselibrary", "Use 1.17 or never chat width (width %s, line %s, class %s).",
                    CHAT_WIDTH, MAX_LINE_LENGTH, ChatFormatter.class.getName());
        } else {
            CHAT_WIDTH = 320;
            MAX_LINE_LENGTH = 60;
            ConsoleLogger.debug("baselibrary", "Use 1.16 or older chat width (width %s, line %s, class %s).",
                    CHAT_WIDTH, MAX_LINE_LENGTH, ChatFormatter.class.getName());
        }
    }

    public static String centerText(String message) {
        if (!message.contains("{center}")) return message;

        String text = message.replace("{center}", "");

        String visibleText = text.replaceAll("§[0-9a-fk-or]", "")
                .replaceAll("&#[a-fA-F0-9]{6}", "")
                .replaceAll("§x(§[a-fA-F0-9]){6}", "");

        if (visibleText.length() > MAX_LINE_LENGTH) {
            ConsoleLogger.debug("baselibrary", "The text %s is too long, and the minimum indentation is used (length %s, max %s).",
                    visibleText, visibleText.length(), MAX_LINE_LENGTH);
            return " " + text;
        } else {
            int padding = (int) ((MAX_LINE_LENGTH - visibleText.length()) / 1.5);
            String result =  " ".repeat(Math.max(1, padding)) + text;
            ConsoleLogger.debug("baselibrary", "Alignment result %s (padding %s, length %s, max %s, chat %s, src %s).",
                    result, padding, visibleText.length(), MAX_LINE_LENGTH, CHAT_WIDTH, message);
            return result;
        }
    }
}