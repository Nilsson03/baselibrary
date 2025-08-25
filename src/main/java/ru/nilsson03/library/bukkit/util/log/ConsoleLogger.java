package ru.nilsson03.library.bukkit.util.log;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nilsson03.library.BaseLibrary;

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

    static {
        // Инициализация для системных сообщений
        PLUGIN_PREFIXES.put("system", DEFAULT_PREFIX);
    }

    public static void register(JavaPlugin plugin) {
        String lowerPluginName = plugin.getName().toLowerCase();
        PLUGIN_PREFIXES.put(lowerPluginName, mainPrefixColor + plugin.getName());
        Logger logger = new Logger(plugin);
        logger.initialize();
        LOGGERS.put(lowerPluginName, logger);
        ConsoleLogger.info(plugin, "Success registered ConsoleLogger.");
    }

    public static void unregister(JavaPlugin plugin) {
        String lowerPluginName = plugin.getName().toLowerCase();
        PLUGIN_PREFIXES.remove(lowerPluginName);
        Logger logger = LOGGERS.getOrDefault(lowerPluginName, null);
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
        log(plugin, LogLevel.DEBUG, format, args);
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
        pluginName = pluginName.toLowerCase();
        String prefix = PLUGIN_PREFIXES.getOrDefault(
                pluginName.toLowerCase(),
                DEFAULT_PREFIX
        );

        String icon = LogIcon.valueOf(level.name()).getIcon();

        String message = String.format(format, args);
        String formatted = String.format("%s §8| %s §7%s",
                prefix, icon, message);

        if (level != LogLevel.DEBUG)
            Bukkit.getConsoleSender().sendMessage(formatted);

        if (level == LogLevel.DEBUG) {
            if (!BaseLibrary.getInstance().getConfig().getBoolean("debug"))
                return;
        }

        if (level == LogLevel.ERROR || level == LogLevel.WARNING || level == LogLevel.DEBUG) {
            if (level == LogLevel.DEBUG) {
                if (!BaseLibrary.getInstance().getConfig().getBoolean("debug"))
                    return;
                }
            if (LOGGERS.containsKey(pluginName)) {
                Logger logger = LOGGERS.get(pluginName);
                logger.log(level, format, args);
            } else {
                Bukkit.getConsoleSender().sendMessage("Cant write message to file because Logger object is null for plugin " +
                        pluginName + ".");
            }
        }
    }
}
