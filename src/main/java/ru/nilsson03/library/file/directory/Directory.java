package ru.nilsson03.library.file.directory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.file.config.YamlConfig;

public class Directory {

    private final String directoryName;
    private final Path path;
    private final Map<String, YamlConfig> cached = new HashMap<>();
    private final NPlugin plugin;

    protected Directory(NPlugin plugin, Path directory, Map<String, YamlConfig> listOfFiles) {
        this.plugin = plugin;
        this.path = directory;
        this.directoryName = directory.getFileName().toString();
        cached.putAll(listOfFiles);
    }

    public static Directory of(NPlugin plugin, Path directory, Map<String, YamlConfig> listOfFiles) throws ExceptionInInitializerError, NullPointerException {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");
        
        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("Path must be a directory");
        }

        return new Directory(plugin, directory, listOfFiles);
    }

    private void removeAndDeleteConfig(String fileName) {
        if (!containsFileWithName(fileName))
            return;

        this.cached.remove(fileName);
    }

    public void removeAndDeleteConfig(YamlConfig config) {
        String fileName = config.getFile().getName();
        removeAndDeleteConfig(fileName);
        config.getCache().close();
    }

    public void addNewConfig(YamlConfig config) {
        Objects.requireNonNull(config, "config cannot be null");

        String name = config.getFile().getName();

        if (containsFileWithName(name)) {
            ConsoleLogger.debug(plugin, "The config file %s is already contains in directory", name);
            return;
        }

        this.cached.put(name, config);
    }

    @Nullable
    public YamlConfig addNewConfig(String fileName) {
        Objects.requireNonNull(fileName, "fileName cannot be null");

        if (!containsFileWithName(fileName)) {
            YamlConfig config = new YamlConfig(plugin, this.path, fileName);
            this.cached.put(fileName, config);
            ConsoleLogger.debug(plugin, "The config file %s is added to directory", fileName);
            return config;
        } else {
            ConsoleLogger.debug(plugin, "The config file %s is already contains in directory", fileName);
            return null;
        }
    }

    public boolean containsFileWithName(String fileName) {
        if (cached.isEmpty()) {
            return false;
        }

        return cached.containsKey(fileName);
    }

    public void save(String fileName) {
        if (!containsFileWithName(fileName)) {
            ConsoleLogger.warn(plugin, "The %s configuration file was not found in the %s directory!", fileName, directoryName);
            return;
        }

        YamlConfig config = getYamlConfig(fileName);
        if (config != null) {
            config.getCache().save();
        }
    }

    public void reload(String fileName) {
        if (!containsFileWithName(fileName)) {
            ConsoleLogger.warn(plugin, "The %s configuration file was not found in the %s directory!", fileName, directoryName);
            return;
        }

        YamlConfig config = getYamlConfig(fileName);
        if (config != null) {
            config.getCache().reload();
        }
    }

    public void reloadAll() {
        if (cached.isEmpty()) {
            ConsoleLogger.debug(plugin, "No cached files to reload in directory: %s", directoryName);
            return;
        }

        int totalFiles = cached.size();
        int reloadedFiles = 0;
        int failedFiles = 0;

        for (YamlConfig config : cached.values()) {
            try {
                config.getCache().reload();
                reloadedFiles++;
            } catch (Exception e) {
                failedFiles++;
                ConsoleLogger.warn(plugin, "Failed to reload config %s in directory %s: %s",
                                 config.getFile().getName(), directoryName, e.getMessage());
            }
        }

        ConsoleLogger.info(plugin, "Reloaded %d/%d config files in directory %s (failed: %d)",
                         reloadedFiles, totalFiles, directoryName, failedFiles);
    }

    @Nullable
    public YamlConfig getYamlConfig(String fileName) {
        try {
            return cached.get(fileName);
        } catch (NullPointerException e) {
            ConsoleLogger.warn(plugin, "The config file %s was not found in the %s directory!", fileName, directoryName);
            return null;
        }
    }

    public void addAll(Map<String, YamlConfig> files) {
        cached.clear();
        cached.putAll(files);
    }

    public void saveAll() {
        cached.values().forEach(config -> config.getCache().save());
    }

    public List<YamlConfig> getCached() {
        return new ArrayList<>(cached.values());
    }

    public String getPath() {
        return path.toString();
    }

    public Path getPathAsPath() {
        return path;
    }
}
