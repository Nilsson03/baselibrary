package ru.nilsson03.library.bukkit.file;

import com.google.common.base.Preconditions;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class FileRepository {
    private static final Map<NPlugin, FileRepository> initializationMap = new ConcurrentHashMap<>();
    private final NPlugin plugin;
    private final Map<String, BukkitDirectory> directories;
    private final List<String> excludedFilesFromParsing;
    private final List<String> excludedDirectoryFromParsing;

    {
        directories = new ConcurrentHashMap<>();
        excludedFilesFromParsing = new CopyOnWriteArrayList<>();
        excludedDirectoryFromParsing = new CopyOnWriteArrayList<>();
    }

    public static FileRepository of(NPlugin plugin) {
        return initializationMap.getOrDefault(plugin, new FileRepository(plugin));
    }

    public FileRepository(NPlugin plugin) {
        if (initializationMap.containsKey(plugin)) {
            ConsoleLogger.debug(plugin, "FileRepository already exists for %s", plugin.getName());
            throw new IllegalStateException("FileRepository already exists for " + plugin.getName());
        }
        this.plugin = plugin;
        initializationMap.put(plugin, this);
    }

    public Optional<BukkitDirectory> getDirectoryOrLoad(String directoryName) {
        if (directories.containsKey(directoryName)) {
            return Optional.of(directories.get(directoryName));
        }
        return load(directoryName);
    }

    public Optional<BukkitDirectory> load(String directory) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        if (shouldSkipDirectory(directory)) {
            ConsoleLogger.debug(plugin, "Skipping load of %s directory", directory);
            return Optional.empty();
        }

        if (directories.containsKey(directory)) {
            return Optional.of(directories.get(directory));
        }

        String normalizePath = plugin.getDataFolder().getPath() + File.separator + directory;
        File directoryFile;
        try {
            directoryFile = FileHelper.getOrCreateDirectory(normalizePath);
        } catch (IOException e) {
            ConsoleLogger.error(plugin, "Couldn't create directory %s: %s", normalizePath, e.getMessage());
            return Optional.empty();
        }

        Map<String, BukkitConfig> files = loadFiles(directoryFile);
        BukkitDirectory bukkitDirectory = BukkitDirectory.of(plugin, directoryFile, files);
        directories.put(directory, bukkitDirectory);
        return Optional.of(bukkitDirectory);
    }

    private Map<String, BukkitConfig> loadFiles(File dir) {
        Map<String, BukkitConfig> result = new HashMap<>();
        File[] directoryFiles = dir.listFiles();
        if (directoryFiles == null) return result;

        for (File file : directoryFiles) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String name = file.getName();
                if (shouldSkipContentParsing(file)) {
                    ConsoleLogger.debug(plugin, "Skipping load of %s", name);
                    continue;
                }
                if (isFileExistsInAnyDirectory(name)) {
                    ConsoleLogger.warn(plugin, "Duplicate config %s found", name);
                    continue;
                }
                result.put(name, new BukkitConfig(plugin, dir, name));
            }
        }
        return result;
    }

    public Optional<BukkitConfig> create(BukkitDirectory directory, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(fileName != null && !fileName.isEmpty(), "fileName cannot be empty");

        String dirPath = directory.getPath();
        if (!directories.containsKey(dirPath)) {
            ConsoleLogger.warn(plugin, "Directory %s not found", dirPath);
            return Optional.empty();
        }

        if (isFileExistsInAnyDirectory(fileName)) {
            ConsoleLogger.warn(plugin, "File %s already exists", fileName);
            return Optional.empty();
        }

        BukkitConfig bukkitConfig = new BukkitConfig(plugin, directory.getFile(), fileName);
        try {
            if (bukkitConfig.getFile().exists()) {
                ConsoleLogger.warn(plugin, "File %s exists on disk", fileName);
                return Optional.empty();
            }

            directory.addNewConfig(bukkitConfig);
            bukkitConfig.saveConfiguration();
            return Optional.of(bukkitConfig);
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Failed to create config %s: %s", fileName, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean shouldSkipContentParsing(File file) {
        return excludedFilesFromParsing.contains(file.getName());
    }

    private boolean shouldSkipDirectory(String directoryName) {
        return excludedDirectoryFromParsing.contains(directoryName);
    }

    private boolean isFileExistsInAnyDirectory(String fileName) {
        return directories.values().stream()
                .anyMatch(dir -> dir.containsFileWithName(fileName));
    }

    public void addExcludedFiles(String... fileNames) {
        excludedFilesFromParsing.addAll(Arrays.asList(fileNames));
    }

    public void addExcludedDirectories(String... directoryNames) {
        excludedDirectoryFromParsing.addAll(Arrays.asList(directoryNames));
    }

    public Optional<BukkitConfig> getByName(BukkitDirectory directory, String fileName) {
        String dirPath = directory.getPath();
        if (!directories.containsKey(dirPath)) {
            ConsoleLogger.debug(plugin, "Directory %s not in cache", dirPath);
            return Optional.empty();
        }
        return directories.get(dirPath).getConfig(fileName);
    }
}