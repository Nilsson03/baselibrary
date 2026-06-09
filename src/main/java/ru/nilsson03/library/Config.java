package ru.nilsson03.library;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;

public class Config {

    private static FileConfiguration configuration;

    public static String getTimeString(TimeUnitForm unit, WordForm form) {
        String key = unit.key + form.key;
        return getConfiguration().getString(key);
    }

    public static FileConfiguration getConfiguration() {
        if (configuration == null) {
            configuration = BaseLibrary.getInstance().getConfig();
        }
        return configuration;
    }

    @AllArgsConstructor
    public enum WordForm {
        FIRST("_first_form"),
        SECOND("_second_form"),
        THIRD("_third_form");

        private final String key;
    }

    @AllArgsConstructor
    @Getter
    public enum TimeUnitForm {
        SECONDS("time.seconds"),
        MINUTES("time.minutes"),
        HOURS("time.hours"),
        DAYS("time.days"),
        WEEKS("time.weeks"),
        MONTHS("time.months"),
        YEARS("time.years");

        private final String key;
    }
}
