package ru.nilsson03.library.menu;

import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MenuHistoryManager {
    
    private static final Map<UUID, Deque<Runnable>> menuHistory = new ConcurrentHashMap<>();
    
    public static void pushMenu(Player player, Runnable menuOpener) {
        Deque<Runnable> history = menuHistory.computeIfAbsent(player.getUniqueId(), k -> new ArrayDeque<>());
        history.addLast(menuOpener);
    }
    
    public static void openMenuWithHistory(Player player, Consumer<Player> menuOpener) {
        pushMenu(player, () -> menuOpener.accept(player));
        menuOpener.accept(player);
    }
    
    public static boolean openPreviousMenu(Player player) {
        Deque<Runnable> history = menuHistory.get(player.getUniqueId());
        
        if (history == null || history.size() <= 1) {
            return false;
        }
        
        history.removeLast();
        
        Runnable previousMenu = history.peekLast();
        if (previousMenu != null) {
            previousMenu.run();
            return true;
        }
        
        return false;
    }
    
    public static void clearHistory(Player player) {
        menuHistory.remove(player.getUniqueId());
    }
    
    public static boolean hasHistory(Player player) {
        Deque<Runnable> history = menuHistory.get(player.getUniqueId());
        return history != null && history.size() > 1;
    }
}
