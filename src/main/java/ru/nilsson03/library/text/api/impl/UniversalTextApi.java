package ru.nilsson03.library.text.api.impl;

import ru.nilsson03.library.text.api.TextApi;
import ru.nilsson03.library.text.api.TextApiFactory;
import ru.nilsson03.library.text.util.ReplaceData;

import java.util.List;

public class UniversalTextApi {
    private static final TextApi API = TextApiFactory.create();

    public static String colorize(String text) {
        return API.colorize(text);
    }

    public static String decolorize(String text) {
        return API.decolorize(text);
    }

    public static List<String> colorize(List<String> lines) {
        return API.colorize(lines);
    }

    public static String replacePlaceholders(String text, ReplaceData... replaceData) {
        return API.replacePlaceholders(text, replaceData);
    }

    public static List<String> replacePlaceholders(List<String> lines, ReplaceData... replaceData) {
        return API.replacePlaceholders(lines, replaceData);
    }
}
