package ru.nilsson03.library.bukkit.integration;

public class PluginInfo {

    private final String pluginName;
    private final String version;

    public PluginInfo(String pluginName, String version) {
        this.pluginName = pluginName;
        this.version = version;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getVersion() {
        return version;
    }
}
