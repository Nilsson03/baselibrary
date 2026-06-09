package ru.nilsson03.library.file.config.data;

import java.util.*;

import com.electronwill.nightconfig.core.Config;
import ru.nilsson03.library.file.config.YamlCache;

public class YamlSection implements YamlDataAccess {

    private final YamlCache cache;
    private final String prefix;

    public YamlSection(YamlCache cache, String prefix) {
        Objects.requireNonNull(cache, "Cache must not be null");
        Objects.requireNonNull(prefix, "Prefix must not be null");
        this.cache = cache;
        this.prefix = prefix;
    }

    private String fullPath(String path) {
        return prefix + "." + path;
    }

    @Override
    public Object get(String path) {
        return cache.get(fullPath(path));
    }

    @Override
    public Object get(String path, Object defaultValue) {
        return cache.get(fullPath(path), defaultValue);
    }

    @Override
    public void set(String path, Object value) {
        cache.set(fullPath(path), value);
    }

    @Override
    public boolean contains(String path) {
        return cache.contains(fullPath(path));
    }

    @Override
    public void remove(String path) {
        cache.remove(fullPath(path));
    }

    @Override
    public YamlSection getSection(String path) {
        return new YamlSection(cache, fullPath(path));
    }

    @Override
    public YamlCache getCache() {
        return cache;
    }

    public Map<String, Object> getValues() {
        Map<String, Object> result = new HashMap<>();
        Object current = cache.get(prefix);

        if (current == null) {
            return result;
        }

        if (current instanceof Config config) {
            for (Config.Entry entry : config.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}