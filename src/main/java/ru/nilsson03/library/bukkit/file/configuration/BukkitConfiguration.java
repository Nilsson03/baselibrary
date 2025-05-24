package ru.nilsson03.library.bukkit.file.configuration;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.FileRepository;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface BukkitConfiguration {

    FileConfiguration getBukkitConfiguration();

    /**
     * Возвращает содержимое файла в виде Map, где ключ — это путь к значению в YAML,
     * а значение — соответствующая строка или список строк.
     *
     * @return Map<String, String>, представляющая содержимое YAML-файла.
     */
    Map<String, String> getFileContent();

    /**
     * Возвращает объект YamlFile, связанный с текущей конфигурацией.
     * Использует метод getPlugin() для получения плагина и getFileName() для получения имени файла.
     * Если плагин не задан (null), выбрасывает исключение NullPointerException.
     *
     * @return Объект YamlFile, представляющий файл конфигурации.
     * @throws NullPointerException если плагин не задан (null).
     */
    default BukkitConfig getFile() {
        Objects.requireNonNull(getPlugin(), "plugin is null");

        FileRepository fileRepository = FileRepository.of(getPlugin());

        try {
            return fileRepository.getByName(getFileName());
        } catch (FileNotFoundException exception) {
            ConsoleLogger.warn("baselibrary",  "Couldn't get %s file in %s plugin repository, reason %s ", getFileName(),  getPlugin().getName(), exception.getMessage());
            throw new RuntimeException(exception.getMessage());
        }
    }

    /**
     * Возвращает имя файла конфигурации.
     *
     * @return Имя файла конфигурации в виде строки.
     */
    String getFileName();

    /**
     * Загружает данные из YAML-файла по указанному ключу конфигурации.
     * Очищает текущее содержимое Map, полученное через getFileContent(),
     * и заполняет его данными из соответствующей секции YAML-файла.
     */
    default void load() {
        Map<String, String> strings = getFileContent();
        Objects.requireNonNull(strings, "File content (Map with value) is null");

        FileConfiguration config = getBukkitConfiguration();
        Objects.requireNonNull(config, "Configuration cannot be null");

        String yamlConfigurationKey = determineMainConfigKey(config);
        strings.clear();

        loadSimpleMessages(config, strings);
    }

    private void loadSimpleMessages(FileConfiguration config, Map<String, String> strings) {
        Objects.requireNonNull(config, "Configuration cannot be null");

        String yamlConfigurationKey = determineMainConfigKey(config);

        strings.clear();

        ConfigurationSection section = config.getConfigurationSection(yamlConfigurationKey);
        if (section == null) {
            ConsoleLogger.warn("baselibrary","Configuration section %s not found in yaml configuration.", yamlConfigurationKey);
            return;
        }

        final String finalConfigurationKey = yamlConfigurationKey;

        section.getKeys(true).forEach(path -> {
            String fullPath = finalConfigurationKey + "." + path;

            if (config.isList(fullPath)) {
                List<String> lines = config.getStringList(fullPath);
                strings.put(fullPath, String.join("\n", lines));
            } else if (config.isString(fullPath)) {
                strings.put(fullPath, config.getString(fullPath));
            } else {
                ConsoleLogger.warn("baselibrary", "Unsupported type for key: %s. Only lists and strings are supported.", fullPath);
            }
        });
    }

    /**
     * Определяет основной ключ конфигурации по структуре файла
     */
    default String determineMainConfigKey(FileConfiguration config) {
        String[] commonKeys = {"messages", "inventories", "translations", "settings"};
        for (String key : commonKeys) {
            if (config.contains(key)) {
                return key;
            }
        }

        Set<String> keys = config.getKeys(false);
        if (!keys.isEmpty()) {
            return keys.iterator().next();
        }

        return "";
    }

    /**
     * Возвращает объект FileOperations, который предоставляет операции для работы с содержимым файла.
     * Использует Map, возвращаемый методом getFileContent(), для инициализации.
     *
     * @return Объект FileOperations для работы с содержимым файла.
     */
    ConfigOperations getFileOperations();

    /**
     * Возвращает объект Plugin, связанный с текущей конфигурацией.
     *
     * @return Объект Plugin, представляющий плагин, к которому относится конфигурация.
     */
    NPlugin getPlugin();
}
