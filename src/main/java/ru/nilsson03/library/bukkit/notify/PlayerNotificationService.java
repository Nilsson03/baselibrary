package ru.nilsson03.library.bukkit.notify;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.Getter;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.notify.pending.PendingNotification;
import ru.nilsson03.library.bukkit.notify.pending.PendingNotificationRegistry;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class PlayerNotificationService {

    private static final Map<NPlugin, PlayerNotificationService> initializeMap = new ConcurrentHashMap<>();
    @Getter
    private final PendingNotificationRegistry pendingRegistry;

    @Getter
    private final Map<UUID, PlayerNotification> playerNotifications = new ConcurrentHashMap<>();
    @Getter
    private final Map<UUID, Map<String, PendingNotification>> pendingNotifications = new ConcurrentHashMap<>();

    private final NPlugin plugin;
    private final File storageFile;
    private final Gson gson;

    @Getter
    private final ScheduledExecutorService scheduler;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public PlayerNotificationService(NPlugin plugin) {
        if (initializeMap.containsKey(plugin)) {
            throw new IllegalStateException("Already initialized for plugin " + plugin.getName());
        }
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.pendingRegistry = new PendingNotificationRegistry();
        this.storageFile = new File(plugin.getDataFolder(), "pending_notifications.json");
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "NotificationScheduler"));
        initializeMap.put(plugin, this);
        Bukkit.getPluginManager().registerEvents(new NotificationListener(this, pendingRegistry), plugin);
        load();
    }

    public NotificationBuilder createBuilder() {
        return new NotificationBuilder(this, scheduler);
    }

    public void shutdown() {
        if (isShutdown.compareAndSet(false, true)) {
            scheduler.shutdownNow();
            playerNotifications.values().forEach(PlayerNotification::cancelAll);
            playerNotifications.clear();
        }

        try (Writer writer = new FileWriter(storageFile)) {
            Map<String, Map<String, PendingNotification>> serializable = new ConcurrentHashMap<>();
            pendingNotifications.forEach((uuid, map) ->
                    serializable.put(uuid.toString(), map));
            gson.toJson(serializable, writer);
        } catch (IOException e) {
            ConsoleLogger.warn("baselibrary", "Failed to save notifications: " + e.getMessage());
        }
    }

    private void load() {
        if (!storageFile.exists()) return;

        try (Reader reader = new FileReader(storageFile)) {
            Map<String, Map<String, PendingNotification>> loaded = gson.fromJson(
                    reader,
                    new com.google.gson.reflect.TypeToken<Map<String, Map<String, PendingNotification>>>(){}.getType()
            );

            if (loaded == null) return;

            int restored = 0;
            int expired = 0;
            int shown = 0;

            for (var entry : loaded.entrySet()) {
                UUID playerId = UUID.fromString(entry.getKey());
                for (var notifEntry : entry.getValue().entrySet()) {
                    PendingNotification data = notifEntry.getValue();

                    if (data.isExpired()) {
                        expired++;
                        continue;
                    }

                    if (data.isOnce() && data.isShown()) {
                        shown++;
                        continue;
                    }

                    pendingNotifications
                            .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                            .put(notifEntry.getKey(), data);
                    restored++;
                }
            }

            ConsoleLogger.info("baselibrary",
                    "Loaded %d pending notifications (%d expired, %d already shown)",
                    restored, expired, shown);
        } catch (Exception e) {
            ConsoleLogger.warn("baselibrary", "Failed to load notifications: " + e.getMessage());
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

    public void addPendingNotification(PendingNotification pendingNotification) {
        UUID playerId = pendingNotification.getPlayerId();
        String notificationId = pendingNotification.getNotificationId();

        pendingNotifications
                .computeIfAbsent(playerId, k -> new ConcurrentHashMap<>())
                .put(notificationId, pendingNotification);
    }
}
