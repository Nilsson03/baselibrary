package ru.nilsson03.library.bukkit.file;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.util.*;

public class BukkitDirectory {

    private final String directoryName;
    private final Map<String, BukkitConfig> cached = new HashMap<>();

    private final NPlugin plugin;
    private final FileRepository pluginFileRepository;

    protected BukkitDirectory(NPlugin plugin, File directory) {
        this.plugin = plugin;
        this.pluginFileRepository = plugin.fileRepository();
        this.directoryName = directory.getName();

        Objects.requireNonNull(pluginFileRepository, "fileRepository cant be null, class BukkitDirectory, method of(NPlugin plugin, File directory)");

        List<BukkitConfig> cache = pluginFileRepository.getAllFromDirectory(directory.getAbsolutePath());
        cache.forEach( (bukkitConfig -> cached.put(bukkitConfig.getName(), bukkitConfig)));
    }

    public static BukkitDirectory of(NPlugin plugin, File directory) throws ExceptionInInitializerError, NullPointerException {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(directory != null && directory.isDirectory(), "directory cannot be null or file");

        return new BukkitDirectory(plugin, directory);
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
}
