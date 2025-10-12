package ru.nilsson03.library.bukkit.file;

import com.google.common.base.Preconditions;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FileRepository {
    private static final Map<NPlugin, FileRepository> initializationMap = new ConcurrentHashMap<>();
    private final NPlugin plugin;
    private final Map<String, BukkitDirectory> directories;
    private final Set<String> excludedPaths;

    {
        directories = new ConcurrentHashMap<>();
        excludedPaths = ConcurrentHashMap.newKeySet();
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

        Map<String, BukkitConfig> files = loadFiles(directoryFile, true, false);
        BukkitDirectory bukkitDirectory = BukkitDirectory.of(plugin, directoryFile, files);
        directories.put(directory, bukkitDirectory);
        return Optional.of(bukkitDirectory);
    }

    private Map<String, BukkitConfig> loadFiles(File dir, boolean autoParseEnabled, boolean force) {
        Map<String, BukkitConfig> result = new HashMap<>();
        File[] directoryFiles = dir.listFiles();
        if (directoryFiles == null) {
            ConsoleLogger.debug(plugin, "No files found in directory: %s", dir.getPath());
            return result;
        }

        ConsoleLogger.debug(plugin, "Loading files from directory: %s (found %d files)", 
                           dir.getPath(), directoryFiles.length);

        for (File file : directoryFiles) {
            if (file.isFile() && file.getName().endsWith(".yml")) {
                String name = file.getName();
                String relativePath = getRelativePathFromPluginRoot(file, dir);
                
                ConsoleLogger.debug(plugin, "Processing file: %s (relative path: %s)", name, relativePath);
                
                if (shouldSkipContentParsing(file) && !force) {
                    ConsoleLogger.debug(plugin, "Skipping load of %s (path: %s)", name, relativePath);
                    continue;
                }
                if (isFileExistsInAnyDirectory(name)) {
                    ConsoleLogger.warn(plugin, "Duplicate config %s found (path: %s)", name, relativePath);
                    continue;
                }
                
                ConsoleLogger.debug(plugin, "Loading config: %s (path: %s)", name, relativePath);
                result.put(name, new BukkitConfig(plugin, dir, name, autoParseEnabled));
            }
        }
        
        ConsoleLogger.debug(plugin, "Loaded %d configs from directory: %s", result.size(), dir.getPath());
        return result;
    }

    public void loadFiles(BukkitDirectory directory, boolean autoParse) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Map<String, BukkitConfig> files = loadFiles(directory.getFile(), autoParse, true);
        directory.addAll(files);
        ConsoleLogger.debug(plugin, "Loaded %d configs from directory: %s", files.size(), directory.getPath());
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

    @Deprecated
    private boolean shouldSkipContentParsing(File file) {
        File parentDir = file.getParentFile();
        if (shouldSkipFileByPath(file, parentDir)) {
            return true;
        }
        
        return false;
    }

    private boolean isFileExistsInAnyDirectory(String fileName) {
        return directories.values().stream()
                .anyMatch(dir -> dir.containsFileWithName(fileName));
    }

    /**
     * Добавляет путь файла или папки в исключения.
     * Поддерживает как файлы в корне, так и файлы в поддиректориях, а также целые папки.
     * 
     * @param path Путь к файлу или папке для исключения (например: "config.yml", "inventories/shop.yml", "users")
     */
    public void addExcludePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            ConsoleLogger.warn(plugin, "Cannot add empty or null path to exclusions");
            return;
        }
        
        String normalizedPath = normalizePath(path);
        excludedPaths.add(normalizedPath);
        ConsoleLogger.debug(plugin, "Added exclusion path: %s (normalized: %s)", path, normalizedPath);
    }

    /**
     * Добавляет несколько путей файлов или папок в исключения.
     * Поддерживает как файлы в корне, так и файлы в поддиректориях, а также целые папки.
     * 
     * @param paths Пути к файлам или папкам для исключения (например: "config.yml", "inventories/shop.yml", "users", "data/players.yml")
     */
    public void addExcludePaths(String... paths) {
        if (paths == null || paths.length == 0) {
            ConsoleLogger.warn(plugin, "Cannot add null or empty paths array to exclusions");
            return;
        }

        int addedCount = 0;
        int skippedCount = 0;

        for (String path : paths) {
            if (path == null || path.trim().isEmpty()) {
                ConsoleLogger.warn(plugin, "Skipping null or empty path in exclusions");
                skippedCount++;
                continue;
            }
            
            String normalizedPath = normalizePath(path);
            if (excludedPaths.add(normalizedPath)) {
                addedCount++;
                ConsoleLogger.debug(plugin, "Added exclusion path: %s (normalized: %s)", path, normalizedPath);
            } else {
                ConsoleLogger.debug(plugin, "Path already excluded: %s (normalized: %s)", path, normalizedPath);
            }
        }

        ConsoleLogger.info(plugin, "Added %d exclusion paths (skipped: %d, total excluded: %d)", 
                         addedCount, skippedCount, excludedPaths.size());
    }

    /**
     * Нормализует путь для кроссплатформенности.
     * Заменяет все разделители на File.separator и убирает лишние разделители.
     * 
     * @param path Исходный путь
     * @return Нормализованный путь
     */
    private String normalizePath(String path) {
        if (path == null) return null;
        return Path.of(path).normalize().toString();
    }

    /**
     * Проверяет, должен ли файл быть исключен на основе его полного пути.
     * Сравнивает точный путь файла с исключениями.
     * 
     * @param file Файл для проверки
     * @param parentDirectory Родительская директория файла
     * @return true если файл должен быть исключен
     */
    private boolean shouldSkipFileByPath(File file, File parentDirectory) {
        if (excludedPaths.isEmpty()) {
            return false;
        }
        
        // Получаем относительный путь от корня плагина
        String relativePath = getRelativePathFromPluginRoot(file, parentDirectory);
        String normalizedPath = normalizePath(relativePath);
        
        // Проверяем точное соответствие пути
        if (excludedPaths.contains(normalizedPath)) {
            ConsoleLogger.debug(plugin, "Skipping file by exact path exclusion: %s (normalized: %s)", 
                              relativePath, normalizedPath);
            return true;
        }
        
        // Проверяем, находится ли файл в исключенной папке
        for (String excludedPath : excludedPaths) {
            if (normalizedPath.startsWith(excludedPath + File.separator) || 
                normalizedPath.startsWith(excludedPath + "/")) {
                ConsoleLogger.debug(plugin, "Skipping file by folder exclusion: %s (in folder: %s)", 
                                  relativePath, excludedPath);
                return true;
            }
        }
        
        return false;
    }

    /**
     * Получает относительный путь файла от корня плагина.
     * 
     * @param file Файл
     * @param parentDirectory Родительская директория
     * @return Относительный путь
     */
    private String getRelativePathFromPluginRoot(File file, File parentDirectory) {
        try {
            String pluginRootPath = plugin.getDataFolder().getCanonicalPath();
            String filePath = file.getCanonicalPath();
            
            if (filePath.startsWith(pluginRootPath)) {
                String relativePath = filePath.substring(pluginRootPath.length());
                // Убираем начальный разделитель если есть
                if (relativePath.startsWith(File.separator)) {
                    relativePath = relativePath.substring(File.separator.length());
                }
                return relativePath;
            }
        } catch (Exception e) {
            ConsoleLogger.warn(plugin, "Failed to get relative path for file %s: %s", 
                              file.getName(), e.getMessage());
        }
        
        // Fallback: используем имя файла если не удалось получить относительный путь
        return file.getName();
    }

    public Optional<BukkitConfig> getByName(BukkitDirectory directory, String fileName) {
        String dirPath = directory.getPath();
        if (!directories.containsKey(dirPath)) {
            ConsoleLogger.debug(plugin, "Directory %s not in cache", dirPath);
            return Optional.empty();
        }
        BukkitConfig config = directories.get(dirPath).getBukkitConfig(fileName);
        return config != null ? Optional.of(config) : Optional.empty();
    }

    /**
     * Получает список всех исключенных путей.
     * 
     * @return Множество исключенных путей
     */
    public Set<String> getExcludedPaths() {
        return new HashSet<>(excludedPaths);
    }

    /**
     * Проверяет, исключен ли указанный путь.
     * 
     * @param path Путь для проверки
     * @return true если путь исключен
     */
    public boolean isPathExcluded(String path) {
        if (path == null) return false;
        String normalizedPath = normalizePath(path);
        return excludedPaths.contains(normalizedPath);
    }

    /**
     * Очищает все исключения путей.
     */
    public void clearExcludedPaths() {
        excludedPaths.clear();
        ConsoleLogger.debug(plugin, "Cleared all path exclusions");
    }
}