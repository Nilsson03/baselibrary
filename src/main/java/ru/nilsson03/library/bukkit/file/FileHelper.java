package ru.nilsson03.library.bukkit.file;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FileHelper {

    /**
     * Загружает конфигурацию из файла.
     *
     * @param plugin    Плагин.
     * @param dataFolder Директория с файлами.
     * @param fileName  Имя файла.
     * @return Загруженная FileConfiguration.
     */
    public static FileConfiguration loadConfiguration(Plugin plugin, File dataFolder, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(dataFolder, "dataFolder cannot be null");
        validateFileName(fileName);

        File file = new File(dataFolder, fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
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
    public static FileConfiguration loadConfiguration(Plugin plugin, String fileName) {
        return loadConfiguration(plugin, plugin.getDataFolder(), fileName);
    }

    /**
     * Загружает несколько конфигураций.
     *
     * @param plugin    Плагин.
     * @param fileNames Имена файлов.
     */
    public static void loadConfigurations(Plugin plugin, String... fileNames) {
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
     * @param nPlugin плагин
     * @return Перезагруженная FileConfiguration.
     */
    public static FileConfiguration reloadFile(NPlugin nPlugin, FileConfiguration configuration) {
        Objects.requireNonNull(nPlugin, "plugin cannot be null");
        Objects.requireNonNull(configuration, "configuration cannot be null");

        String fileName = configuration.getName();
        saveFile(configuration, nPlugin.getDataFolder(), fileName);
        ConsoleLogger.debug(nPlugin, "The file %s has been successfully reloaded.", fileName);
        return loadConfiguration(nPlugin, fileName);
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
