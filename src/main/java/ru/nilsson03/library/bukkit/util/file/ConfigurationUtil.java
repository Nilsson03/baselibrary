package ru.nilsson03.library.bukkit.util.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;

public class ConfigurationUtil {

    public static FileConfiguration load(NPlugin plugin, File directory, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");
        validateFileName(fileName);

        File configFile = new File(directory, fileName);

        if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + configFile.getParent());
        }

        if (!configFile.exists() || configFile.length() == 0) {
            try (InputStream input = plugin.getResource(fileName)) {
                if (input != null) {
                    Files.copy(input, configFile.toPath());
                    ConsoleLogger.info(plugin, "Successfully copied default config: %s", configFile.getPath());
                } else {
                    if (configFile.createNewFile()) {
                        ConsoleLogger.info(plugin, "Created EMPTY config file: %s", configFile.getPath());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to initialize config: " + fileName, e);
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static Set<FileConfiguration> load(NPlugin plugin, File dataFolder, String... fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(dataFolder, "dataFolder cannot be null");

        Set<FileConfiguration> fileConfigurations = new HashSet<>();

        for (String string : fileName) {
            fileConfigurations.add(load(plugin, dataFolder, string));
        }

        return fileConfigurations;
    }

    public static FileConfiguration load(NPlugin plugin, String fileName) {
        return load(plugin, plugin.getDataFolder(), fileName);
    }

    public static FileConfiguration reload(File configFile) {
        Objects.requireNonNull(configFile, "configFile cannot be null");

        if (!configFile.exists()) {
            throw new IllegalStateException("Config file does not exist: " + configFile.getPath());
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static void save(FileConfiguration config, File configFile) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(configFile, "configFile cannot be null");

        try {
            config.save(configFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + configFile.getPath(), e);
        }
    }

    public static void save(FileConfiguration config, File directory, String fileName) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");
        validateFileName(fileName);

        File configFile = new File(directory, fileName);
        save(config, configFile);
    }

    public static void remove(FileConfiguration config, String path) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(path, "path cannot be null");
        config.set(path, null);
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be null or empty");
        }
    }
}
