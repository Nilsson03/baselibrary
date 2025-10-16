package ru.nilsson03.library.bukkit.integration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class Integration {

    private final JavaPlugin plugin;

    private final Set<PluginInfo> dependencies;

    public Integration(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dependencies = new HashSet<>();
    }

    public void addDependency(PluginInfo... pluginInfos) {
        dependencies.addAll(Arrays.asList(pluginInfos));
    }

    public void checkDependencies() {
        for (PluginInfo dependency : dependencies) {
            if (!isVersionCompatible(dependency.getPluginName(), dependency.getVersion())) {
                ConsoleLogger.warn(plugin, "Dependency %s is not compatible with the current version of the plugin.", dependency.getPluginName());
                Bukkit.getPluginManager().disablePlugin(plugin);
                return;
            }
        }
    }

    public boolean isVersionCompatible(String pluginName, String minVersion) {
        Plugin target = Bukkit.getPluginManager().getPlugin(pluginName);
        if (target == null) return false;

        return VersionChecker.isCompatible(target.getDescription().getVersion(), minVersion);
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }
}
