package ru.nilsson03.library.text.component;

import net.md_5.bungee.api.chat.TextComponent;
import ru.nilsson03.library.text.component.action.ClickAction;

public record Button(TextComponent component, ClickAction action) {
    
}
