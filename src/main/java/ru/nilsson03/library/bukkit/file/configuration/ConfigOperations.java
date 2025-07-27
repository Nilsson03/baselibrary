package ru.nilsson03.library.bukkit.file.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.*;
import java.util.stream.Collectors;

public class ConfigOperations {

    private final Map<String, String> map;

    public ConfigOperations(@NotNull Map<String, String> map) {
        this.map = Objects.requireNonNull(map, "Map cannot be null");
    }

    /**
     * Получает значение типа boolean по указанному пути.
     *
     * @param path Путь к значению.
     * @return Значение boolean или значение по умолчанию (false).
     */
    public boolean getBoolean(@NotNull String path) {
        return getBoolean(path, false);
    }

    /**
     * Получает значение типа boolean по указанному пути с возможностью указать значение по умолчанию.
     *
     * @param path       Путь к значению.
     * @param defValue   Значение по умолчанию.
     * @return Значение boolean или значение по умолчанию.
     */
    public boolean getBoolean(@NotNull String path, boolean defValue) {
        String value = map.getOrDefault(path, null);
        try {
            return Boolean.parseBoolean((String) value);
        } catch (Exception e) {
            ConsoleLogger.warn("baselibrary", "Could not parse boolean value for path: " + path);
            return defValue;
        }
    }

    /**
     * Получает значение типа int по указанному пути.
     *
     * @param path Путь к значению.
     * @return Значение int или значение по умолчанию (0).
     */
    public int getInt(@NotNull String path) {
        return getInt(path, 0);
    }

    /**
     * Получает значение типа int по указанному пути с возможностью указать значение по умолчанию.
     *
     * @param path       Путь к значению.
     * @param defValue   Значение по умолчанию.
     * @return Значение int или значение по умолчанию.
     */
    public int getInt(@NotNull String path, int defValue) {
        String value = map.getOrDefault(path, null);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            ConsoleLogger.warn("baselibrary", "Could not parse int value for path: " + path);
            return defValue;
        }
    }

    /**
     * Получает значение типа double по указанному пути.
     *
     * @param path Путь к значению.
     * @return Значение double или значение по умолчанию (0.0D).
     */
    public double getDouble(@NotNull String path) {
        return getDouble(path, 0.0D);
    }

    /**
     * Получает значение типа double по указанному пути с возможностью указать значение по умолчанию.
     *
     * @param path       Путь к значению.
     * @param defValue   Значение по умолчанию.
     * @return Значение double или значение по умолчанию.
     */
    public double getDouble(@NotNull String path, double defValue) {
        String value = map.getOrDefault(path, null);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            ConsoleLogger.warn("baselibrary", "Could not parse double value for path: " + path);
            return defValue;
        }
    }

    /**
     * Получает список строк по указанному пути с возможностью замены данных.
     *
     * @param path          Путь к значению.
     * @param replacesData  Массив ReplaceData для замены данных.
     * @return Список строк или пустой список при ошибке.
     */
    public List<String> getList(@NotNull String path, ReplaceData... replacesData) {
        String value = map.getOrDefault(path, null);
        if (value == null) {
            ConsoleLogger.warn("baselibrary", "Could not parse list value for path: " + path + ". Returning default message.");
            return List.of(defaultErrorMessage());
        }

        return Arrays.stream(value.split("\n"))
                .map(String::trim)
                .map(line -> {
                    if (line.startsWith("[") && line.endsWith("]") &&
                            line.indexOf('[', 1) == -1 && line.lastIndexOf(']', line.length() - 2) == -1) {
                        return line.substring(1, line.length() - 1);
                    }
                    return line;
                })
                .filter(line -> !line.isEmpty())
                .map(line -> applyReplaces(line, replacesData))
                .map(UniversalTextApi::colorize)
                .collect(Collectors.toList());
    }

    public String getString(@NotNull String path, ReplaceData... replacesData) {
        return getString(path, defaultErrorMessage(), replacesData);
    }

    public String getString(@NotNull String path, @Nullable String defValue, ReplaceData... replacesData) {
        String value = map.getOrDefault(path, null);
        if (value == null) {
            ConsoleLogger.warn("baselibrary", "Could not parse string value for path: " + path + ". Returning default value.");
            return defValue != null ? UniversalTextApi.colorize(defValue) : "";
        }

        String processedValue = applyReplaces(value, replacesData);
        return UniversalTextApi.colorize(processedValue);
    }

    private String applyReplaces(String text, ReplaceData... replacesData) {
        String result = text;
        for (ReplaceData replaceData : replacesData) {
            if (replaceData != null && replaceData.getKey() != null && replaceData.getObject() != null) {
                result = result.replace(replaceData.getKey(), replaceData.getObject().toString());
            }
        }
        return result;
    }

    private String defaultErrorMessage() {
        return "&cCouldn't get the value from the configuration.";
    }
}
