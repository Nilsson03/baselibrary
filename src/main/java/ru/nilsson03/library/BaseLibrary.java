package ru.nilsson03.library;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ru.nilsson03.library.alt.cache.CacheManager;
import ru.nilsson03.library.bukkit.integration.Integration;
import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class BaseLibrary extends JavaPlugin {

    private static BaseLibrary instance;

    private Integration integration;
    private CacheManager cacheManager;

    @Override
    public void onEnable() {
        instance = this;

        ServerVersion currentVersion = ServerVersionUtils.getServerVersion();

        if (currentVersion.isOlderThan(ServerVersion.v1_16)) {
            ConsoleLogger.error(this, "The library does not support versions lower than 1.16. Please install version 1.16 or higher and try again.");
            onDisable();
            return;
        }

        ConsoleLogger.register(this);

        integration = new Integration(this);

        ConsoleLogger.info(this, "BaseLibrary version %s has been successfully enabled.", getDescription().getVersion());
        ConsoleLogger.info(this, "The server version used is %s on %s", currentVersion.name(), Bukkit.getName());
        ConsoleLogger.info(this, "Thank you for using my plugins, you can find all the detailed information on my discord server - %s", "https://dsc.gg/velialcult");

    }

    @Override
    public void onDisable() {
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
        ConsoleLogger.unregister(this);
    }

    public static BaseLibrary getInstance() {
        return instance;
    }

    public Integration getIntegration() {
        return integration;
    }

    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
