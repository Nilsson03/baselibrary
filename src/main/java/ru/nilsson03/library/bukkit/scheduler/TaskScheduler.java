package ru.nilsson03.library.bukkit.scheduler;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TaskScheduler {
    private final JavaPlugin plugin;
    private final Map<UUID, ScheduledTask> tasks = new ConcurrentHashMap<>();

    public TaskScheduler(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public TaskBuilder createTask(Runnable task) {
        return new TaskBuilder(this, plugin, task);
    }

    synchronized void registerTask(ScheduledTask task) {
        tasks.put(task.getTaskId(), task);
    }

    synchronized void unregisterTask(UUID taskId) {
        tasks.remove(taskId);
    }

    public boolean isRunning(UUID taskId) {
        Optional<ScheduledTask> optionalScheduledTask = getTask(taskId);
        if (optionalScheduledTask.isPresent()) {
            ScheduledTask task = optionalScheduledTask.get();
            return !task.isCompleted();
        }
        return false;
    }

    public Optional<ScheduledTask> getTask(UUID taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    public boolean cancelTask(UUID taskId) {
        ScheduledTask task = tasks.remove(taskId);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }

    public void shutdown() {
        new ArrayList<>(tasks.keySet()).forEach(this::cancelTask);
    }
}
