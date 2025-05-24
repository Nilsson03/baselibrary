package ru.nilsson03.library.text.component.action.impl;

import org.bukkit.entity.Player;
import ru.nilsson03.library.text.component.action.ClickAction;

public class RunnableAction implements ClickAction {
    private final Runnable runnable;

    public RunnableAction(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void execute(Player player) {
        runnable.run();
    }
}
