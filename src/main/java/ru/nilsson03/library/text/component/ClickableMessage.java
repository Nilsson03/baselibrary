package ru.nilsson03.library.text.component;

import net.md_5.bungee.api.chat.*;

import org.bukkit.entity.Player;
import ru.nilsson03.library.text.component.action.ClickAction;
import ru.nilsson03.library.text.component.action.ClickActionRegistry;

import java.util.ArrayList;
import java.util.List;

public final class ClickableMessage {
    private final TextComponent root;
    private final List<TextComponent> parts;
    private final List<Button> buttonParts;

    private ClickableMessage(String text) {
        this.root = new TextComponent("");
        this.parts = new ArrayList<>();
        this.buttonParts = new ArrayList<>();

        if (text != null && !text.isEmpty()) {
            this.appendText(text);
        }
    }

    private ClickableMessage(List<String> lines) {
        this.root = new TextComponent("");
        this.parts = new ArrayList<>();
        this.buttonParts = new ArrayList<>();

        if (lines != null && !lines.isEmpty()) {
            this.appendLines(lines);
        }
    }

    public static ClickableMessage of(String text) {
        return new ClickableMessage(text);
    }

    public static ClickableMessage of(List<String> lines) {
        return new ClickableMessage(lines);
    }

    public ClickableMessage appendLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return this;
        }

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line != null && !line.isEmpty()) {
                this.appendText(line);
                if (i < lines.size() - 1) {
                    this.appendNewLine();
                }
            }
        }
        return this;
    }

    public ClickableMessage appendNewLine() {
        return this.appendText("\n");
    }

    public ClickableMessage appendText(String text) {
        if (text == null || text.isEmpty()) {
            return this;
        }
        TextComponent component = new TextComponent(TextComponent.fromLegacyText(text));
        this.parts.add(component);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ClickableMessage appendButton(String buttonText, String hoverText, ClickAction action) {
        if (buttonText == null || buttonText.isEmpty()) {
            return this;
        }

        TextComponent button = new TextComponent(TextComponent.fromLegacyText(buttonText));

        if (hoverText != null && !hoverText.isEmpty()) {
            button.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{new TextComponent(TextComponent.fromLegacyText(hoverText))}
            ));
        }

        if (action != null) {
            this.buttonParts.add(new Button(button, action));
        }

        this.parts.add(button);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ClickableMessage appendButtonLine(String buttonText, String hoverText, ClickAction action) {
        this.appendNewLine();
        return this.appendButton(buttonText, hoverText, action);
    }

    public BaseComponent[] build(Player player) {
        root.setText("");
        root.setHoverEvent(null);
        root.setClickEvent(null);

        if (player != null) {
            for (Button button : buttonParts) {
                String actionId = ClickActionRegistry.registerAction(player, button.action());
                button.component().setClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        "/" + actionId
                ));
            }
        }

        for (BaseComponent child : parts) {
            root.addExtra(child);
        }

        return new BaseComponent[]{root};
    }

    public void sendTo(Player player) {
        player.spigot().sendMessage(build(player));
    }
}