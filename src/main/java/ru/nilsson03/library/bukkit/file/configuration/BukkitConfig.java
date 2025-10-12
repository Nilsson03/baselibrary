package ru.nilsson03.library.bukkit.file.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.FileHelper;
import ru.nilsson03.library.bukkit.file.configuration.impl.BukkitConfigurationImpl;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

/**
 * @author Nicholas Alexandrov 18.06.2023
 */
public class BukkitConfig {

    private final String name;
    private final File file;
    private BukkitConfigurationImpl configuration;
    private FileConfiguration fileConfiguration;
    private final NPlugin plugin;

    public BukkitConfig(NPlugin plugin, File directory, String fileName) {
        this(plugin, directory, fileName, true);
    }

    public BukkitConfig(NPlugin plugin, File directory, String fileName, boolean autoParseEnabled) {
        if (plugin == null) {
            ConsoleLogger.debug("baselibrary", "Plugin cannot be null, class %s, plugin", getClass().getName());
            throw new IllegalArgumentException("plugin cannot be null, class " + getClass().getName());
        }

        validateFileName(fileName);

        if (fileName == null || fileName.isEmpty()) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }
        this.plugin = plugin;
        
        // Добавляем расширение .yml если файл не имеет расширения
        String processedFileName = fileName;
        if (!fileName.toLowerCase().endsWith(".yml") && !fileName.toLowerCase().endsWith(".yaml")) {
            processedFileName = fileName + ".yml";
        }
        
