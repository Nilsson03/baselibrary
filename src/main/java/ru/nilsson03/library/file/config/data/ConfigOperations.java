package ru.nilsson03.library.file.config.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nilsson03.library.file.config.YamlCache;

@AllArgsConstructor
public class ConfigOperations implements YamlDataAccess {

    @Getter
    private final YamlCache cache;

    @Override
    public Object get(String path) {
        return cache.get(path);
    }

    @Override
    public Object get(String path, Object defaultValue) {
        if (cache.contains(path)) return  cache.get(path);
        return defaultValue;
    }

    public void set(String path, Object value) {
        cache.set(path, value);
    }

    public boolean contains(String path) {
        return cache.contains(path);
    }

    public void remove(String path) {
        cache.remove(path);
    }

    public void save() { cache.save(); }
    public void reload() { cache.reload(); }
    public void close() { cache.close(); }
}