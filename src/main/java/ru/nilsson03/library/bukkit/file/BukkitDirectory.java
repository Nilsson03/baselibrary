package ru.nilsson03.library.bukkit.file;

import com.google.common.base.Preconditions;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.util.*;

public class BukkitDirectory {

    private final String directoryName;
    private final Map<String, BukkitConfig> cached = new HashMap<>();

    private final NPlugin plugin;

    protected BukkitDirectory(NPlugin plugin, File directory, Map<String, BukkitConfig> listOfFiles) {
        this.plugin = plugin;
        this.directoryName = directory.getName();
        cached.putAll(listOfFiles);
    }

    public static BukkitDirectory of(NPlugin plugin, File directory, Map<String, BukkitConfig> listOfFiles) throws ExceptionInInitializerError, NullPointerException {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(directory != null && directory.isDirectory(), "directory cannot be null or file");

        return new BukkitDirectory(plugin, directory, listOfFiles);
    }

    public void removeConfig(String fileName) {
        if (!containsFileWithName(fileName))
            return;

        this.cached.remove(fileName);
    }

    public void removeConfig(BukkitConfig config) {
        removeConfig(config.getName());
    }

    public void addNewConfig(BukkitConfig config) {
        Objects.requireNonNull(config, "config cannot be null");

        String name = config.getName();

        if (containsFileWithName(name)) {
            ConsoleLogger.debug(plugin, "The config file %s is already contains in directory", name);
            return;
        }

        // todo сделать проверку на содержании в других категориях в (fileRepo)

        this.cached.put(name, config);
    }

    public boolean containsFileWithName(String fileName) {
        if (cached.isEmpty()) {
            ConsoleLogger.warn(plugin, "The cached contents of the %s directory are empty!", directoryName);
            return false;
        }

        return cached.containsKey(fileName);
    }

    public void save(String fileName) {
        if (!containsFileWithName(fileName)) {
            ConsoleLogger.warn(plugin, "The %s configuration file was not found in the %s directory!", fileName, directoryName);
            return;
        }

        Optional<BukkitConfig> configOptional = getConfig(fileName);
        BukkitConfig config = configOptional.get();
        config.saveConfiguration();
    }

    public void reload(String fileName) {
        if (!containsFileWithName(fileName)) {
            ConsoleLogger.warn(plugin, "The %s configuration file was not found in the %s directory!", fileName, directoryName);
            return;
        }

        Optional<BukkitConfig> configOptional = getConfig(fileName);
        BukkitConfig bukkitConfig = configOptional.get();
        FileConfiguration configuration = FileHelper.reloadFile(bukkitConfig.getPlugin(), bukkitConfig.getFileConfiguration());
        bukkitConfig.updateFileConfiguration(configuration);
    }

    public Optional<BukkitConfig> getConfig(String fileName) {
        return Optional.ofNullable(this.cached.get(fileName));
    }

    public List<BukkitConfig> getCached() {
        return new ArrayList<>(cached.values());
    }
}