        this.file = new File(directory, processedFileName);
        this.name = processedFileName;

        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + directory);
        }

        try {
            this.fileConfiguration = FileHelper.loadConfiguration(plugin, directory, processedFileName);
        } catch (Exception e) {
            ConsoleLogger.debug(plugin, "Failed to load configuration file %s, class %s, plugin %s", e.getMessage(), getClass().getName(), plugin.getName());
            throw new IllegalStateException("Failed to load configuration file, class " + getClass().getName(), e);
        }
        this.configuration = new BukkitConfigurationImpl(plugin, processedFileName, fileConfiguration, autoParseEnabled);
    }

    public NPlugin getPlugin() {
        return plugin;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }

    public BukkitConfigurationImpl getBukkitConfiguration() {
        if (configuration == null) {
            configuration = new BukkitConfigurationImpl(plugin, file.getName(), fileConfiguration);
        }
        return configuration;
    }

    /**
     * Получает конфигурацию с возможностью отключения автоматического парсинга.
     * 
     * @param autoParseEnabled true если нужно включить автоматический парсинг
     * @return BukkitConfigurationImpl с указанными настройками парсинга
     */
    public BukkitConfigurationImpl getBukkitConfiguration(boolean autoParseEnabled) {
        return new BukkitConfigurationImpl(plugin, file.getName(), fileConfiguration, autoParseEnabled);
    }

    public void reloadConfiguration() {
        FileConfiguration updatedConfiguration = FileHelper.reloadFile(getPlugin(), fileConfiguration);
        updateFileConfiguration(updatedConfiguration);
    }

    public void updateFileConfiguration(FileConfiguration fileConfiguration) {
        Objects.requireNonNull(fileConfiguration, "configuration cannot be null");
        this.fileConfiguration = fileConfiguration;
        configuration.clearFileContentAndLoad();
        ConsoleLogger.debug(plugin, "File %s has been loaded (Class %s).",
                file.getName(),
                this.getClass().getName());
    }

    public void saveConfiguration() {
        try {
            FileHelper.saveFile(fileConfiguration, file.getParentFile(), name);
            ConsoleLogger.debug(plugin, "File %s successfully saved (Class %s).",
                    name,
                    this.getClass().getName());
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to save configuration " + name + " for plugin " + plugin.getName() + " error: " + exception.getMessage());
            throw new RuntimeException("Failed to save configuration " + name + " for plugin " + plugin.getName(), exception);
        }
    }

    public FileConfiguration loadConfiguration() {
        try {
            fileConfiguration = FileHelper.loadConfiguration(plugin, name);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to load configuration " + name + " for plugin " + plugin.getName() + " error: " + exception.getMessage());
            throw new RuntimeException("Failed to load configuration " + name + " for plugin " + plugin.getName(), exception);
        }

        return fileConfiguration;
    }

    public FileConfiguration getFileConfiguration() {
        if (fileConfiguration == null) {
            fileConfiguration = loadConfiguration();
        }
        return fileConfiguration;
    }


    public InputStream getInputStream() throws FileNotFoundException, SecurityException {
        return new FileInputStream(file);
    }

    /**
     * Получает объект ConfigOperations для работы с кэшированными полями файла.
     * 
     * Важно: Этот метод работает только при включенной функции авто-парсинга.
     * Если авто-парсинг отключен (autoParseEnabled = false), то operations() вернет null,
     * так как кэшированные данные не загружаются.
     * 
     * ConfigOperations предоставляет доступ к предварительно загруженным и обработанным
     * данным из YAML файла, что может быть полезно для быстрого доступа к часто используемым значениям.
     * 
     *
     * @return ConfigOperations для работы с кэшированными данными или null если авто-парсинг отключен.
     * @throws NullPointerException если конфигурация не инициализирована.
     */
    public ConfigOperations operations() throws NullPointerException {
        Objects.requireNonNull(configuration, "Configuration cannot be null");

        return getBukkitConfiguration().operations();
    }

    /**
     * Получает значение типа boolean из конфигурационного файла по указанному пути.
     * Работает напрямую с FileConfiguration без использования кэшированных данных.
     *
     * @param path Путь к значению в YAML файле.
     * @return Значение boolean или false по умолчанию.
     */
    public boolean getBoolean(String path) {
        return fileConfiguration.getBoolean(path);
    }

    /**
     * Получает значение типа int из конфигурационного файла по указанному пути.
     * Работает напрямую с FileConfiguration без использования кэшированных данных.
     *
     * @param path Путь к значению в YAML файле.
     * @return Значение int или 0 по умолчанию.
     */
    public int getInt(String path) {
        return fileConfiguration.getInt(path);
    }

    /**
     * Получает значение типа long из конфигурационного файла по указанному пути.
     * Работает напрямую с FileConfiguration без использования кэшированных данных.
     *
     * @param path Путь к значению в YAML файле.
     * @return Значение long или 0 по умолчанию.
     */
    public long getLong(String path) {
        return fileConfiguration.getLong(path);
    }

    /**
     * Получает значение типа double из конфигурационного файла по указанному пути.
     * Работает напрямую с FileConfiguration без использования кэшированных данных.
     *
     * @param path Путь к значению в YAML файле.
     * @return Значение double или 0.0 по умолчанию.
     */
    public double getDouble(String path) {
        return fileConfiguration.getDouble(path);
    }

    /**
     * Получает список строк из конфигурационного файла по указанному пути.
     * Работает напрямую с FileConfiguration без использования кэшированных данных.
     *
     * @param path Путь к значению в YAML файле.
     * @return Список строк или пустой список если значение не найдено.
     */
    public List<String> getList(String path) {
        return fileConfiguration.getStringList(path);
    }

    /**
     * Получает строку из конфигурационного файла по указанному пути.
     * Работает напрямую с FileConfiguration без использования кэшированных данных.
     *
     * @param path Путь к значению в YAML файле.
     * @return Строка или null если значение не найдено.
     */
    public String getString(String path) {
        return fileConfiguration.getString(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return fileConfiguration.getConfigurationSection(path);
    }

    public void delete() {
        boolean delete = file.delete();

        if (delete)
            ConsoleLogger.info(plugin, "Deleted configuration %s for plugin %s.", getFile().getName(), plugin.getName());
        else
            ConsoleLogger.warn(plugin, "Failed to delete configuration %s for plugin %s.", getFile().getName(), plugin.getName());
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.startsWith("/")) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }
    }

    @Deprecated
    public FileConfiguration getConfiguration() {
        return fileConfiguration;
    }
}
