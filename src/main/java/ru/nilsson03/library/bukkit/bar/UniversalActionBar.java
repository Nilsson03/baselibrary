package ru.nilsson03.library.bukkit.bar;

import org.bukkit.entity.Player;
import ru.nilsson03.library.nms.NMSActionBar;

public class UniversalActionBar {
    private static final ActionBarHandler handler;

    static {
        handler = new NMSActionBar();
    }

    public static void send(Player player, String message) {
        handler.send(player, message);
    }
}
