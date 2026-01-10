package ru.nilsson03.library.bukkit.util;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public final class Parameter {

    private final Object value;

    /**
     * Создает параметр с автоматическим определением типа.
     *
     * @param value Значение параметра. Поддерживаются строки, числа, boolean и списки.
     * @throws IllegalArgumentException Если тип значения не поддерживается.
     */
    public Parameter(Object value) {
        if (value == null) {
            ConsoleLogger.debug("baselibrary", "Parameter value is null");
            throw new IllegalArgumentException("Parameter value cannot be null");
        }

        if (!validateValue(value)) {
            ConsoleLogger.debug("baselibrary", "Parameter can only contain strings, numbers, boolean values, lists, or ConfigurationSection. Provided: %s", value.getClass().getName());
            throw new IllegalArgumentException("Unsupported parameter type: " + value.getClass().getName());
        }

        this.value = value;
    }

    /**
     * Возвращает значение параметра.
     *
     * @return Значение параметра.
     */
    public Object getRawValue() {
        return value;
    }

    /**
     * Возвращает значение параметра в указанном типе.
     *
     * @param targetType Класс целевого типа.
     * @return Значение параметра, приведенное к целевому типу.
     * @throws IllegalArgumentException Если приведение типа невозможно.
     */
    public <R> R getValueAs(Class<R> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isInstance(value)) {
            return targetType.cast(value);
        }

        try {
            if (targetType == Integer.class || targetType == int.class) {
                return (R) parseInt();
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return (R) parseBoolean();
            } else if (targetType == Long.class || targetType == long.class) {
                return (R) parseLong();
            } else if (targetType == Double.class || targetType == double.class) {
                return (R) parseDouble();
            } else if (targetType == Float.class || targetType == float.class) {
                return (R) parseFloat();
            } else if (targetType == String.class) {
                return (R) value.toString();
            } else if (List.class.isAssignableFrom(targetType)) {
                return (R) parseList();
            } else if (Map.class.isAssignableFrom(targetType)) {
                return (R) parseMap();
            } else if (ConfigurationSection.class.isAssignableFrom(targetType)) {
                if (value instanceof ConfigurationSection) {
                    return (R) value;
                }
            }
        } catch (Exception e) {
            ConsoleLogger.debug("baselibrary", "Failed to convert parameter value to %s: %s", targetType.getName(), e.getMessage());
            throw new IllegalArgumentException("Cannot convert value to " + targetType.getName(), e);
        }

        ConsoleLogger.debug("baselibrary", "Unsupported target type %s when converting to Parameter", targetType.getName());
        throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
    }

    private List<String> parseList() {
        String value = getValueAs(String.class);
        if (value.startsWith("[")) value = value.substring(1);
        if (value.endsWith("]")) value = value.substring(0, value.length() - 1);
        return new ArrayList<>(Arrays.asList(value.split(", ")));
    }

    private Map<String, Object> parseMap() {
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        if (value instanceof ConfigurationSection) {
            return ((ConfigurationSection) value).getValues(false);
        }
        throw new IllegalArgumentException("Cannot convert to Map");
    }

    private Float parseFloat() {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return Float.parseFloat(value.toString());
    }

    /**
     * Возвращает значение параметра в указанном типе или значение по умолчанию.
     *
     * @param targetType Класс целевого типа.
     * @param defaultValue Значение по умолчанию.
     * @return Значение параметра или defaultValue, если приведение невозможно.
     */
    public <R> R getValueAsOrDefault(Class<R> targetType, R defaultValue) {
        try {
            return getValueAs(targetType);
        } catch (IllegalArgumentException e) {
            return defaultValue;
        }
    }

    private Integer parseInt() {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    private Boolean parseBoolean() {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(value.toString());
    }

    private Long parseLong() {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    private Double parseDouble() {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    private boolean validateValue(Object value) {
        return value instanceof String ||
                value instanceof Number ||
                value instanceof Boolean ||
                value instanceof List ||
                value instanceof Map ||
                value instanceof ConfigurationSection;
    }

    private static Parameter fromEmptyString() {
        return new Parameter("");
    }

    /**
     * Создает параметр из строки с автоматическим определением типа.
     *
     * @param value Строковое значение параметра.
     * @return Новый экземпляр Parameter.
     */
    public static Parameter fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }

        if (value.isEmpty()) {
            return fromEmptyString();
        }

        try {
            if (value.contains(".")) {
                return new Parameter(Double.parseDouble(value));
            } else {
                return new Parameter(Long.parseLong(value));
            }
        } catch (NumberFormatException e1) {
            if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                return new Parameter(Boolean.parseBoolean(value));
            }
            return new Parameter(value);
        }
    }

    /**
     * Создает параметр из ConfigurationSection.
     *
     * @param section ConfigurationSection для создания параметра.
     * @return Новый экземпляр Parameter.
     */
    public static Parameter fromConfigurationSection(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("ConfigurationSection cannot be null");
        }
        return new Parameter(section);
    }
}
