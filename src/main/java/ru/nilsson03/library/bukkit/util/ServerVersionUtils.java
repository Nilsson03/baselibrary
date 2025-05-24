package ru.nilsson03.library.bukkit.util;

import org.bukkit.Bukkit;

public class ServerVersionUtils {

    public static final String NMS_VERSION;

    public static final String BUKKIT_VERSION;

    public static final ServerVersion CURRENT_VERSION;

    public static final String CORE;

    static {
        NMS_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        BUKKIT_VERSION = Bukkit.getBukkitVersion().split("-")[0];
        CURRENT_VERSION = detectServerVersion();
        CORE = Bukkit.getName();
    }

    /**
     * Определяет текущую версию сервера.
     * @return ServerVersion текущей версии
     */
    public static ServerVersion getServerVersion() {
        return CURRENT_VERSION;
    }

    /**
     * Определяет версию сервера при инициализации.
     */
    private static ServerVersion detectServerVersion() {
        String version = BUKKIT_VERSION;
        for (ServerVersion value : ServerVersion.values()) {
            if (version.startsWith(value.getVersionString())) {
                return value;
            }
        }
        return ServerVersion.UNKNOWN;
    }

    /**
     * Проверяет, находится ли текущая версия сервера в указанном диапазоне (включительно).
     *
     * @param min минимальная версия диапазона
     * @param max максимальная версия диапазона
     * @return true если текущая версия >= min и <= max
     * @throws IllegalArgumentException если min или max равны null, либо min > max
     */
    public static boolean isVersionBetween(ServerVersion min, ServerVersion max) {
        return CURRENT_VERSION.getWeight() >= min.getWeight() && CURRENT_VERSION.getWeight() <= max.getWeight();
    }

    /**
     * Проверяет, является ли текущая версия сервера новее указанной.
     *
     * @param serverVersion версия для сравнения
     * @return true если текущая версия новее указанной
     */
    public static boolean isVersionNewerThan(ServerVersion serverVersion) {
        return CURRENT_VERSION.isNewerThan(serverVersion);
    }

    /**
     * Проверяет, является ли текущая версия сервера старее указанной.
     *
     * @param serverVersion версия для сравнения
     * @return true если текущая версия старее указанной
     */
    public static boolean isVersionOlderThan(ServerVersion serverVersion) {
        return CURRENT_VERSION.isOlderThan(serverVersion);
    }
}
