package ru.nilsson03.library.bukkit.util;

import org.bukkit.Bukkit;

public class ServerVersionUtils {

    public static final String NMS_VERSION;

    public static final String BUKKIT_VERSION;

    public static final ServerVersion CURRENT_VERSION;

    public static final String CORE;

    static {
        NMS_VERSION = getNMSVersion();
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

    private static String getNMSVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        String[] packageParts = packageName.split("\\.");

        if (packageParts.length > 3) {
            return packageParts[3];
        }

        String serverVersion = Bukkit.getServer().getVersion();
        String mcVersion;
        if (serverVersion.contains("(MC: ")) {
            mcVersion = serverVersion.split("\\(MC: ")[1].split("\\)")[0];
        } else if (serverVersion.contains("-")) {
            mcVersion = serverVersion.split("-")[1].split(" ")[0];
        } else {
            mcVersion = "1.21.5"; // fallback
        }

        String[] versionParts = mcVersion.split("\\.");
        String major = versionParts[0];
        String minor = versionParts[1];

        return "v" + major + "_" + minor + "_R1";
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
