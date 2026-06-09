package ru.nilsson03.library.bukkit.notify.pending;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Отложенное уведомление (будет показано при входе игрока)
 * Содержит все данные для восстановления после перезагрузки
 */
@Getter
@Builder
public class PendingNotification implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID playerId;
    private final String notificationId;
    private final Duration delay;
    private final String onCreateKey;
    private final String onExpireKey;
    private final String onCancelKey;

    @Singular("metadata")
    private final Map<String, String> metadata;

    private final long createdAt = System.currentTimeMillis();

    public long getDelayMillis() {
        return delay.toMillis();
    }

    @Builder.Default
    private final boolean once = false;

    @Builder.Default
    private boolean shown = false;

    public long getRemainingMillis() {
        long elapsed = System.currentTimeMillis() - createdAt;
        return Math.max(0, getDelayMillis() - elapsed);
    }

    public boolean isExpired() {
        return getRemainingMillis() <= 0;
    }

    public boolean canShow() {
        if (isExpired()) return false;
        if (once && shown) return false;
        return true;
    }

    public void markAsShown() {
        this.shown = true;
    }
}