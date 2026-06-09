package ru.nilsson03.library.file.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class YamlCache {

    private final Map<String, Object> content;
    private final FileConfig config;

    public YamlCache(FileConfig config) {
        Objects.requireNonNull(config, "Config must not be null");
        this.config = config;
        this.content = new HashMap<>();
    }

    public Object get(String path) {
        if (path == null || path.isEmpty()) return null;
        String[] parts = path.split("\\.");
        Object current = content.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            if (current == null) return null;
            current = navigate(current, parts[i]);
        }
        return current;
    }

    public Object get(String path, Object defaultValue) {
        Object value = get(path);
        return value != null ? value : defaultValue;
    }

    private Object navigate(Object current, String key) {
        if (current instanceof Config) {
            return ((Config) current).get(key);
        } else if (current instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) current;
            Object value = map.get(key);
            if (value == null) {
                Integer asInt = tryParseInt(key);
                if (asInt != null) value = map.get(asInt);
            }
            return value;
        }
        return null;
    }

    private Integer tryParseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public Object set(String path, Object value) {
        return content.put(path, value);
    }

    public boolean contains(String path) {
        return get(path) != null;
    }

    public Object remove(String path) {
        return content.remove(path);
    }

    public Set<String> getAllKeys() {
        return content.keySet();
    }

    /**
     * Загрузка содержимого файла
     */
    public void load() {
        config.load();
        content.putAll(config.valueMap());
    }

    /**
     * Запись содержимого кэша в файл
     */
    public void save() {
        config.clear();
        content.forEach(config::set);
        config.save();
    }

    /**
     * Закрытие файла (БЕЗ СОХРАНЕНИЯ)
     */
    public void close() {
        config.close();
    }

    /**
     * Перезагрузка конфигурации
     */
    public void reload() {
        content.clear();
        config.load();
        content.putAll(config.valueMap());
    }

    public Map<String, Object> getContent() {
        return content;
    }
}
