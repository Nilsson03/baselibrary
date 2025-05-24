package ru.nilsson03.library.bukkit.file.configuration;

import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.FileHelper;
import ru.nilsson03.library.bukkit.util.Parameter;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ParameterFile {
    private final NPlugin plugin;
    private final String fileName;
    private final FileConfiguration fileConfiguration;
    private final Map<String, Parameter> parameters = new ConcurrentHashMap<>();

    public static ParameterFile of(NPlugin plugin, String fileName) {
        FileConfiguration fileConfiguration = FileHelper.loadConfiguration(plugin, fileName);
        return new ParameterFile(plugin, fileName, fileConfiguration);
    }

    protected ParameterFile(NPlugin plugin, String fileName, FileConfiguration fileConfiguration) {
        if (plugin == null) {
            ConsoleLogger.debug("baselibrary", "Plugin cannot be null, class %s, plugin", getClass().getName());
            throw new IllegalArgumentException("plugin cannot be null, class " + getClass().getName());
        }
        if (fileName == null || fileName.isEmpty()) {
            ConsoleLogger.debug(plugin, "File name cannot be null or empty, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File name cannot be null or empty, class " + getClass().getName());
        }

        if (fileConfiguration == null) {
            ConsoleLogger.debug(plugin, "File configuration cannot be null, class %s, plugin %s", getClass().getName(), plugin.getName());
            throw new IllegalArgumentException("File configuration cannot be null, class " + getClass().getName());
        }
        this.plugin = plugin;
        this.fileName = fileName;
        this.fileConfiguration = fileConfiguration;
        loadParameters();
    }

    private void loadParameters() {
        for (String key : fileConfiguration.getKeys(true)) {
            String value = fileConfiguration.getString(key);
            parameters.put(key, Parameter.fromString(value));
        }
    }

    public Optional<Parameter> getParameter(String key) {
        return Optional.ofNullable(parameters.get(key));
    }

    public <T> T getValueAs(String key, Class<T> type) {
        Parameter param = getParameter(key)
                .orElseThrow( () -> new IllegalArgumentException("Parameter not found: " + key + " in " + fileName));
        return param != null ? param.getValueAs(type) : null;
    }

    public Parameter getParameterOrDefault(String key, String defaultValue) {
        return parameters.getOrDefault(key, Parameter.fromString(defaultValue));
    }

    public NPlugin getPlugin() {
        return plugin;
    }
}
