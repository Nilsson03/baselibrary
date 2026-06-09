package ru.nilsson03.library.file.directory;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.file.config.YamlConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class DirectoryRepository {
    private static final Map<NPlugin, DirectoryRepository> initializationMap = new ConcurrentHashMap<>();
    private final NPlugin plugin;
    private final Map<String, Directory> directories;

    {
        directories = new ConcurrentHashMap<>();
    }

    public static DirectoryRepository of(NPlugin plugin) {
        return initializationMap.getOrDefault(plugin, new DirectoryRepository(plugin));
    }

    public DirectoryRepository(NPlugin plugin) {
        if (initializationMap.containsKey(plugin)) {
            ConsoleLogger.debug(plugin, "DirectoryRepository already exists for %s", plugin.getName());
            throw new IllegalStateException("DirectoryRepository already exists for " + plugin.getName());
        }
        this.plugin = plugin;
        initializationMap.put(plugin, this);
    }

    public void unregister() {
        if (initializationMap.containsKey(plugin)) {
            directories.clear();
            initializationMap.remove(plugin);
        }
    }

    public Optional<Directory> getDirectoryOrLoad(String directoryName) {
        if (directories.containsKey(directoryName)) {
            return Optional.of(directories.get(directoryName));
        }
        return load(directoryName);
    }

    public Optional<Directory> load(String directory) {
        Objects.requireNonNull(plugin, "plugin cannot be null");

        if (directories.containsKey(directory)) {
            return Optional.of(directories.get(directory));
        }

        Path directoryPath = plugin.getDataFolder().toPath().resolve(directory);
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            ConsoleLogger.error(plugin, "Couldn't create directory %s: %s", directoryPath, e.getMessage());
            return Optional.empty();
        }

        Map<String, YamlConfig> files = loadFiles(directoryPath);
        Directory dir = Directory.of(plugin, directoryPath, files);
        directories.put(directory, dir);
        return Optional.of(dir);
    }

    private Map<String, YamlConfig> loadFiles(Path dir) {
        Map<String, YamlConfig> result = new HashMap<>();
        
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> yamlFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .toList();

            ConsoleLogger.debug(plugin, "Loading files from directory: %s (found %d files)", 
                               dir, yamlFiles.size());

            for (Path file : yamlFiles) {
                String name = file.getFileName().toString();
                
                if (name.trim().isEmpty()) {
                    ConsoleLogger.warn(plugin, "Skipping file with empty name in directory: %s", dir);
                    continue;
                }
                
                String relativePath = getRelativePathFromPluginRoot(file);
                
                ConsoleLogger.debug(plugin, "Processing file: %s (relative path: %s)", name, relativePath);
                
                if (isFileExistsInAnyDirectory(name)) {
                    ConsoleLogger.warn(plugin, "Duplicate config %s found (path: %s)", name, relativePath);
                    continue;
                }
                
                ConsoleLogger.debug(plugin, "Loading config: %s (path: %s)", name, relativePath);
                YamlConfig config = new YamlConfig(plugin, dir, name);
                result.put(name, config);
            }
            
            ConsoleLogger.debug(plugin, "Loaded %d configs from directory: %s", result.size(), dir);
        } catch (IOException e) {
            ConsoleLogger.error(plugin, "Failed to list files in directory %s: %s", dir, e.getMessage());
        }
        
        return result;
    }

    public void loadFiles(Directory directory) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Map<String, YamlConfig> files = loadFiles(directory.getPathAsPath());
        directory.addAll(files);
        ConsoleLogger.debug(plugin, "Loaded %d configs from directory: %s", files.size(), directory.getPath());
    }

    public Optional<YamlConfig> create(Directory directory, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be empty");
        }

        String dirPath = directory.getPath();
        if (!directories.containsValue(directory)) {
            ConsoleLogger.warn(plugin, "Directory %s not found", dirPath);
            return Optional.empty();
        }

        if (isFileExistsInAnyDirectory(fileName)) {
            ConsoleLogger.warn(plugin, "File %s already exists", fileName);
            return Optional.empty();
        }

        try {
            Path filePath = directory.getPathAsPath().resolve(fileName);
            if (Files.exists(filePath)) {
                ConsoleLogger.warn(plugin, "File %s exists on disk", fileName);
                return Optional.empty();
            }

            YamlConfig config = directory.addNewConfig(fileName);
            if (config != null) {
                config.getCache().save();
                return Optional.of(config);
            }
            return Optional.empty();
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Failed to create config %s: %s", fileName, e.getMessage());
            return Optional.empty();
        }
    }

    private boolean isFileExistsInAnyDirectory(String fileName) {
        return directories.values().stream()
                .anyMatch(dir -> dir.containsFileWithName(fileName));
    }

    private String normalizePath(String path) {
        if (path == null) return null;
        return Path.of(path).normalize().toString();
    }

    private String getRelativePathFromPluginRoot(Path file) {
        try {
            Path pluginRootPath = plugin.getDataFolder().toPath();
            if (file.startsWith(pluginRootPath)) {
                Path relativePath = pluginRootPath.relativize(file);
                return relativePath.toString();
            }
        } catch (Exception e) {
            ConsoleLogger.warn(plugin, "Failed to get relative path for file %s: %s", 
                              file.getFileName(), e.getMessage());
        }
        return file.getFileName().toString();
    }

    public Optional<YamlConfig> getByName(Directory directory, String fileName) {
        String dirPath = directory.getPath();
        if (!directories.containsValue(directory)) {
            ConsoleLogger.debug(plugin, "Directory %s not in cache", dirPath);
            return Optional.empty();
        }
        YamlConfig config = directory.getYamlConfig(fileName);
        return config != null ? Optional.of(config) : Optional.empty();
    }
}
