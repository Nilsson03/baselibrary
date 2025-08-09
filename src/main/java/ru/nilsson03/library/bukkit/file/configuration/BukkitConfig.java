package ru.nilsson03.library.bukkit.file.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.FileHelper;
import ru.nilsson03.library.bukkit.file.configuration.impl.BukkitConfigurationImpl;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Nicholas Alexandrov 18.06.2023
 */
public class BukkitConfig {

    private final String name;
    private final File file;
    private BukkitConfigurationImpl configuration;
    private FileConfiguration fileConfiguration;
    private final NPlugin plugin;

    public BukkitConfig(NPlugin plugin, File directory, String fileName) {
        if (plugin == null) {
            ConsoleLogger.debug("baselibrary", "Plugin cannot be null, class %s, plugin", getClass().getName());
            throw new IllegalArgumentException("plugin cannot be null, class " + getClass().getName());
        }

        validateFileName(fileName);

        if (fileName == null || fileName.isEmpty()) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }
        this.plugin = plugin;
        this.file = new File(directory, fileName);
        this.name = fileName;

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + directory);
        }

        try {
            this.fileConfiguration = FileHelper.loadConfiguration(plugin, directory, fileName);
        } catch (Exception e) {
            ConsoleLogger.debug(plugin, "Failed to load configuration file, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalStateException("Failed to load configuration file, class " + getClass().getName(), e);
        }
        this.configuration = new BukkitConfigurationImpl(plugin, fileName, fileConfiguration);
    }

    public NPlugin getPlugin() {
        return plugin;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public BukkitConfigurationImpl getBukkitConfiguration() {
        if (configuration == null) {
            configuration = new BukkitConfigurationImpl(plugin, file.getName(), fileConfiguration);
        }
        return configuration;
    }

    public void reloadConfiguration() {
        var updatedConfiguration = FileHelper.reloadFile(getPlugin(), fileConfiguration);
        updateFileConfiguration(updatedConfiguration);
    }

    public void updateFileConfiguration(FileConfiguration fileConfiguration) {
        Objects.requireNonNull(fileConfiguration, "configuration cannot be null");
        this.fileConfiguration = fileConfiguration;
        configuration.load();
        ConsoleLogger.debug(plugin, "File %s has been loaded (Class %s).",
                file.getName(),
                this.getClass().getName());
    }

    public void saveConfiguration() {
        try {
            FileHelper.saveFile(fileConfiguration, plugin.getDataFolder(), name);
            ConsoleLogger.debug(plugin, "File %s successfully saved (Class %s).",
                    name,
                    this.getClass().getName());
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to save configuration " + name + " for plugin " + plugin.getName() + " error: " + exception.getMessage());
            throw new RuntimeException("Failed to save configuration " + name + " for plugin " + plugin.getName(), exception);
        }
    }

    public FileConfiguration loadConfiguration() {
        try {
            fileConfiguration = FileHelper.loadConfiguration(plugin, name);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to load configuration " + name + " for plugin " + plugin.getName() + " error: " + exception.getMessage());
            throw new RuntimeException("Failed to load configuration " + name + " for plugin " + plugin.getName(), exception);
        }

        return fileConfiguration;
    }

    public FileConfiguration getFileConfiguration() {
        if (fileConfiguration == null) {
            fileConfiguration = loadConfiguration();
        }
        return fileConfiguration;
    }


    public InputStream getInputStream() throws FileNotFoundException, SecurityException {
        return new FileInputStream(file);
    }

    public ConfigOperations operations() throws NullPointerException {
        Objects.requireNonNull(configuration, "Configuration cannot be null");

        return getBukkitConfiguration().operations();
    }

    public void delete() {
        boolean delete = file.delete();

        if (delete)
            ConsoleLogger.info(plugin, "Deleted configuration %s for plugin %s.", getFile().getName(), plugin.getName());
        else
            ConsoleLogger.warn(plugin, "Failed to delete configuration %s for plugin %s.", getFile().getName(), plugin.getName());
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.startsWith("/")) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }
    }

    @Deprecated
    public FileConfiguration getConfiguration() {
        return fileConfiguration;
    }
}
