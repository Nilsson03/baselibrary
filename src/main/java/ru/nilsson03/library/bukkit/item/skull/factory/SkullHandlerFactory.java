package ru.nilsson03.library.bukkit.item.skull.factory;

import org.bukkit.Bukkit;
import ru.nilsson03.library.bukkit.item.skull.SkullTextureHandler;
import ru.nilsson03.library.bukkit.item.skull.impl.universal.PaperSkullTextureHandler;
import ru.nilsson03.library.bukkit.item.skull.impl.versioned.SpigotSkullTextureHandler_v1_19;
import ru.nilsson03.library.bukkit.item.skull.impl.versioned.SpigotSkullTextureHandler_v1_20;
import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class SkullHandlerFactory {

    public static SkullTextureHandler createHandler() {
        String serverType = Bukkit.getServer().getName();
        String core = ServerVersionUtils.CORE;
        ServerVersion version = ServerVersionUtils.getServerVersion();

        if (core.equalsIgnoreCase("Paper") || core.equalsIgnoreCase("Purpur")) {
            return new PaperSkullTextureHandler();
        } else if (core.equalsIgnoreCase("Spigot")) {
            if (version.isOlderOrEqual(ServerVersion.v1_19)) {
                return new SpigotSkullTextureHandler_v1_19();
            } else if (version.isNewerOrEqual(ServerVersion.v1_20)) {
                return new SpigotSkullTextureHandler_v1_20();
            }
        }
        ConsoleLogger.error("baselibrary", "Unsupported server version %s or core %s. Class %s", version.name(), core, "SkullHandlerFactory");
        throw new RuntimeException("Unsupported server version: " + version.name());
    }
}
