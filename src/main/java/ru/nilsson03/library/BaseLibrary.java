package ru.nilsson03.library;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import ru.nilsson03.library.bukkit.integration.Integration;
import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.bukkit.util.TranslationUtil;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.text.component.action.ClickActionRegistry;

public class BaseLibrary extends JavaPlugin {

    private static BaseLibrary instance;

    private Integration integration;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();

        ServerVersion currentVersion = ServerVersionUtils.getServerVersion();

        if (currentVersion.isOlderThan(ServerVersion.v1_16)) {
            ConsoleLogger.error(this, "The library does not support versions lower than 1.16. Please install version 1.16 or higher and try again.");
            onDisable();
            return;
        }

        boolean writeLogs = getConfig().getBoolean("writeLogs", false);
        ConsoleLogger.register(this, writeLogs);

        integration = new Integration(this);
        ClickActionRegistry.register(this);

        TranslationUtil.initialize(this);
        TranslationUtil.loadTranslations("ru");
        TranslationUtil.setDefaultLanguage("ru");

        ConsoleLogger.info(this, "BaseLibrary version %s has been successfully enabled.", getDescription().getVersion());
        ConsoleLogger.info(this, "The server version used is %s on %s", currentVersion.name(), Bukkit.getName());
        ConsoleLogger.info(this, "Thank you for using my plugins, you can find all the detailed information on my discord server - %s", "https://dsc.gg/velialcult");

    }

    @Override
    public void onDisable() {
        ConsoleLogger.unregister(this);
    }

    public static BaseLibrary getInstance() {
        return instance;
    }

    public Integration getIntegration() {
        return integration;
    }
}
