package ru.nilsson03.library.text.api;

import ru.nilsson03.library.text.api.impl.varios.AdventureTextApi;
import ru.nilsson03.library.text.api.impl.varios.SpigotTextApi;

public class TextApiFactory {

    public static TextApi create() {
        try {
            Class.forName("net.kyori.adventure.text.Component");
            return new AdventureTextApi();
        } catch (ClassNotFoundException e) {
            return new SpigotTextApi();
        }
    }
}
