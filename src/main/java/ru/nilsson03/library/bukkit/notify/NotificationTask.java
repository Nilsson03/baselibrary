package ru.nilsson03.library.bukkit.notify;

import java.util.concurrent.ScheduledFuture;

public class NotificationTask {
    private final ScheduledFuture<?> task;
    private final Runnable onCancel;

    public NotificationTask(ScheduledFuture<?> task, Runnable onCancel) {
        this.task = task;
        this.onCancel = onCancel;
    }

    public void cancel() {
        task.cancel(false);
        if (onCancel != null) {
            onCancel.run();
        }
    }
}
