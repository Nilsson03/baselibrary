package ru.nilsson03.library.alt.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class LightCache<K, V> {

    private final String name;
    private final Map<K, CacheEntry<V>> cache;
    private final ScheduledExecutorService scheduler;

    private final int cacheSize;
    private final long expireAfterAccess;
    
    // Статистика производительности
    private volatile long totalCleanups = 0;
    private volatile long totalCleanupTime = 0;
    private volatile long totalRemovedEntries = 0;
    private volatile long lastCleanupTime = 0;
    private volatile int maxCleanupTime = 0;
    
    // Статистика операций
    private volatile long totalGets = 0;
    private volatile long totalPuts = 0;
    private volatile long totalGetTime = 0;
    private volatile long totalPutTime = 0;
    private volatile int maxGetTime = 0;
    private volatile int maxPutTime = 0;

    public LightCache(String name, int cacheSize, long expireAfterAccess, ScheduledExecutorService scheduler) {
        this.name = name;
        this.cacheSize = cacheSize;
        this.expireAfterAccess = expireAfterAccess;
        this.cache = new ConcurrentHashMap<>();
        this.scheduler = scheduler;
        startCleanupTask();
    }

    public V get(K key) {
        if (isShutdown()) {
            return null;
        }
        
        long startTime = System.currentTimeMillis();
        totalGets++;
        
        CacheEntry<V> entry = cache.get(key);
        V result = entry == null ? null : entry.getValue();
        
        long duration = System.currentTimeMillis() - startTime;
        totalGetTime += duration;
        maxGetTime = Math.max(maxGetTime, (int) duration);
        
        return result;
    }

    public void put(K key, V value) {
        if (isShutdown()) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        totalPuts++;
        
        if (cache.size() >= cacheSize) {
            removeOldestEntry();
        }
        cache.put(key, new CacheEntry<>(value));
        
        long duration = System.currentTimeMillis() - startTime;
        totalPutTime += duration;
        maxPutTime = Math.max(maxPutTime, (int) duration);
    }
    
    private void removeOldestEntry() {
        if (!cache.isEmpty()) {
            K firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
        }
    }

    private void startCleanupTask() {
        // Чистка каждые 5 минут
        scheduler.scheduleAtFixedRate(this::cleanupExpired, 0, 5, TimeUnit.MINUTES);
    }
    
    private void cleanupExpired() {
        if (isShutdown()) {
            return;
        }
        
        long startTime = System.currentTimeMillis();
        totalCleanups++;
        
        if (cache.isEmpty()) {
            ConsoleLogger.debug("CacheManager", "Cache '%s' is empty, skipping cleanup", name);
            lastCleanupTime = System.currentTimeMillis() - startTime;
            return;
        }

        int initialSize = cache.size();
        long now = System.currentTimeMillis();
        
        // Удаляем истекшие записи и считаем количество
        final int[] removedCount = {0};
        cache.entrySet().removeIf(entry -> {
            boolean expired = (now - entry.getValue().getAccessTime()) > expireAfterAccess;
            if (expired) {
                removedCount[0]++;
            }
            return expired;
        });
        
        // Обновляем статистику
        long cleanupDuration = System.currentTimeMillis() - startTime;
        totalCleanupTime += cleanupDuration;
        totalRemovedEntries += removedCount[0];
        lastCleanupTime = cleanupDuration;
        maxCleanupTime = Math.max(maxCleanupTime, (int) cleanupDuration);
        
        // Логируем статистику очистки
        ConsoleLogger.debug("CacheManager", 
            "Cache '%s' cleanup completed: removed %d/%d entries in %dms (avg: %.2fms, max: %dms)", 
            name, removedCount[0], initialSize, cleanupDuration, 
            (double) totalCleanupTime / totalCleanups, maxCleanupTime);
    }

    /**
     * Корректно закрывает кэш (очищает данные, но не останавливает scheduler)
     */
    public void shutdown() {
        cache.clear();
        ConsoleLogger.debug("CacheManager", "Cache '%s' shutdown completed", name);
    }
    
    /**
     * Проверяет, закрыт ли кэш
     */
    public boolean isShutdown() {
        return scheduler == null || scheduler.isShutdown();
    }
    
    /**
     * Получает имя кэша
     */
    public String getName() {
        return name;
    }
    
    /**
     * Получает текущий размер кэша
     */
    public int size() {
        return cache.size();
    }

    public static class CacheEntry<T> {
        private final T value;
        private long timestamp;

        public CacheEntry(T value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public T getValue() {
            timestamp = System.currentTimeMillis(); // обновляем время доступа
            return value;
        }

        public long getAccessTime() {
            return timestamp;
        }
    }
    
    /**
     * Получает статистику производительности кэша
     */
    public CacheStats getStats() {
        return new CacheStats(
            cache.size(),
            totalCleanups,
            totalCleanupTime,
            totalRemovedEntries,
            lastCleanupTime,
            maxCleanupTime,
            cacheSize,
            expireAfterAccess,
            totalGets,
            totalPuts,
            totalGetTime,
            totalPutTime,
            maxGetTime,
            maxPutTime
        );
    }
    
    /**
     * Получает краткую статистику производительности
     */
    public String getQuickStats() {
        return String.format("Cache '%s': %d/%d items, %d cleanups, %.2fms avg cleanup, %d gets, %d puts", 
            name, cache.size(), cacheSize, totalCleanups, 
            totalCleanups > 0 ? (double) totalCleanupTime / totalCleanups : 0.0,
            totalGets, totalPuts);
    }
    
    /**
     * Сбрасывает статистику
     */
    public void resetStats() {
        totalCleanups = 0;
        totalCleanupTime = 0;
        totalRemovedEntries = 0;
        lastCleanupTime = 0;
        maxCleanupTime = 0;
        totalGets = 0;
        totalPuts = 0;
        totalGetTime = 0;
        totalPutTime = 0;
        maxGetTime = 0;
        maxPutTime = 0;
    }
    
    /**
     * Класс для хранения статистики кэша
     */
    public static class CacheStats {
        private final int currentSize;
        private final long totalCleanups;
        private final long totalCleanupTime;
        private final long totalRemovedEntries;
        private final long lastCleanupTime;
        private final int maxCleanupTime;
        private final int maxSize;
        private final long expireAfterAccess;
        
        // Статистика операций
        private final long totalGets;
        private final long totalPuts;
        private final long totalGetTime;
        private final long totalPutTime;
        private final int maxGetTime;
        private final int maxPutTime;
        
        public CacheStats(int currentSize, long totalCleanups, long totalCleanupTime, 
                         long totalRemovedEntries, long lastCleanupTime, int maxCleanupTime,
                         int maxSize, long expireAfterAccess, long totalGets, long totalPuts,
                         long totalGetTime, long totalPutTime, int maxGetTime, int maxPutTime) {
            this.currentSize = currentSize;
            this.totalCleanups = totalCleanups;
            this.totalCleanupTime = totalCleanupTime;
            this.totalRemovedEntries = totalRemovedEntries;
            this.lastCleanupTime = lastCleanupTime;
            this.maxCleanupTime = maxCleanupTime;
            this.maxSize = maxSize;
            this.expireAfterAccess = expireAfterAccess;
            this.totalGets = totalGets;
            this.totalPuts = totalPuts;
            this.totalGetTime = totalGetTime;
            this.totalPutTime = totalPutTime;
            this.maxGetTime = maxGetTime;
            this.maxPutTime = maxPutTime;
        }
        
        public int getCurrentSize() { return currentSize; }
        public long getTotalCleanups() { return totalCleanups; }
        public long getTotalCleanupTime() { return totalCleanupTime; }
        public long getTotalRemovedEntries() { return totalRemovedEntries; }
        public long getLastCleanupTime() { return lastCleanupTime; }
        public int getMaxCleanupTime() { return maxCleanupTime; }
        public int getMaxSize() { return maxSize; }
        public long getExpireAfterAccess() { return expireAfterAccess; }
        
        // Геттеры для статистики операций
        public long getTotalGets() { return totalGets; }
        public long getTotalPuts() { return totalPuts; }
        public long getTotalGetTime() { return totalGetTime; }
        public long getTotalPutTime() { return totalPutTime; }
        public int getMaxGetTime() { return maxGetTime; }
        public int getMaxPutTime() { return maxPutTime; }
        
        public double getAverageGetTime() {
            return totalGets > 0 ? (double) totalGetTime / totalGets : 0.0;
        }
        
        public double getAveragePutTime() {
            return totalPuts > 0 ? (double) totalPutTime / totalPuts : 0.0;
        }
        
        public double getAverageCleanupTime() {
            return totalCleanups > 0 ? (double) totalCleanupTime / totalCleanups : 0.0;
        }
        
        public double getCleanupEfficiency() {
            return totalCleanups > 0 ? (double) totalRemovedEntries / totalCleanups : 0.0;
        }
        
        public double getCacheUtilization() {
            return maxSize > 0 ? (double) currentSize / maxSize * 100 : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "CacheStats{size=%d/%d (%.1f%%), cleanups=%d, avgCleanupTime=%.2fms, maxCleanupTime=%dms, " +
                "efficiency=%.2f entries/cleanup, gets=%d (avg=%.2fms, max=%dms), puts=%d (avg=%.2fms, max=%dms)}",
                currentSize, maxSize, getCacheUtilization(), totalCleanups, 
                getAverageCleanupTime(), maxCleanupTime, getCleanupEfficiency(),
                totalGets, getAverageGetTime(), maxGetTime, totalPuts, getAveragePutTime(), maxPutTime
            );
        }
    }
}
