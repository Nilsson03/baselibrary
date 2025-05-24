package ru.nilsson03.library.bukkit.integration;

public class DependencyException extends RuntimeException {

    public DependencyException(String message, Object... args) {
        super(String.format(message, args));
    }

    public DependencyException(Throwable cause, String message, Object... args) {
        super(String.format(message, args), cause);
    }
}
