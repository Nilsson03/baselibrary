package ru.nilsson03.library.text.component.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClickActionRegistry implements Listener {
    private static final String COMMAND_PREFIX = "msgaction_";
    private static final Map<String, ClickAction> ACTIONS = new ConcurrentHashMap<>();
    private static final Map<UUID, String> PLAYER_ACTIONS = new ConcurrentHashMap<>();

    public static void register(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new ClickActionRegistry(), plugin);
    }

    public static String registerAction(Player player, ClickAction action) {
        String actionId = COMMAND_PREFIX + UUID.randomUUID();
        ACTIONS.put(actionId, action);
        PLAYER_ACTIONS.put(player.getUniqueId(), actionId);
        return actionId;
    }

    public static void unregisterAction(Player player) {
        String actionId = PLAYER_ACTIONS.remove(player.getUniqueId());
        if (actionId != null) {
            ACTIONS.remove(actionId);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().substring(1);
        if (ACTIONS.containsKey(command)) {
            event.setCancelled(true);
            ClickAction action = ACTIONS.remove(command);
            if (action != null) {
                action.execute(event.getPlayer());
            }
            PLAYER_ACTIONS.remove(event.getPlayer().getUniqueId());
        }
    }
}
