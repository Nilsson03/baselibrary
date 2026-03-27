package ru.nilsson03.library.menu.command.impl;

import org.bukkit.entity.Player;
import ru.nilsson03.library.menu.MenuHistoryManager;
import ru.nilsson03.library.menu.command.MenuAction;

public class PreviousMenuAction implements MenuAction {

    @Override
    public void execute(Player player) {
        MenuHistoryManager.openPreviousMenu(player);
    }
}
