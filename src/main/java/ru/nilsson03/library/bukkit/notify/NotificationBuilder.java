package ru.nilsson03.library.bukkit.notify;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class NotificationBuilder {
    private final PlayerNotificationService service;
    private final ScheduledExecutorService scheduler;

    private UUID playerId;
    private String notificationId;
    private Duration delay = Duration.ofSeconds(30);
    private Runnable onCreateAction;
    private Runnable onExpireAction;
    private Runnable onCancelAction;

    public NotificationBuilder(PlayerNotificationService service, ScheduledExecutorService scheduler) {
        this.service = service;
        this.scheduler = scheduler;
    }

    public NotificationBuilder forPlayer(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "Player ID cannot be null");
        return this;
    }

    public NotificationBuilder withId(String notificationId) {
        this.notificationId = Objects.requireNonNull(notificationId, "Notification ID cannot be null");
        return this;
    }

    public NotificationBuilder expiresAfter(Duration delay) {
        this.delay = Objects.requireNonNull(delay, "Delay cannot be null");
        return this;
    }

    public NotificationBuilder withCreateAction(Runnable action) {
        this.onCreateAction = action;
        return this;
    }

    public NotificationBuilder withExpireAction(Runnable action) {
        this.onExpireAction = action;
        return this;
    }

    public NotificationBuilder withCancelAction(Runnable action) {
        this.onCancelAction = action;
        return this;
    }

    public CompletableFuture<Boolean> schedule() {
        validateParameters();

        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerNotification notification = service.getPlayerNotifications()
                        .computeIfAbsent(playerId, k -> new PlayerNotification(playerId));

                if (notification.contains(notificationId)) {
                    return false;
                }

                executeCreateAction();
                ScheduledFuture<?> task = scheduleExpiration(notification);
                notification.add(notificationId, task, onCancelAction);

                return true;
            } catch (Exception e) {
                logError(e);
                return false;
            }
        });
    }

    private void validateParameters() {
        if (playerId == null) {
            throw new IllegalStateException("Player ID must be set");
        }
        if (notificationId == null) {
            throw new IllegalStateException("Notification ID must be set");
        }
    }

    private void executeCreateAction() {
        if (onCreateAction != null) {
            onCreateAction.run();
        }
    }

    private ScheduledFuture<?> scheduleExpiration(PlayerNotification notification) {
        return scheduler.schedule(() -> {
            if (onExpireAction != null) {
                onExpireAction.run();
            }
            service.cleanupNotification(playerId, notificationId);
        }, delay.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void logError(Exception e) {
        ConsoleLogger.warn("baselibrary",
                "Failed to create notification for %s: %s",
                playerId, e.getMessage());
    }
}
