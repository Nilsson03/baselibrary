package ru.nilsson03.library.bukkit.util.log;

public enum LogIcon {

    INFO(LogLevel.INFO, "§2ℹ §7"),
    WARNING(LogLevel.WARNING, "§e⚠ §7"),
    ERROR(LogLevel.ERROR, "§c✕ §7"),
    SUCCESS(LogLevel.SUCCESS, "§a✓ §7"),
    DEBUG(LogLevel.DEBUG, "§9⚙ §7");

    private final LogLevel level;
    private final String icon;

    LogIcon(LogLevel level,
          String icon) {
        this.level = level;
        this.icon = icon;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getIcon() {
        return icon;
    }
}
