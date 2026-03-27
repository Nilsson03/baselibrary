package ru.nilsson03.library.menu.command.impl;

import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.nilsson03.library.menu.command.MenuAction;

/**
 * Исполнитель команд от имени консоли.
 * Команда выполняется от лица сервера (консоли).
 */
@AllArgsConstructor
public class ConsoleMenuAction implements MenuAction {

    private final String clickCommand;

    @Override
    public void execute(Player player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), clickCommand.replace("{player}", player.getName()));
    }
}
