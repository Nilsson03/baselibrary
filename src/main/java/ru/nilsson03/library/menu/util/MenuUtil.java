package ru.nilsson03.library.menu.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.menu.item.CustomItem;
import ru.nilsson03.library.menu.item.impl.NavigationBackButton;
import ru.nilsson03.library.menu.item.impl.StaticCustomItem;
import ru.nilsson03.library.menu.item.impl.UpdatableCustomItem;
import ru.nilsson03.library.text.util.ReplaceData;
import xyz.xenondevs.invui.item.Item;

import java.util.*;

public class MenuUtil {

    public static List<CustomItem> parseSection(NPlugin plugin, ConfigurationSection section, ReplaceData... replacesData) {
        return parseSection(plugin, section, null, replacesData);
    }

    public static List<CustomItem> parseSection(NPlugin plugin, ConfigurationSection section, FileConfiguration config, ReplaceData... replacesData) {
        if (section == null) {
            throw new IllegalArgumentException("Items section cannot be null");
        }

        List<CustomItem> items = new ArrayList<>();
        Set<String> keys = section.getKeys(false);

        for (String key : keys) {
            ConfigurationSection itemConfig = section.getConfigurationSection(key);
            if (itemConfig != null) {
                if (key.equalsIgnoreCase("back_button") || key.equalsIgnoreCase("navigation_back")) {
                    continue;
                }
                
                boolean update = itemConfig.getBoolean("update", false);
                CustomItem item = update 
                    ? new UpdatableCustomItem(itemConfig, replacesData)
                    : new StaticCustomItem(itemConfig, replacesData);
                
                items.add(item);
            } else {
                ConsoleLogger.warn(plugin, "Could not get item section for %s from configuration", key);
            }
        }

        return items;
    }

    public static Map<Character, Item> parseSectionAsMap(NPlugin plugin, ConfigurationSection section, FileConfiguration config, ReplaceData... replacesData) {
        if (section == null) {
            throw new IllegalArgumentException("Items section cannot be null");
        }

        Map<Character, Item> items = new HashMap<>();
        Set<String> keys = section.getKeys(false);

        for (String key : keys) {
            ConfigurationSection itemConfig = section.getConfigurationSection(key);
            if (itemConfig != null) {
                String position = itemConfig.getString("position");
                if (position != null && !position.isEmpty()) {
                    char posChar = position.charAt(0);

                    Item item;
                    if ((key.equalsIgnoreCase("back_button") || key.equalsIgnoreCase("navigation_back")) && config != null) {
                        item = new NavigationBackButton(config);
                    } else {
                        boolean update = itemConfig.getBoolean("update", false);
                        item = update 
                            ? new UpdatableCustomItem(itemConfig, replacesData)
                            : new StaticCustomItem(itemConfig, replacesData);
                    }

                    items.put(posChar, item);
                }
            } else {
                ConsoleLogger.warn(plugin, "Could not get item section for %s from configuration", key);
            }
        }

        return items;
    }

}
