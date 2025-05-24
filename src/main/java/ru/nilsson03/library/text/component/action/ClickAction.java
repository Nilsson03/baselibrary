package ru.nilsson03.library.text.component.action;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface ClickAction {
    void execute(Player player);
}
