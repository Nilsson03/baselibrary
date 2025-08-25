package ru.nilsson03.library.bukkit.file;

import com.google.common.base.Preconditions;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class BukkitDirectory {

    private final String directoryName;
    private final String path;
    private final File file;
    private final Map<String, BukkitConfig> cached = new HashMap<>();

    private final NPlugin plugin;

    protected BukkitDirectory(NPlugin plugin, File directory, Map<String, BukkitConfig> listOfFiles) {
        this.plugin = plugin;
        this.path = directory.getPath();
        this.file = directory;
        this.directoryName = directory.getName();
        cached.putAll(listOfFiles);
    }

    public static BukkitDirectory of(NPlugin plugin, File directory, Map<String, BukkitConfig> listOfFiles) throws ExceptionInInitializerError, NullPointerException {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(directory != null && directory.isDirectory(), "directory cannot be null or file");

        return new BukkitDirectory(plugin, directory, listOfFiles);
    }

    private void removeAndDeleteConfig(String fileName) {
        if (!containsFileWithName(fileName))
            return;

        this.cached.remove(fileName);
    }

    public void removeAndDeleteConfig(BukkitConfig config) {
        removeAndDeleteConfig(config.getName());
        config.delete();
    }

    public void addNewConfig(BukkitConfig config) {
        Objects.requireNonNull(config, "config cannot be null");

        String name = config.getName();

        if (containsFileWithName(name)) {
            ConsoleLogger.debug(plugin, "The config file %s is already contains in directory", name);
            return;
        }

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

        BukkitConfig config = getBukkitConfig(fileName);
        if (config != null) {
            config.reloadConfiguration();
        }
    }

    /**
     * Получает BukkitConfig из кэша файлов директории,
     * затем создаёт обёртку Optional и возвращает результат.
     * @param fileName название файла из директории
     * @return файл в обёртке Optional
     *
     * @see BukkitConfig
     */
    @Deprecated(since = "1.2.2")
    public Optional<BukkitConfig> getConfig(String fileName) {
        return Optional.ofNullable(this.cached.get(fileName));
    }

    /**
     * Получает BukkitConfig из кэша файлов директории,
     * @param fileName название файла из директории
     * @return файл
     */
    @Nullable
    public BukkitConfig getBukkitConfig(String fileName) {
        try {
            return cached.get(fileName);
        } catch (NullPointerException e) {
            ConsoleLogger.warn(plugin, "The config file %s was not found in the %s directory!", fileName, directoryName);
            return null;
        }
    }

    /**
     * Перезагружает все конфигурации в кэше
     */
    public void reloadAllConfigs() {
        try {
            for (BukkitConfig config : cached.values()) {
                config.reloadConfiguration();
            }
        } catch (Exception exception) {
            ConsoleLogger.error("baselibrary", "An error %s occurred while reloading the %s directory configurations:",
                    exception.getMessage(),
                    getFile().getName());
        }
    }

    public List<BukkitConfig> getCached() {
        return new ArrayList<>(cached.values());
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }
}
