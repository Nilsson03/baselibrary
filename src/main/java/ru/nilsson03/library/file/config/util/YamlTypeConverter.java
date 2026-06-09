package ru.nilsson03.library.file.config.util;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;

@UtilityClass
public final class YamlTypeConverter {

    public static String getString(Object value, String defaultValue) {
        if (value == null) return defaultValue;
        return value.toString();
    }

    public static int getInt(Object value, int defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static long getLong(Object value, long defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static double getDouble(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static float getFloat(Object value, float defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    public static boolean getBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String lower = ((String) value).toLowerCase();
            if (lower.equals("true") || lower.equals("yes") || lower.equals("1")) return true;
            if (lower.equals("false") || lower.equals("no") || lower.equals("0")) return false;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(Object value) {
        return (List<String>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Integer> getIntList(Object value) {
        return (List<Integer>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Double> getDoubleList(Object value) {
        return (List<Double>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Boolean> getBooleanList(Object value) {
        return (List<Boolean>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> getMapList(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getSection(Object value) {
        return (Map<String, Object>) value;
    }
}