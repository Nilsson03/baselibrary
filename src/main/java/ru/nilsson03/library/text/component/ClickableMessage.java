package ru.nilsson03.library.text.component;

import net.md_5.bungee.api.chat.*;

import org.bukkit.entity.Player;
import ru.nilsson03.library.text.component.action.ClickAction;
import ru.nilsson03.library.text.component.action.ClickActionRegistry;

public final class ClickableMessage {
    private final TextComponent component;
    private ClickAction action;

    private ClickableMessage(String text) {
        this.component = new TextComponent(TextComponent.fromLegacyText(text));
    }

    public static ClickableMessage of(String text) {
        return new ClickableMessage(text);
    }

    @SuppressWarnings("deprecation")
    public ClickableMessage withHover(String hoverText) {
        if (hoverText != null && !hoverText.isEmpty()) {
            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{new TextComponent(TextComponent.fromLegacyText(hoverText))}
            ));
        }
        return this;
    }

    public ClickableMessage withAction(ClickAction action) {
        this.action = action;
        return this;
    }

    public BaseComponent[] build(Player player) {
        if (action != null && player != null) {
            String actionId = ClickActionRegistry.registerAction(player, action);
            component.setClickEvent(new ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/" + actionId
            ));
        }
        return new BaseComponent[]{component};
    }

    public void sendTo(Player player) {
        player.spigot().sendMessage(build(player));
    }
}
