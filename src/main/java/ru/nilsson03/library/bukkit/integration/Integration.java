package ru.nilsson03.library.bukkit.integration;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class Integration {

    private final JavaPlugin plugin;

    public Integration(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean checkDependency(Method method) {
        PluginDependency annotation = method.getAnnotation(PluginDependency.class);
        if (annotation == null) return true;

        return isVersionCompatible(annotation.name(), annotation.minVersion());
    }

    public <T> T executeOrSkip(Supplier<T> action, Method method, T defaultValue) {
        if (checkDependency(method)) {
            return action.get();
        }
        return defaultValue;
    }

    public void executeOrSkip(Runnable action, Method method) {
        if (checkDependency(method)) {
            action.run();
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
