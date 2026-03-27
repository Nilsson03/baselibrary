package ru.nilsson03.library.menu.command.impl;

import org.bukkit.entity.Player;
import ru.nilsson03.library.menu.command.MenuAction;

public class CloseMenuAction implements MenuAction {

    @Override
    public void execute(Player player) {
        player.closeInventory();
    }
}
