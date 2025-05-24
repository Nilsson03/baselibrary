package ru.nilsson03.library.text.api;

import ru.nilsson03.library.text.util.ReplaceData;

import java.util.List;

public interface TextApi {
    String colorize(String text);
    String decolorize(String text);
    List<String> colorize(List<String> lines);
    String replacePlaceholders(String text, ReplaceData... replaces);
    List<String> replacePlaceholders(List<String> lines, ReplaceData... replaces);

    // Замена с поддержкой стилей (только для Adventure)
    default String replaceStyled(String text, ReplaceData... replaces) {
        return replacePlaceholders(text, replaces); // Дефолтная реализация
    }

    default boolean isColored(String text) {
        return text.contains("§") || text.contains("&");
    }
}
