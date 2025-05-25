package ru.nilsson03.library.bukkit.file.configuration.impl;

import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfiguration;
import ru.nilsson03.library.bukkit.file.configuration.ConfigOperations;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.HashMap;
import java.util.Map;

public class BukkitConfigurationImpl implements BukkitConfiguration {

    private final NPlugin plugin;
    private final String fileName;
    private final Map<String, String> fileContent;
    private final FileConfiguration bukkitConfiguration;
    private final ConfigOperations fileOperations;

    public BukkitConfigurationImpl(NPlugin plugin, String fileName, FileConfiguration bundleConfiguration) throws NullPointerException {
        if (plugin == null) {
            ConsoleLogger.debug("baselibrary", "Plugin cannot be null, class %s, plugin", getClass().getName());
            throw new IllegalArgumentException("plugin cannot be null, class " + getClass().getName());
        }
        this.plugin = plugin;
        if (fileName == null || fileName.isEmpty()) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }
        this.fileName = fileName;
        this.fileContent = new HashMap<>();
        if (bundleConfiguration == null) {
            ConsoleLogger.debug(plugin, "File configuration cannot be null, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File configuration cannot be null, class " + getClass().getName());
        }
        this.bukkitConfiguration = bundleConfiguration;

        try {
            load();
            this.fileOperations = new ConfigOperations(fileContent);
        } catch (ClassCastException e) {
            ConsoleLogger.warn(plugin,
                    "YAML configuration file %s contains format errors: %s",
                    fileName, e.getMessage());
            throw new IllegalStateException("Failed to load configuration", e);
        }
    }

    public FileConfiguration getBukkitConfiguration() {
        return bukkitConfiguration;
    }

    public Map<String, String> getFileContent() {
        return fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public ConfigOperations getFileOperations() {
        return fileOperations;
    }

    public NPlugin getPlugin() {
        return plugin;
    }
}
