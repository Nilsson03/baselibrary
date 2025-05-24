package ru.nilsson03.library.bukkit.notify;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Контейнер для уведомлений конкретного игрока
 */
public class PlayerNotification {
    private final UUID playerId;
    private final Map<String, NotificationTask> activeNotifications = new ConcurrentHashMap<>();

    public PlayerNotification(UUID playerId) {
        this.playerId = playerId;
    }

    public boolean contains(String notificationId) {
        return activeNotifications.containsKey(notificationId);
    }

    public void add(String notificationId, ScheduledFuture<?> task, Runnable onCancel) {
        activeNotifications.put(notificationId, new NotificationTask(task, onCancel));
    }

    public void remove(String notificationId) {
        NotificationTask task = activeNotifications.remove(notificationId);
        if (task != null) {
            task.cancel();
        }
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public void cancelAll() {
        activeNotifications.values().forEach(NotificationTask::cancel);
        activeNotifications.clear();
    }

    public boolean isEmpty() {
        return activeNotifications.isEmpty();
    }
}
