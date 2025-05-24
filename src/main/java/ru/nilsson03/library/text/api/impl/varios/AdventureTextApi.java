package ru.nilsson03.library.text.api.impl.varios;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import ru.nilsson03.library.text.api.TextApi;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.List;

public class AdventureTextApi implements TextApi {
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.builder()
                    .character('&')
                    .hexColors()
                    .build();

    @Override
    public String colorize(String text) {
        return LEGACY_SERIALIZER.serialize(LEGACY_SERIALIZER.deserialize(text));
    }

    @Override
    public String decolorize(String text) {
        return text.replaceAll("(?i)&[0-9a-fk-orx]", "")
                .replaceAll("&#[a-f0-9]{6}", "");
    }

    @Override
    public List<String> colorize(List<String> lines) {
        lines.replaceAll(this::colorize);
        return lines;
    }

    @Override
    public String replacePlaceholders(String text, ReplaceData... replaces) {
        Component component = LEGACY_SERIALIZER.deserialize(text);
        component = replaceInComponent(component, replaces);
        return LEGACY_SERIALIZER.serialize(component);
    }

    @Override
    public String replaceStyled(String text, ReplaceData... replaces) {
        Component component = LEGACY_SERIALIZER.deserialize(text);
        component = replaceInComponent(component, replaces);
        return LEGACY_SERIALIZER.serialize(component);
    }

    private Component replaceInComponent(Component component, ReplaceData... replaces) {
        for (ReplaceData replace : replaces) {
            if (replace != null && replace.getKey() != null) {
                TextReplacementConfig config = TextReplacementConfig.builder()
                        .matchLiteral(replace.getKey())
                        .replacement(LEGACY_SERIALIZER.deserialize(
                                String.valueOf(replace.getObject())))
                        .build();
                component = component.replaceText(config);
            }
        }
        return component;
    }

    @Override
    public List<String> replacePlaceholders(List<String> lines, ReplaceData... replaces) {
        lines.replaceAll(line -> replacePlaceholders(line, replaces));
        return lines;
    }
}
