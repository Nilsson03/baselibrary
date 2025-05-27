package ru.nilsson03.library.alt.top;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.scheduler.ScheduledTask;
import ru.nilsson03.library.bukkit.scheduler.TaskScheduler;

import java.time.Duration;

public class TopScheduler {

    private final TaskScheduler taskScheduler;
    private ScheduledTask updateTask;

    TopScheduler(NPlugin plugin) {
        this.taskScheduler = plugin.taskScheduler();
    }

    public void startUpdater(ScheduledTask updateTask) {
        this.updateTask = updateTask;
    }

    public void stopUpdater() {
        if (updateTask != null) {
            taskScheduler.cancelTask(updateTask.getTaskId());
            updateTask = null;
        }
    }

    public Duration getTimeUntilNextUpdate() {
        if (updateTask == null) {
            return Duration.ZERO;
        }
        return updateTask.getRemainingTime();
    }
}
