package ru.nilsson03.library.bukkit.entity;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.NamespacedKey;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class ManagedEntity<T extends Entity> {
    private static final Object CHUNK_LOCK = new Object();
    private static final Map<ChunkKey, Integer> FORCE_COUNTS = new java.util.HashMap<>();
    private static final Map<ChunkKey, Boolean> FORCE_PREVIOUS = new java.util.HashMap<>();
    private static final Map<Plugin, Set<ManagedEntity<?>>> REGISTRY = new IdentityHashMap<>();

    private final JavaPlugin plugin;
    private final String id;
    private final Class<T> type;
    private final Location spawnLocation;
    private final ManagedEntityOptions options;
    private final Consumer<T> configurator;
    private final NamespacedKey markerKey;
    private T entity;
    private ChunkKey forcedChunk;
    private int taskId = -1;
    private boolean removed;

    private ManagedEntity(JavaPlugin plugin, String id, Location location, Class<T> type,
                          ManagedEntityOptions options, Consumer<T> configurator) {
        if (plugin == null || id == null || id.trim().isEmpty() || location == null ||
                location.getWorld() == null || type == null) {
            throw new IllegalArgumentException("plugin, id, type and a world location are required");
        }
        this.plugin = plugin;
        this.id = id;
        this.type = type;
        this.spawnLocation = location.clone();
        this.options = options == null ? new ManagedEntityOptions() : options;
        this.configurator = configurator == null ? value -> { } : configurator;
        this.markerKey = new NamespacedKey(plugin, "managed_entity");
        synchronized (REGISTRY) {
            REGISTRY.computeIfAbsent(plugin, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(this);
        }
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler public void onPluginDisable(PluginDisableEvent event) {
                if (event.getPlugin() == plugin) remove();
            }
        }, plugin);
    }

    public static <T extends Entity> ManagedEntity<T> spawn(JavaPlugin plugin, String id,
            Location location, Class<T> type, ManagedEntityOptions options, Consumer<T> configurator) {
        ManagedEntity<T> managed = new ManagedEntity<>(plugin, id, location, type, options, configurator);
        managed.spawn();
        return managed;
    }

    public static <T extends Entity> ManagedEntity<T> spawn(JavaPlugin plugin, String id,
            Location location, Class<T> type, ManagedEntityOptions options) {
        return spawn(plugin, id, location, type, options, null);
    }

    public void spawn() {
        ensureMainThread();
        removed = false;
        if (isPresent()) { debug("spawn skipped: uuid=%s is already present", entity.getUniqueId()); return; }
        Chunk chunk = spawnLocation.getChunk();
        debug("loading spawn chunk %s/%s (loaded=%s)", chunk.getX(), chunk.getZ(), chunk.isLoaded());
        chunk.load();
        T existing = findMarkedEntity();
        if (existing != null) {
            entity = existing;
            configure(entity);
            acquireChunk(entity.getLocation().getChunk());
            debug("adopted persistent entity uuid=%s at %s", entity.getUniqueId(), entity.getLocation());
            startMonitor();
            return;
        }
        if (options.forceLoadChunk) acquireChunk(chunk);
        entity = spawnLocation.getWorld().spawn(spawnLocation, type, this::configure);
        debug("spawned id=%s uuid=%s type=%s at %s", id, entity.getUniqueId(), type.getSimpleName(), entity.getLocation());
        startMonitor();
    }

    private void configure(T value) {
        value.setPersistent(options.persistent);
        if (value instanceof LivingEntity) ((LivingEntity) value).setRemoveWhenFarAway(options.removeWhenFarAway);
        value.getPersistentDataContainer().set(markerKey, PersistentDataType.STRING, id);
        configurator.accept(value);
    }

    private T findMarkedEntity() {
        World world = spawnLocation.getWorld();
        for (Entity candidate : world.getEntities()) {
            if (!type.isInstance(candidate)) continue;
            String marker = candidate.getPersistentDataContainer().get(markerKey, PersistentDataType.STRING);
            if (id.equals(marker)) return type.cast(candidate);
        }
        return null;
    }

    private void startMonitor() {
        if (taskId != -1 || options.monitorInterval <= 0) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::monitor,
                options.monitorInterval, options.monitorInterval);
        debug("monitor scheduled: interval=%d ticks", options.monitorInterval);
    }

    private void monitor() {
        if (removed || !plugin.isEnabled()) return;
        if (isPresent()) {
            if (options.forceLoadChunk) reconcileChunk(entity.getLocation().getChunk());
            debug("monitor healthy: uuid=%s valid=%s dead=%s chunk=%s/%s", entity.getUniqueId(),
                    entity.isValid(), entity.isDead(), entity.getLocation().getChunk().getX(), entity.getLocation().getChunk().getZ());
            return;
        }
        debug("entity disappeared: id=%s uuid=%s valid=%s dead=%s respawn=%s", id,
                entity == null ? "null" : entity.getUniqueId(), entity != null && entity.isValid(),
                entity != null && entity.isDead(), options.respawnIfMissing);
        releaseChunk();
        entity = null;
        if (options.respawnIfMissing) {
            try { spawn(); } catch (Throwable error) {
                ConsoleLogger.error(plugin, "Managed entity %s respawn failed: %s", id, error.getMessage());
            }
        }
    }

    public boolean respawn() { ensureMainThread(); removeEntityOnly(); removed = false; spawn(); return isPresent(); }

    public void remove() { ensureMainThread(); removed = true; removeEntityOnly(); synchronized (REGISTRY) {
        Set<ManagedEntity<?>> set = REGISTRY.get(plugin); if (set != null) { set.remove(this); if (set.isEmpty()) REGISTRY.remove(plugin); }
    } }

    private void removeEntityOnly() {
        if (taskId != -1) { Bukkit.getScheduler().cancelTask(taskId); taskId = -1; }
        if (entity != null) {
            debug("removing uuid=%s valid=%s dead=%s", entity.getUniqueId(), entity.isValid(), entity.isDead());
            if (!entity.isDead()) entity.remove();
        }
        entity = null;
        releaseChunk();
    }

    public boolean isPresent() { return entity != null && entity.isValid() && !entity.isDead(); }
    public T getEntity() { return entity; }
    public T requireEntity() { if (!isPresent()) throw new IllegalStateException("Managed entity " + id + " is not present"); return entity; }
    public java.util.UUID getEntityUuid() { return entity == null ? null : entity.getUniqueId(); }
    public Location getLocation() { return isPresent() ? entity.getLocation().clone() : spawnLocation.clone(); }
    public String getId() { return id; }

    private void reconcileChunk(Chunk chunk) {
        ChunkKey next = ChunkKey.of(chunk);
        if (forcedChunk != null && forcedChunk.equals(next)) return;
        releaseChunk(); acquireChunk(chunk);
        debug("moved force-load ownership to chunk %s/%s", chunk.getX(), chunk.getZ());
    }

    private void acquireChunk(Chunk chunk) {
        if (!options.forceLoadChunk) return;
        ChunkKey key = ChunkKey.of(chunk);
        synchronized (CHUNK_LOCK) {
            Integer count = FORCE_COUNTS.get(key);
            if (count == null) { FORCE_PREVIOUS.put(key, chunk.getWorld().isChunkForceLoaded(chunk.getX(), chunk.getZ())); chunk.getWorld().setChunkForceLoaded(chunk.getX(), chunk.getZ(), true); count = 0; }
            FORCE_COUNTS.put(key, count + 1);
        }
        forcedChunk = key;
        debug("force-load acquired chunk %s/%s refs=%d", chunk.getX(), chunk.getZ(), FORCE_COUNTS.get(key));
    }

    private void releaseChunk() {
        if (forcedChunk == null) return;
        ChunkKey key = forcedChunk; forcedChunk = null;
        synchronized (CHUNK_LOCK) {
            Integer count = FORCE_COUNTS.get(key); if (count == null) return;
            if (count <= 1) { FORCE_COUNTS.remove(key); Boolean old = FORCE_PREVIOUS.remove(key); boolean previous = Boolean.TRUE.equals(old); World world = Bukkit.getWorld(key.world); if (world != null && !previous) world.setChunkForceLoaded(key.x, key.z, false); debug("force-load released chunk %s/%s refs=0", key.x, key.z); }
            else { FORCE_COUNTS.put(key, count - 1); debug("force-load released chunk %s/%s refs=%d", key.x, key.z, count - 1); }
        }
    }

    private void debug(String format, Object... args) { if (options.debug) ConsoleLogger.debug(plugin, "ManagedEntity[%s] " + format, prepend(id, args)); }
    private static Object[] prepend(Object first, Object[] rest) { Object[] out = new Object[rest.length + 1]; out[0] = first; System.arraycopy(rest, 0, out, 1, rest.length); return out; }
    private void ensureMainThread() { if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("ManagedEntity must be used on Bukkit main thread"); }

    public static void cleanup(JavaPlugin plugin) {
        ManagedEntity<?>[] values;
        synchronized (REGISTRY) {
            Set<ManagedEntity<?>> set = REGISTRY.get(plugin);
            values = set == null ? new ManagedEntity<?>[0] : set.toArray(new ManagedEntity<?>[0]);
        }
        for (ManagedEntity<?> value : values) value.remove();
    }

    private static final class ChunkKey {
        final java.util.UUID world; final int x; final int z;
        private ChunkKey(java.util.UUID world, int x, int z) { this.world = world; this.x = x; this.z = z; }
        static ChunkKey of(Chunk chunk) { return new ChunkKey(chunk.getWorld().getUID(), chunk.getX(), chunk.getZ()); }
        public boolean equals(Object o) { if (!(o instanceof ChunkKey)) return false; ChunkKey k = (ChunkKey) o; return x == k.x && z == k.z && world.equals(k.world); }
        public int hashCode() { return world.hashCode() * 31 * 31 + x * 31 + z; }
    }
}
