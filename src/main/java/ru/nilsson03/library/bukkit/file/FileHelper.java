package ru.nilsson03.library.bukkit.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FileHelper {

    /**
     * Загружает конфигурацию из файла.
     *
     * @param plugin    Плагин.
     * @param dataFolder Директория с файлами.
     * @param jarResourcePath  Имя файла.
     * @return Загруженная FileConfiguration.
     */
    public static FileConfiguration loadConfiguration(NPlugin plugin, File dataFolder, String jarResourcePath) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(dataFolder, "dataFolder cannot be null");
        validateFileName(jarResourcePath);

        File configFile = new File(dataFolder, jarResourcePath);

        if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + configFile.getParent());
        }

        if (!configFile.exists() || configFile.length() == 0) {
            try (InputStream input = plugin.getResource(jarResourcePath)) {
                if (input != null) {
                    Files.copy(input, configFile.toPath());
                    ConsoleLogger.info(plugin, "Successfully copied default config: %s", configFile.getPath());
                } else {
                    if (configFile.createNewFile()) {
                        ConsoleLogger.info(plugin, "Created EMPTY config file: %s", configFile.getPath());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to initialize config: " + jarResourcePath, e);
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static Set<FileConfiguration> loadConfigurations(NPlugin plugin, File dataFolder, String... fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(dataFolder, "dataFolder cannot be null");

        Set<FileConfiguration> fileConfigurations = new HashSet<>();

        for (String string : fileName) {
            fileConfigurations.add(loadConfiguration(plugin, dataFolder, string));
        }

        return fileConfigurations;
    }

    /**
     * Загружает конфигурацию из файла в директории плагина.
     *
     * @param plugin   Плагин.
     * @param fileName Имя файла.
     * @return Загруженная FileConfiguration.
     */
    public static FileConfiguration loadConfiguration(NPlugin plugin, String fileName) {
        return loadConfiguration(plugin, plugin.getDataFolder(), fileName);
    }

    /**
     * Загружает несколько конфигураций.
     *
     * @param plugin    Плагин.
     * @param fileNames Имена файлов.
     */
    public static void loadConfigurations(NPlugin plugin, String... fileNames) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(fileNames, "fileNames cannot be null");

        for (String fileName : fileNames) {
            loadConfiguration(plugin, fileName);
        }
    }

    /**
     * Создает директорию, если она не существует.
     *
     * @param pathName Путь к директории.
     * @return Созданная или существующая директория.
     */
    public static File getOrCreateDirectory(String pathName) throws IOException {
        validatePathName(pathName);

        File directory = new File(pathName);
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + pathName);
            }
        } else if (!directory.isDirectory()) {
            throw new IOException("Path is not a directory: " + pathName);
        }
        return directory;
    }

    /**
     * Создает файл или загружает его, если он уже существует.
     *
     * @param dataFolder Директория для файла.
     * @param fileName   Имя файла.
     * @return Созданный или существующий файл.
     */
    public static File createFileOrLoad(File dataFolder, String fileName) {
        Objects.requireNonNull(dataFolder, "dataFolder cannot be null");
        validateFileName(fileName);

        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException | SecurityException e) {
                ConsoleLogger.warn("baselibrary","Не удалось создать %s по причине %s", fileName, e.getMessage());
            }
        }
        return file;
    }

    /**
     * Создает файл или загружает его в директории плагина.
     *
     * @param plugin   Плагин.
     * @param fileName Имя файла.
     * @return Созданный или существующий файл.
     */
    public static File createFileOrLoad(Plugin plugin, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        return createFileOrLoad(plugin.getDataFolder(), fileName);
    }

    /**
     * Сохраняет конфигурацию в файл.
     *
     * @param fileConfiguration Конфигурация для сохранения.
     * @param directory        Директория для файла.
     * @param fileName          Имя файла.
     */
    public static void saveFile(FileConfiguration fileConfiguration, File directory, String fileName) {
        Objects.requireNonNull(fileConfiguration, "fileConfiguration cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");
        validateFileName(fileName);

        try {
            File file = new File(directory, fileName);
            fileConfiguration.save(file);
        } catch (IOException e) {
            ConsoleLogger.warn("baselibrary", "Failed to save file %s due to %s.", fileName, e.getMessage());
        }
    }

    /**
     * Перезагружает конфигурацию из файла.
     *
     * @param plugin плагин
     * @param config конфигурация
     * @return Перезагруженная FileConfiguration.
     */
    public static FileConfiguration reloadFile(NPlugin plugin, BukkitConfig config) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(config, "configuration cannot be null");

        File directory = config.getDirectory();
        String fileName = config.getName();
        File configFile = new File(directory, fileName);

        if (!configFile.exists()) {
            ConsoleLogger.warn(plugin, "Config file %s does not exist, cannot reload", fileName);
            return config.getFileConfiguration();
        }

        FileConfiguration reloadedConfig = YamlConfiguration.loadConfiguration(configFile);
        ConsoleLogger.debug(plugin, "The file %s has been successfully reloaded from disk.", fileName);
        return reloadedConfig;
    }

    /**
     * Проверяет корректность имя файла.
     *
     * @param fileName Имя файла.
     * @throws IllegalArgumentException если fileName не прошло проверку на корректность.
     */
    private static void validateFileName(String fileName) throws IllegalArgumentException {
        if (fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be empty");
        }
    }

    /**
     * Проверяет корректность пути к директории.
     *
     * @param pathName Путь к директории.
     * @throws IllegalArgumentException если pathName не прошло проверку на корректность.
     */
    private static void validatePathName(String pathName) throws IllegalArgumentException {
        if (pathName.trim().isEmpty()) {
            throw new IllegalArgumentException("pathName cannot be null or empty");
        }
    }
}
