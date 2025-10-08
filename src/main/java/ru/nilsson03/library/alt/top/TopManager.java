package ru.nilsson03.library.alt.top;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.scheduler.TaskScheduler;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class TopManager<K, V> {

    private final NPlugin plugin;

    private final LinkedHashMap<Long, V> topStorage;
    private final Function<V, K> keyExtractor;
    private final ToLongFunction<V> valueExtractor;
    private final Function<K, String> nameFormatter;

    private final TaskScheduler taskScheduler;
    private TopScheduler scheduler;

    public TopManager(NPlugin plugin,
            Function<V, K> keyExtractor,
            ToLongFunction<V> valueExtractor,
            Function<K, String> nameFormatter) {
        this.plugin = plugin;
        this.keyExtractor = keyExtractor;
        this.valueExtractor = valueExtractor;
        this.nameFormatter = nameFormatter;
        this.topStorage = new LinkedHashMap<>();
        this.taskScheduler = plugin.taskScheduler();
    }

    public void updateTop(Collection<V> collection) {
        topStorage.clear();
        List<V> sorted = collection.stream()
                .sorted(Comparator.comparingLong(valueExtractor).reversed())
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            topStorage.put((long) (i + 1), sorted.get(i));
            ConsoleLogger.debug("baselibrary", "Added to top: rank %d, value %d", i + 1,
                    valueExtractor.applyAsLong(sorted.get(i)));
        }
    }

    public void startUpdater(Supplier<Collection<V>> dataProvider, Duration interval, Duration delay) {

        if (scheduler != null)
            stopUpdater();

        scheduler = new TopScheduler(plugin);

        scheduler.startUpdater(taskScheduler.createTask(() -> {
            Collection<V> currentData = dataProvider.get();
            updateTop(currentData);
        }).withDelay(delay)
                .withInterval(interval)
                .schedule());
    }

    public void stopUpdater() {
        if (scheduler == null) {
            ConsoleLogger.warn(plugin,
                    "It was not possible to cancel the task of updating the top for the %s plugin, because the top updater is null.",
                    plugin.getName());
            return;
        }
        scheduler.stopUpdater();
    }

    public V getPlayerByRank(int rank) {
        return topStorage.get((long) rank);
    }

    public long getPlayerRank(K key) {
        return topStorage.entrySet()
                .stream()
                .filter(entry -> keyExtractor.apply(entry.getValue()).equals(key))
                .mapToLong(Map.Entry::getKey)
                .findFirst()
                .orElse(-1);
    }

    public List<V> getTopPlayers(int limit) {
        return topStorage.entrySet()
                .stream()
                .limit(limit)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    public List<Map.Entry<Integer, V>> getTopPlayersWithPositions(int limit) {
        List<Map.Entry<Integer, V>> topPlayers = new ArrayList<>();

        // Если limit = -1, показываем всех игроков
        int count = (limit == -1) ? topStorage.size() : Math.min(limit, topStorage.size());

        for (int i = 0; i < count; i++) {
            long rank = i + 1;
            V value = topStorage.get(rank);
            if (value != null) {
                topPlayers.add(new AbstractMap.SimpleEntry<>(i + 1, value));
            }
        }
        return topPlayers;
    }

    public List<String> getTopPlayersFormatted(String format, int limit) {
        List<String> top = new ArrayList<>();
        List<Map.Entry<Integer, V>> topPlayers = getTopPlayersWithPositions(limit);

        // Если нет игроков в топе, возвращаем пустой список
        if (topPlayers.isEmpty()) {
            return top;
        }

        for (int i = 0; i < limit; i++) {
            if (i < topPlayers.size()) {
                Map.Entry<Integer, V> entry = topPlayers.get(i);
                K key = keyExtractor.apply(entry.getValue());
                String name = nameFormatter.apply(key);
                long value = valueExtractor.applyAsLong(entry.getValue());

                top.add(format
                        .replace("{number}", String.valueOf(entry.getKey()))
                        .replace("{player}", name)
                        .replace("{value}", String.valueOf(value)));
            } else {
                top.add("Пусто");
            }
        }
        return top;
    }

    public LinkedHashMap<Long, V> getTopStorage() {
        return this.topStorage;
    }
}
