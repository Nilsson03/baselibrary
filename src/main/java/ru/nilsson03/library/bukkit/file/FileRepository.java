package ru.nilsson03.library.bukkit.file;

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

    public void load() {
        load(plugin.getDataFolder().getName());
    }

    /**
     * Загружает все YAML-файлы из указанной директории и поддиректорий.
     *
     * @param directory относительный путь к директории (null для корневой)
     */
    public void load(String directory) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        if (shouldSkipDirectory(directory)) {
            ConsoleLogger.debug(plugin, "Skipping load of %s directory", directory);
            return;
        }

        if (directories.containsKey(directory)) {
            ConsoleLogger.warn(plugin, "The %s directory has already been added to the repository, I'm skipping it.", directory);
            return;
        }

        String normalizePath = normalizePath(plugin.getDataFolder().getPath() + File.separator + directory);

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
                String normalizePath = normalizePath(currentRelativePath + file.getName() + File.separator);
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

    /**
     * Внутренний метод для поиска конфигурации.
     *
     * @param name имя конфигурации
     * @return найденная конфигурация
     */
    public Optional<BukkitConfig> getByName(String name) {
        String directoryPath = normalizePath(plugin.getDataFolder().getAbsolutePath());

        if (!directories.containsKey(directoryPath)) {
            ConsoleLogger.debug(plugin, "Couldn't find %s directory in cache", directoryPath);
            return Optional.empty();
        }

        return directories.get(directoryPath)
                .getConfig(name);
    }

    /**
     * Получает конфигурацию по имени и директории.
     *
     * @param directory относительный путь к директории (null для корневой)
     * @param name имя конфигурации (без .yml)
     * @return найденная конфигурация
     */
    public Optional<BukkitConfig> getByName(String directory, String name) {
        String dirPath = directory != null ? normalizePath(directory) : "";
        String normalizePath = normalizePath(plugin.getDataFolder().getPath() + File.separator + dirPath);

        if (!directories.containsKey(normalizePath)) {
            ConsoleLogger.debug(plugin, "Couldn't find %s directory in cache", normalizePath);
            return Optional.empty();
        }

        return directories.get(normalizePath)
                .getConfig(name);
    }

    /**
     * Создает новую конфигурацию в указанной директории.
     *
     * @param directory относительный путь к директории
     * @param name имя конфигурации
     * @return новая конфигурация
     */
    public Optional<BukkitConfig> create(String directory, String name) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(name, "name cannot be null");

        String dirPath = directory != null ? normalizePath(directory) : "";
        String fullPath = normalizePath(plugin.getDataFolder().getPath() + File.separator + dirPath);

        if (!directories.containsKey(fullPath)) {
            ConsoleLogger.warn(plugin, "Directory %s not found in cache!", fullPath);
            return Optional.empty();
        }

        if (isFileExistsInAnyDirectory(name)) {
            ConsoleLogger.warn(plugin, "File %s already exists in another directory!", name);
            return Optional.empty();
        }

        BukkitDirectory bukkitDirectory = directories.get(fullPath);
        BukkitConfig bukkitConfig = new BukkitConfig(plugin, name);

        try {
            if (bukkitConfig.getFile().exists()) {
                ConsoleLogger.warn(plugin, "File %s already exists on disk!", name);
                return Optional.empty();
            }

            bukkitDirectory.addNewConfig(bukkitConfig);
            bukkitConfig.saveConfiguration();
            return Optional.of(bukkitConfig);
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Failed to create config %s: %s", name, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Получает все конфигурации из указанной директории.
     *
     * @param directory относительный путь к директории (null для корневой)
     * @return список конфигураций
     */
    public List<BukkitConfig> getAllFromDirectory(String directory) {
        String dirPath = directory != null ? normalizePath(directory) : "";
        String normalizePath = normalizePath(plugin.getDataFolder().getPath() + File.separator + dirPath);

        if (!directories.containsKey(normalizePath)) {
            ConsoleLogger.warn(plugin, "It is impossible to get a list of configuration files from the %s directory because it has not been added to the cache!", directory);
            return Collections.emptyList();
        }

        BukkitDirectory bukkitDirectory = directories.get(normalizePath);
        return bukkitDirectory.getCached();
    }

    private boolean isFileExistsInAnyDirectory(String fileName) {
        return directories.values()
                .stream()
                .anyMatch(dir -> dir.containsFileWithName(fileName));
    }

    private String normalizePath(String path) {
        return path.replace("/", File.separator).replace("\\", File.separator);
    }
}