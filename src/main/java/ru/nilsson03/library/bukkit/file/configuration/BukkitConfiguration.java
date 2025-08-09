package ru.nilsson03.library.bukkit.file.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

        strings.clear();

        loadSimpleMessages(config, strings);
    }

    private void loadSimpleMessages(FileConfiguration config, Map<String, String> strings) {
        Objects.requireNonNull(config, "Configuration cannot be null");
        strings.clear();

        for (String key : config.getKeys(true)) {
            if (config.isConfigurationSection(key)) {
                continue;
            }

            String value;
            if (config.isList(key)) {
                List<String> lines = config.getStringList(key);
                value = String.join("\n", lines);
            } else if (config.isString(key)) {
                value = config.getString(key);
            } else if (config.isBoolean(key) || config.isInt(key) || config.isDouble(key)) {
                value = String.valueOf(config.get(key));
            } else {
                ConsoleLogger.warn("baselibrary", "Unsupported type for key: %s. Supported: lists, strings, numbers, booleans.", key);
                continue;
            }

            strings.put(key, value);
        }
    }

    /**
     * Возвращает объект ConfigOperations, который предоставляет операции для работы с содержимым файла.
     * Использует Map, возвращаемый методом getFileContent(), для инициализации.
     *
     * @return Объект ConfigOperations для работы с содержимым файла.
     */
    ConfigOperations operations();

    /**
     * Возвращает объект Plugin, связанный с текущей конфигурацией.
     *
     * @return Объект Plugin, представляющий плагин, к которому относится конфигурация.
     */
    NPlugin getPlugin();
}
