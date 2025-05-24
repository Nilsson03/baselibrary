package ru.nilsson03.library.bukkit.scheduler;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class TaskBuilder {
    private final TaskScheduler scheduler;
    private final JavaPlugin plugin;
    private final Runnable task;

    private Duration delay = Duration.ZERO;
    private Duration interval;
    private Consumer<Exception> errorHandler;
    private boolean async;

    public TaskBuilder(TaskScheduler scheduler, JavaPlugin plugin, Runnable task) {
        this.scheduler = Objects.requireNonNull(scheduler);
        this.plugin = Objects.requireNonNull(plugin);
        this.task = Objects.requireNonNull(task);
    }

    public TaskBuilder withDelay(Duration delay) {
        this.delay = Objects.requireNonNull(delay);
        return this;
    }

    public TaskBuilder withInterval(Duration interval) {
        this.interval = Objects.requireNonNull(interval);
        return this;
    }

    public TaskBuilder withErrorHandler(Consumer<Exception> handler) {
        this.errorHandler = handler;
        return this;
    }

    public TaskBuilder async() {
        this.async = true;
        return this;
    }

    public ScheduledTask schedule() {
        Runnable wrappedTask = createWrappedTask();
        BukkitTask bukkitTask = createBukkitTask(wrappedTask);

        return createScheduledTask(bukkitTask);
    }

    private Runnable createWrappedTask() {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                handleError(e);
            }
        };
    }

    private BukkitTask createBukkitTask(Runnable task) {
        long delayTicks = delay.toMillis() / 50;
        long intervalTicks = interval != null ? interval.toMillis() / 50 : 0;

        if (async) {
            return interval != null
                    ? plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, delayTicks, intervalTicks)
                    : plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delayTicks);
        } else {
            return interval != null
                    ? plugin.getServer().getScheduler().runTaskTimer(plugin, task, delayTicks, intervalTicks)
                    : plugin.getServer().getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    private void handleError(Exception e) {
        if (errorHandler != null) {
            try {
                errorHandler.accept(e);
            } catch (Exception ex) {
                plugin.getLogger().severe("Error in error handler: " + ex.getMessage());
            }
        } else {
            plugin.getLogger().severe("Task error: " + e.getMessage());
        }
    }

    private ScheduledTask createScheduledTask(BukkitTask bukkitTask) {
        ScheduledTask scheduledTask = new ScheduledTask(
                UUID.randomUUID(),
                bukkitTask,
                System.currentTimeMillis() + delay.toMillis(),
                interval != null
        );

        scheduler.registerTask(scheduledTask);
        return scheduledTask;
    }
}
