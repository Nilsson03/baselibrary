package ru.nilsson03.library.bukkit.util.log;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Статический логгер с ручной регистрацией плагинов
 */
public final class ConsoleLogger {

    private static final String mainPrefixColor = "§6";
    private static final Map<String, String> PLUGIN_PREFIXES = new HashMap<>();
    private static final Map<String, Logger> LOGGERS = new HashMap<>();
    private static final String DEFAULT_PREFIX = "§6System";

    private static boolean debug = true;

    static {
        // Инициализация для системных сообщений
        PLUGIN_PREFIXES.put("system", DEFAULT_PREFIX);
    }

    public static void register(JavaPlugin plugin) {
        PLUGIN_PREFIXES.put(plugin.getName().toLowerCase(), mainPrefixColor + plugin.getName());
        Logger logger = new Logger(plugin);
        logger.initialize();
        LOGGERS.put(plugin.getName(), logger);
    }

    public static void unregister(JavaPlugin plugin) {
        PLUGIN_PREFIXES.remove(plugin.getName().toLowerCase());
        Logger logger = LOGGERS.getOrDefault(plugin.getName(), null);
        if (logger != null) {
            logger.close();
        }
    }

    public static void info(JavaPlugin plugin, String format, Object... args) {
        log(plugin, LogLevel.INFO, format, args);
    }

    public static void warn(JavaPlugin plugin, String format, Object... args) {
        log(plugin, LogLevel.WARNING, format, args);
    }

    public static void error(JavaPlugin plugin, String format, Object... args) {
        log(plugin, LogLevel.ERROR, format, args);
    }

    public static void success(JavaPlugin plugin, String format, Object... args) {
        log(plugin, LogLevel.SUCCESS, format, args);
    }

    public static void debug(JavaPlugin plugin, String format, Object... args) {
        if (debug) {
            log(plugin, LogLevel.DEBUG, format, args);
        }
    }

    private static void log(JavaPlugin plugin, LogLevel level, String format, Object... args) {
        log(plugin.getName(), level, format, args);
    }

    // Альтернативные методы без JavaPlugin
    public static void info(String pluginName, String format, Object... args) {
        log(pluginName, LogLevel.INFO, format, args);
    }

    public static void warn(String pluginName, String format, Object... args) {
        log(pluginName, LogLevel.WARNING, format, args);
    }

    public static void error(String pluginName, String format, Object... args) {
        log(pluginName, LogLevel.ERROR, format, args);
    }

    public static void success(String pluginName, String format, Object... args) {
        log(pluginName, LogLevel.SUCCESS, format, args);
    }

    public static void debug(String pluginName, String format, Object... args) {
        log(pluginName, LogLevel.DEBUG, format, args);
    }

    private static void log(String pluginName, LogLevel level, String format, Object... args) {
        String prefix = PLUGIN_PREFIXES.getOrDefault(
                pluginName.toLowerCase(),
                DEFAULT_PREFIX
        );

        String icon = LogIcon.valueOf(level.name()).getIcon();

        String message = String.format(format, args);
        String formatted = String.format("%s §8| %s §7%s",
                prefix, icon, message);

        Bukkit.getConsoleSender().sendMessage(formatted);

        if (level == LogLevel.ERROR || level == LogLevel.WARNING || level == LogLevel.DEBUG) {
            if (LOGGERS.containsKey(pluginName)) {
                Logger logger = LOGGERS.getOrDefault(pluginName, null);
                if (logger == null) {
                    ConsoleLogger.warn(pluginName, "Couldn't get an instance of the Logger class to write a message to the event log.");
                    return;
                }

                logger.log(level, format, args);
            }
        }
    }

    public static void debug(boolean b) {
        debug = b;
    }
}
