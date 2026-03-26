package ru.nilsson03.library.menu.command.impl;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import ru.nilsson03.library.menu.command.MenuAction;

/**
 * Исполнитель команд от имени игрока.
 * Команда выполняется так, как будто игрок ввёл её в чат.
 */
@AllArgsConstructor
public class PlayerMenuAction implements MenuAction {

    private final String clickCommand;

    @Override
    public void execute(Player player) {
        player.performCommand(clickCommand);
    }
}
