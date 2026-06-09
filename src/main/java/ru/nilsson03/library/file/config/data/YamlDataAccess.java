package ru.nilsson03.library.file.config.data;

import ru.nilsson03.library.file.config.YamlCache;
import ru.nilsson03.library.file.config.util.YamlTypeConverter;

import java.util.List;
import java.util.Map;

public interface YamlDataAccess {

    YamlCache getCache();

    Object get(String path);
    Object get(String path, Object defaultValue);

    default String getString(String path) { return getString(path, null); }
    default String getString(String path, String defaultValue) {
        return YamlTypeConverter.getString(get(path), defaultValue);
    }

    default int getInt(String path) { return getInt(path, 0); }
    default int getInt(String path, int defaultValue) {
        return YamlTypeConverter.getInt(get(path), defaultValue);
    }

    default long getLong(String path) { return getLong(path, 0L); }
    default long getLong(String path, long defaultValue) {
        return YamlTypeConverter.getLong(get(path), defaultValue);
    }

    default double getDouble(String path) { return getDouble(path, 0.0); }
    default double getDouble(String path, double defaultValue) {
        return YamlTypeConverter.getDouble(get(path), defaultValue);
    }

    default float getFloat(String path) { return getFloat(path, 0.0f); }
    default float getFloat(String path, float defaultValue) {
        return YamlTypeConverter.getFloat(get(path), defaultValue);
    }

    default boolean getBoolean(String path) { return getBoolean(path, false); }
    default boolean getBoolean(String path, boolean defaultValue) {
        return YamlTypeConverter.getBoolean(get(path), defaultValue);
    }

    default List<String> getStringList(String path) {
        return YamlTypeConverter.getStringList(get(path));
    }

    default List<Integer> getIntList(String path) {
        return YamlTypeConverter.getIntList(get(path));
    }

    default List<Double> getDoubleList(String path) {
        return YamlTypeConverter.getDoubleList(get(path));
    }

    default List<Boolean> getBooleanList(String path) {
        return YamlTypeConverter.getBooleanList(get(path));
    }

    default List<Map<String, Object>> getMapList(String path) {
        return YamlTypeConverter.getMapList(get(path));
    }

    default YamlSection getSection(String path) {
        return new YamlSection(getCache(), path);
    }

    void set(String path, Object value);
    boolean contains(String path);
    void remove(String path);
}