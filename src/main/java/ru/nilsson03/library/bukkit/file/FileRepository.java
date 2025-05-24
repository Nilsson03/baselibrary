package ru.nilsson03.library.bukkit.file;


import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.FileNotFoundException;
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
        }

        this.plugin = plugin;

        initializationMap.put(plugin, this);
    }

    public void removeFile(String directory, BukkitConfig config) {
        if (directories.containsKey(directory)) {
            directories.get(directory).removeConfig(config);
            String fileName = config.getFile().getName();
            boolean delete = config.getFile().delete();

            if (!delete) {
                ConsoleLogger.warn("baselibrary", "Couldn't delete %s file from %s directory", fileName, directory);
            } else {
                ConsoleLogger.info("baselibrary", "Deleted %s file from %s directory", fileName, directory);
            }
        } else {
            ConsoleLogger.warn("baselibrary", "The %s file could not be deleted from the %s directory because the directory was not found in the list.", config.getName(), directory);
        }
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

        String path = plugin.getDataFolder().getPath() + File.separator + directory;

        File directoryFile;
        try {
            directoryFile = FileHelper.getDirectory(path);
        } catch (IOException exception) {
            ConsoleLogger.error("baselibrary", "Couldn't get or create a directory with a path: %s", path);
            throw new RuntimeException("Couldn't get or create a directory with a path: " + path + " : " + exception.getMessage());
        }

        BukkitDirectory bukkitDirectory = BukkitDirectory.of(plugin, directoryFile);
        List<BukkitConfig> loadedFiles = new ArrayList<>();
        loadFilesRecursively(directoryFile, "", loadedFiles);

        directoryFilesMap.put(directory != null ? directory : "", loadedFiles);
    }

    private boolean shouldSkipContentParsing(File file) {
        return excludedFilesFromParsing.contains(file.getName());
    }

    private boolean shouldSkipDirectory(String directoryName) {
        return excludedDirectoryFromParsing.contains(directoryName);
    }

    private void loadFilesRecursively(File dir, String relativePath, List<BukkitConfig> result) {
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                loadFilesRecursively(file,
                        relativePath + file.getName() + "/", result);
            } else if (file.getName().endsWith(".yml")) {
                String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                boolean skipParsing = shouldSkipContentParsing(file);

                if (skipParsing)
                    ConsoleLogger.debug(plugin, "Skipping load of %s", name);

                result.add(new BukkitConfig(plugin, name));
            }
        }
    }

    /**
     * Внутренний метод для поиска конфигурации.
     *
     * @param name имя конфигурации
     * @return найденная конфигурация
     * @throws FileNotFoundException если конфигурация не найдена
     */
    public BukkitConfig getByName(String name) throws FileNotFoundException {
        String directoryName = plugin.getDataFolder().getName();
        Optional<BukkitConfig> optional = directoryFilesMap.get(directoryName)
                .stream()
                .filter(predicate -> predicate.getName().equals(name))
                .findFirst();

        if (optional.isPresent()) {
            return optional.get();
        }

        throw new FileNotFoundException("Не удалось найти файл с названием: " + name);
    }

    /**
     * Получает конфигурацию по имени и директории.
     *
     * @param directory относительный путь к директории (null для корневой)
     * @param name имя конфигурации (без .yml)
     * @return найденная конфигурация
     * @throws FileNotFoundException если конфигурация не найдена
     */
    public BukkitConfig getByName(String directory, String name) throws FileNotFoundException {
        String dirKey = directory != null ? directory : "";
        Optional<BukkitConfig> config = directoryFilesMap.getOrDefault(dirKey, Collections.emptyList())
                .stream()
                .filter(predicate -> predicate.getName().equals(dirKey + "/" + name))
                .findFirst();

        return config.orElseThrow(() ->
                new FileNotFoundException("The file '" + name + "' was not found in the directory '" + dirKey + "'"));
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

        String path = plugin.getDataFolder().getPath() + File.pathSeparator + directory;

        File directoryFile;
        try {
            directoryFile = FileHelper.getDirectory(path);
        } catch (IOException exception) {
            ConsoleLogger.error("baselibrary", "Couldn't get or create a directory with path: " + path);
            return Optional.empty();
        }

        File configFile = FileHelper.createFileOrLoad(directoryFile, name);
        BukkitConfig bukkitConfig = new BukkitConfig(plugin, (directory != null ? directory + "/" : "") + name);
        directoryFilesMap.computeIfAbsent(directory, k -> new ArrayList<>()).add(bukkitConfig);

        return Optional.of(bukkitConfig);
    }

    /**
     * Получает все конфигурации из указанной директории.
     *
     * @param directory относительный путь к директории (null для корневой)
     * @return список конфигураций
     */
    public List<BukkitConfig> getAllFromDirectory(String directory) {
        return directoryFilesMap.getOrDefault(directory != null ? directory : "", Collections.emptyList());
    }

    /**
     * Сохраняет все конфигурации указанного плагина.
     */
    public void save(String directory, String fileName) {
        BukkitConfig bukkitConfig;
        try {
            bukkitConfig = getByName(directory, fileName);
        } catch (FileNotFoundException exception) {
            String text = "Не удалось сохранить файл" + directory + "/" + fileName;
            ConsoleLogger.warn("baselibrary", "Couldn't save %s file.", directory + "/" + fileName);
            throw new RuntimeException(text + ":" + exception.getMessage());
        }

        if (bukkitConfig != null) {
            bukkitConfig.saveConfiguration();
        }
    }

    /**
     * Перезагружает конкретную конфигурацию плагина по имени файла.
     *
     * @param fileName имя конфигурации (без .yml)
     */
    public void reload(String directory, String fileName) {

        BukkitConfig bukkitConfig;
        try {
            bukkitConfig = getByName(directory, fileName);
        } catch (FileNotFoundException exception) {
            String text = "Не удалось перезагрузить файл" + directory + "/" + fileName;
            ConsoleLogger.warn("baselibrary", "Failed to reload %s file.", directory + "/" + fileName);
            throw new RuntimeException(text + ":" + exception.getMessage());
        }

        if (bukkitConfig == null) {
            return;
        }

        FileConfiguration configuration = FileHelper.reloadFile(bukkitConfig.getPlugin(), bukkitConfig.getFileConfiguration());
        bukkitConfig.updateFileConfiguration(configuration);
    }
}