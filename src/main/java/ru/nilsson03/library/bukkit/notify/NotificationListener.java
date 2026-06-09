package ru.nilsson03.library.bukkit.notify;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.bukkit.notify.pending.PendingNotification;
import ru.nilsson03.library.bukkit.notify.pending.PendingNotificationRegistry;

@AllArgsConstructor
public class NotificationListener implements Listener  {

    private final PlayerNotificationService playerNotificationService;
    private final PendingNotificationRegistry pendingNotificationRegistry;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        UUID playerId = player.getUniqueId();
        Map<String, PendingNotification> pendings = playerNotificationService.getPendingNotifications().get(playerId);

        if (pendings == null) return;

        pendings.forEach((notifId, data) -> {
            if (!data.canShow()) {
                if (data.getOnExpireKey() != null) {
                    pendingNotificationRegistry.getOnExpire(data.getOnExpireKey()).run();
                }
                return;
            }

            if (data.getOnCreateKey() != null) {
                pendingNotificationRegistry.getOnCreate(data.getOnCreateKey()).run();
            }
            
            data.markAsShown();

            if (data.getRemainingMillis() > 0) {
                playerNotificationService.getScheduler().schedule(() -> {
                    if (data.getOnExpireKey() != null) {
                        pendingNotificationRegistry.getOnExpire(data.getOnExpireKey()).run();
                    }
                }, data.getRemainingMillis(), TimeUnit.MILLISECONDS);
            }
        });

        playerNotificationService.getPendingNotifications().remove(playerId);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        UUID playerId = player.getUniqueId();

        PlayerNotification notification = playerNotificationService.getPlayerNotifications().remove(playerId);
        if (notification != null) {
            notification.cancelAll();
        }
    }
}
