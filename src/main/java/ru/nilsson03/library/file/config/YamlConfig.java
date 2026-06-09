package ru.nilsson03.library.file.config;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.electronwill.nightconfig.core.file.FileConfig;
import com.electronwill.nightconfig.yaml.YamlFormat;

import lombok.Getter;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.file.config.data.ConfigOperations;
import ru.nilsson03.library.file.config.data.YamlSection;

public class YamlConfig {

    private final NPlugin plugin;
    private final FileConfig config;
    @Getter
    private final YamlCache cache;
    @Getter
    private final ConfigOperations operations;

    public YamlConfig(NPlugin plugin,
            Path folder,
            String fileName) {
        this.plugin = plugin;
        Objects.requireNonNull(folder, "Folder must not be null");
        validateFileName(fileName);
        this.config = FileConfig.of(
                folder.resolve(fileName).toFile(),
                YamlFormat.defaultInstance());
        this.cache = new YamlCache(config);
        this.cache.load();
        this.operations = new ConfigOperations(cache);
    }

    private void validateFileName(String fileName) {
        if (fileName == null || fileName.contains("..") || fileName.startsWith("/")) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(),
                    plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }
    }

    public String getName() {
        String fullName = config.getFile().getName();
        int dotIndex = fullName.lastIndexOf('.');
        if (dotIndex == -1) {
            return fullName;
        }
        return fullName.substring(0, dotIndex);
    }

    public File getFile() {
        return config.getFile();
    }

    public Object get(String path) {
        return operations.get(path);
    }

    public Object get(String path, Object defaultValue) {
        return operations.get(path, defaultValue);
    }

    public String getString(String path) {
        return operations.getString(path);
    }

    public String getString(String path, String defaultValue) {
        return operations.getString(path, defaultValue);
    }

    public int getInt(String path) {
        return operations.getInt(path);
    }

    public int getInt(String path, int defaultValue) {
        return operations.getInt(path, defaultValue);
    }

    public long getLong(String path) {
        return operations.getLong(path);
    }

    public long getLong(String path, long defaultValue) {
        return operations.getLong(path, defaultValue);
    }

    public double getDouble(String path) {
        return operations.getDouble(path);
    }

    public double getDouble(String path, double defaultValue) {
        return operations.getDouble(path, defaultValue);
    }

    public float getFloat(String path) {
        return operations.getFloat(path);
    }

    public float getFloat(String path, float defaultValue) {
        return operations.getFloat(path, defaultValue);
    }

    public boolean getBoolean(String path) {
        return operations.getBoolean(path);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return operations.getBoolean(path, defaultValue);
    }

    public List<String> getStringList(String path) {
        return operations.getStringList(path);
    }

    public List<Integer> getIntList(String path) {
        return operations.getIntList(path);
    }

    public List<Double> getDoubleList(String path) {
        return operations.getDoubleList(path);
    }

    public List<Boolean> getBooleanList(String path) {
        return operations.getBooleanList(path);
    }

    public List<Map<String, Object>> getMapList(String path) {
        return operations.getMapList(path);
    }

    public YamlSection getSection(String path) {
        return operations.getSection(path);
    }

    public void set(String path, Object value) {
        operations.set(path, value);
    }

    public boolean contains(String path) {
        return operations.contains(path);
    }

    public void remove(String path) {
        operations.remove(path);
    }

    public void save() {
        operations.save();
    }

    public void reload() {
        operations.reload();
    }

    public void close() {
        operations.close();
    }
}
