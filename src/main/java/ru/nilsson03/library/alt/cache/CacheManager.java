package ru.nilsson03.library.alt.cache;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CacheManager {
    
    private static final CacheManager INSTANCE = new CacheManager();
    
    private final Map<String, LightCache<?, ?>> caches = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger cacheCounter = new AtomicInteger(0);
    
    private volatile boolean shutdown = false;
    
    private CacheManager() {
        this.scheduler = Executors.newScheduledThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "cache-manager-" + cacheCounter.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        );
        
        ConsoleLogger.info("CacheManager", "Cache manager initialized with %d threads", 
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2));
    }
    
    public static CacheManager getInstance() {
        return INSTANCE;
    }
    
    public <K, V> LightCache<K, V> createCache(String name, int cacheSize, long expireAfterAccess) {
        if (shutdown) {
            throw new IllegalStateException("Cache manager is shutdown");
        }
        
        if (caches.containsKey(name)) {
            ConsoleLogger.warn("CacheManager", "Cache with name '%s' already exists, returning existing", name);
            @SuppressWarnings("unchecked")
            LightCache<K, V> existing = (LightCache<K, V>) caches.get(name);
            return existing;
        }
        
        LightCache<K, V> cache = new LightCache<>(name, cacheSize, expireAfterAccess, scheduler);
        caches.put(name, cache);
        
        ConsoleLogger.debug("CacheManager", "Created cache '%s' with size=%d, expireAfter=%dms", 
            name, cacheSize, expireAfterAccess);
        
        return cache;
    }
    
    public <K, V> LightCache<K, V> createCache(String name) {
        return createCache(name, 1000, TimeUnit.MINUTES.toMillis(10));
    }
    
    @SuppressWarnings("unchecked")
    public <K, V> LightCache<K, V> getCache(String name) {
        return (LightCache<K, V>) caches.get(name);
    }
    
    public boolean removeCache(String name) {
        LightCache<?, ?> cache = caches.remove(name);
        if (cache != null) {
            cache.shutdown();
            ConsoleLogger.debug("CacheManager", "Removed and shutdown cache '%s'", name);
            return true;
        }
        return false;
    }

    public CacheManagerStats getStats() {
        int totalCaches = caches.size();
        int totalSize = caches.values().stream().mapToInt(cache -> cache.size()).sum();
        long totalCleanups = caches.values().stream().mapToLong(cache -> cache.getStats().getTotalCleanups()).sum();
        long totalGets = caches.values().stream().mapToLong(cache -> cache.getStats().getTotalGets()).sum();
        long totalPuts = caches.values().stream().mapToLong(cache -> cache.getStats().getTotalPuts()).sum();
        
        return new CacheManagerStats(totalCaches, totalSize, totalCleanups, totalGets, totalPuts, shutdown);
    }
    
    public String getQuickStats() {
        CacheManagerStats stats = getStats();
        return String.format("CacheManager: %d caches, %d total items, %d cleanups, %d gets, %d puts, shutdown=%s",
            stats.getTotalCaches(), stats.getTotalSize(), stats.getTotalCleanups(), 
            stats.getTotalGets(), stats.getTotalPuts(), stats.isShutdown());
    }
    
    public void shutdown() {
        if (shutdown) {
            return;
        }
        
        ConsoleLogger.info("CacheManager", "Shutting down cache manager with %d caches", caches.size());
        
        shutdown = true;
        
        caches.values().forEach(LightCache::shutdown);
        caches.clear();
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                    if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                        ConsoleLogger.warn("CacheManager", "Cache scheduler did not terminate gracefully");
                    }
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        ConsoleLogger.info("CacheManager", "Cache manager shutdown completed");
    }
    
    /**
     * Проверяет, закрыт ли менеджер
     */
    public boolean isShutdown() {
        return shutdown;
    }
    
    /**
     * Получает список всех кэшей
     */
    public Map<String, LightCache<?, ?>> getAllCaches() {
        return Map.copyOf(caches);
    }
    
    /**
     * Класс для хранения статистики менеджера кэшей
     */
    public static class CacheManagerStats {
        private final int totalCaches;
        private final int totalSize;
        private final long totalCleanups;
        private final long totalGets;
        private final long totalPuts;
        private final boolean shutdown;
        
        public CacheManagerStats(int totalCaches, int totalSize, long totalCleanups, 
                                long totalGets, long totalPuts, boolean shutdown) {
            this.totalCaches = totalCaches;
            this.totalSize = totalSize;
            this.totalCleanups = totalCleanups;
            this.totalGets = totalGets;
            this.totalPuts = totalPuts;
            this.shutdown = shutdown;
        }
        
        public int getTotalCaches() { return totalCaches; }
        public int getTotalSize() { return totalSize; }
        public long getTotalCleanups() { return totalCleanups; }
        public long getTotalGets() { return totalGets; }
        public long getTotalPuts() { return totalPuts; }
        public boolean isShutdown() { return shutdown; }
        
        @Override
        public String toString() {
            return String.format("CacheManagerStats{caches=%d, size=%d, cleanups=%d, gets=%d, puts=%d, shutdown=%s}",
                totalCaches, totalSize, totalCleanups, totalGets, totalPuts, shutdown);
        }
    }
}
