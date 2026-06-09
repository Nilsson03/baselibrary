package ru.nilsson03.library.bukkit.notify.pending;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PendingNotificationRegistry {
    private final Map<String, Runnable> onCreateActions = new ConcurrentHashMap<>();
    private final Map<String, Runnable> onExpireActions = new ConcurrentHashMap<>();
    private final Map<String, Runnable> onCancelActions = new ConcurrentHashMap<>();

    public PendingNotificationRegistry onCreate(String key, Runnable action) {
        onCreateActions.put(key, action);
        return this;
    }

    public PendingNotificationRegistry onExpire(String key, Runnable action) {
        onExpireActions.put(key, action);
        return this;
    }

    public PendingNotificationRegistry onCancel(String key, Runnable action) {
        onCancelActions.put(key, action);
        return this;
    }

    public PendingNotificationRegistry all(String key, Runnable onCreate, Runnable onExpire, Runnable onCancel) {
        onCreateActions.put(key, onCreate);
        onExpireActions.put(key, onExpire);
        onCancelActions.put(key, onCancel);
        return this;
    }

    public Runnable getOnCreate(String key) { 
        return onCreateActions.getOrDefault(key, () -> {}); 
    }
    
    public Runnable getOnExpire(String key) { 
        return onExpireActions.getOrDefault(key, () -> {}); 
    }
    
    public Runnable getOnCancel(String key) { 
        return onCancelActions.getOrDefault(key, () -> {}); 
    }

    boolean hasKey(String key) {
        return onCreateActions.containsKey(key) ||
                onExpireActions.containsKey(key) ||
                onCancelActions.containsKey(key);
    }
}
