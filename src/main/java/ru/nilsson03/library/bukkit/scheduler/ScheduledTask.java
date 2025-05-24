package ru.nilsson03.library.bukkit.scheduler;

import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class ScheduledTask {
    private final UUID taskId;
    private final BukkitTask bukkitTask;
    private final long executionTime;
    private final boolean repeating;

    public ScheduledTask(UUID taskId, BukkitTask bukkitTask, long executionTime, boolean repeating) {
        this.taskId = taskId;
        this.bukkitTask = bukkitTask;
        this.executionTime = executionTime;
        this.repeating = repeating;
    }

    public Duration getRemainingTime() {
        long remaining = executionTime - System.currentTimeMillis();
        return Duration.ofMillis(Math.max(0, remaining));
    }

    public Instant getExecutionTime() {
        return Instant.ofEpochMilli(executionTime);
    }

    public UUID getTaskId() {
        return taskId;
    }

    public boolean isRepeating() {
        return repeating;
    }

    public boolean isCompleted() {
        return !repeating && !bukkitTask.isCancelled()
                && System.currentTimeMillis() >= executionTime;
    }

    public void cancel() {
        bukkitTask.cancel();
    }
}
