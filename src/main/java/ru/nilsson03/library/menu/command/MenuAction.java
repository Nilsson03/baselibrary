package ru.nilsson03.library.menu.command;

import org.bukkit.entity.Player;

/**
 * Исполнитель команд. Определяет способ выполнения команды.
 */
@FunctionalInterface
public interface MenuAction {
    
    /**
     * Выполняет команду для указанного игрока.
     *
     * @param player игрок, для которого выполняется команда
     */
    void execute(Player player);
}
