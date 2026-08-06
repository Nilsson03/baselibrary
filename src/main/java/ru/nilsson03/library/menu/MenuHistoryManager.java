package ru.nilsson03.library.menu;

import org.bukkit.entity.Player;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MenuHistoryManager {

    private static final Map<UUID, Deque<Consumer<Player>>> menuHistory = new ConcurrentHashMap<>();

    public static void pushMenu(Player player, Consumer<Player> menuOpener) {
        Deque<Consumer<Player>> history = menuHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        history.addLast(menuOpener);
    }

    public static void openMenuWithHistory(Player player, Consumer<Player> menuOpener) {
        pushMenu(player, menuOpener);
        menuOpener.accept(player);
    }

    public static boolean openPreviousMenu(Player player) {
        if (!hasHistory(player)) {
            return false;
        }

        Deque<Consumer<Player>> history = menuHistory.get(player.getUniqueId());
        history.removeLast();
        history.peekLast().accept(player);
        return true;
    }

    public static void clearHistory(Player player) {
        menuHistory.remove(player.getUniqueId());
    }

    public static boolean hasHistory(Player player) {
        Deque<Consumer<Player>> history = menuHistory.get(player.getUniqueId());
        return history != null && history.size() > 1;
    }
}
