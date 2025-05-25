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

/**
 * Репозиторий для управления конфигурационными файлами плагина.
 * Обеспечивает загрузку, сохранение и перезагрузку YAML-конфигураций.
 */
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

    public void addExcludedFiles(String... fileNames) {
        excludedFilesFromParsing.addAll(Arrays.asList(fileNames));
    }

    public void addExcludedDirectories(String... directoryNames) {
        excludedDirectoryFromParsing.addAll(Arrays.asList(directoryNames));
    }

    public FileRepository(NPlugin plugin) {

        if (initializationMap.containsKey(plugin)) {
            ConsoleLogger.debug(plugin, "Failed to initialize FileRepository for %s plugin because a repository has already been created for it", plugin.getName());
            throw new IllegalStateException("Failed to initialize FileRepository for " + plugin.getName() + " plugin because a repository has already been created for it");
        }

        this.plugin = plugin;

        initializationMap.put(plugin, this);
    }

    public Optional<BukkitDirectory> getDirectoryOrLoad(String directoryName) {
        if (directories.containsKey(directoryName))
            return Optional.of(directories.get(directoryName));
        else {
            Optional<BukkitDirectory> optionalBukkitDirectory = load(directoryName);
            optionalBukkitDirectory.ifPresent(bukkitDirectory -> directories.put(directoryName, bukkitDirectory));
            return optionalBukkitDirectory;
        }
    }

    public Optional<BukkitDirectory> load(String directory) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        if (shouldSkipDirectory(directory)) {
            ConsoleLogger.debug(plugin, "Skipping load of %s directory", directory);
            return Optional.empty();
        }

        if (directories.containsKey(directory)) {
            ConsoleLogger.warn(plugin, "The %s directory has already been added to the repository, I'm skipping it.", directory);
            return Optional.empty();
        }

        String normalizePath = plugin.getDataFolder().getPath() + File.separator + directory;

        File directoryFile;
        try {
            directoryFile = FileHelper.getOrCreateDirectory(normalizePath);
        } catch (IOException exception) {
            ConsoleLogger.error(plugin, "Couldn't get or create a directory with a path: %s", normalizePath);
            throw new RuntimeException("Couldn't get or create a directory with a path: " + normalizePath + " : " + exception.getMessage());
        }

        Map<String, BukkitConfig> listOfFiles = loadFilesRecursively(directoryFile, "");
        BukkitDirectory bukkitDirectory = BukkitDirectory.of(plugin, directoryFile, listOfFiles);
        directories.put(normalizePath, bukkitDirectory);
        return Optional.of(bukkitDirectory);
    }

    private boolean shouldSkipContentParsing(File file) {
        return excludedFilesFromParsing.contains(file.getName());
    }

    private boolean shouldSkipDirectory(String directoryName) {
        return excludedDirectoryFromParsing.contains(directoryName);
    }

    protected Map<String, BukkitConfig> loadFilesRecursively(File dir, String relativePath) {
        Map<String, BukkitConfig> result = new HashMap<>();
        String currentRelativePath = relativePath != null ? relativePath : "";
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                String normalizePath = currentRelativePath + file.getName() + File.separator;
                result.putAll(loadFilesRecursively(file, normalizePath));
            } else if (file.getName().endsWith(".yml")) {
                String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                boolean skipParsing = shouldSkipContentParsing(file);

                if (skipParsing) {
                    ConsoleLogger.debug(plugin, "Skipping load of %s", name);
                    continue;
                }

                if (isFileExistsInAnyDirectory(name)) {
                    ConsoleLogger.warn(plugin, "Duplicate config file name %s found in %s", name, file.getParent());
                    continue;
                }

                result.put(name, new BukkitConfig(plugin, name));
            }
        }
        return result;
    }

    public Optional<BukkitConfig> getByName(BukkitDirectory directory, String fileName) {
        String dirPath =  directory.getPath();

        if (!directories.containsKey(dirPath)) {
            ConsoleLogger.debug(plugin, "Couldn't find %s directory in cache", dirPath);
            return Optional.empty();
        }

        return directories.get(fileName)
                .getConfig(fileName);
    }

    public Optional<BukkitConfig> create(BukkitDirectory directory, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(fileName != null && !fileName.isEmpty(), "fileName cannot be null or empty");

        String dirPath = directory.getPath();

        if (!directories.containsKey(dirPath)) {
            ConsoleLogger.warn(plugin, "Directory %s not found in cache!", dirPath);
            return Optional.empty();
        }

        if (isFileExistsInAnyDirectory(fileName)) {
            ConsoleLogger.warn(plugin, "File %s already exists in another directory!", fileName);
            return Optional.empty();
        }

        BukkitConfig bukkitConfig = new BukkitConfig(plugin, fileName);

        try {
            if (bukkitConfig.getFile().exists()) {
                ConsoleLogger.warn(plugin, "File %s already exists on disk!", fileName);
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

    public List<BukkitConfig> getAllFromDirectory(BukkitDirectory directory) {
        Objects.requireNonNull(directory, "Directory cant be null");

        String dirPath = directory.getPath();

        if (!directories.containsKey(dirPath)) {
            ConsoleLogger.warn(plugin, "It is impossible to get a list of configuration files from the %s directory because it has not been added to the cache!", directory);
            return Collections.emptyList();
        }

        BukkitDirectory bukkitDirectory = directories.get(dirPath);
        return bukkitDirectory.getCached();
    }

    private boolean isFileExistsInAnyDirectory(String fileName) {
        return directories.values()
                .stream()
                .anyMatch(dir -> dir.containsFileWithName(fileName));
    }
}