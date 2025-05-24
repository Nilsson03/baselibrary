package ru.nilsson03.library.bukkit.notify;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Сервис для управления временными уведомлениями игроков
 * с использованием современных Java API
 */
public class PlayerNotificationService {
    private final Map<UUID, PlayerNotification> playerNotifications = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public PlayerNotificationService() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "NotificationScheduler"));
    }

    public NotificationBuilder createBuilder() {
        return new NotificationBuilder(this, scheduler);
    }

    public Map<UUID, PlayerNotification> getPlayerNotifications() {
        return playerNotifications;
    }

    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            scheduler.shutdownNow();
            playerNotifications.values().forEach(PlayerNotification::cancelAll);
            playerNotifications.clear();
        }
    }

    void cleanupNotification(UUID playerId, String notificationId) {
        PlayerNotification notification = playerNotifications.get(playerId);
        if (notification != null) {
            notification.remove(notificationId);
            if (notification.isEmpty()) {
                playerNotifications.remove(playerId);
            }
        }
    }
}
