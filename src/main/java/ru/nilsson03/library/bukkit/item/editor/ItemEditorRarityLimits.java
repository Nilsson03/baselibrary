package ru.nilsson03.library.bukkit.item.editor;

import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.BaseLibrary;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Bounds used by the shared item editor for per-item rarity levels.
 *
 * <p>The defaults intentionally keep the old behaviour (level 1 only). A
 * plugin can opt into more levels by changing the BaseLibrary configuration
 * or by passing an instance to the ItemEditorMenu constructor.</p>
 */
public final class ItemEditorRarityLimits {
    public static final int DEFAULT_MIN_LEVEL = 1;
    public static final int DEFAULT_MAX_LEVEL = 1;

    private final int minLevel;
    private final int maxLevel;
    private final Map<Integer, Integer> itemCountLimits;

    public ItemEditorRarityLimits(int minLevel, int maxLevel) {
        this(minLevel, maxLevel, Collections.emptyMap());
    }

    public ItemEditorRarityLimits(int minLevel, int maxLevel, Map<Integer, Integer> itemCountLimits) {
        this.minLevel = Math.max(1, minLevel);
        this.maxLevel = Math.max(this.minLevel, maxLevel);
        Map<Integer, Integer> copy = new LinkedHashMap<>();
        if (itemCountLimits != null) {
            itemCountLimits.forEach((level, limit) -> {
                if (level != null && level >= 1 && limit != null) copy.put(level, limit);
            });
        }
        this.itemCountLimits = Collections.unmodifiableMap(copy);
    }

    public static ItemEditorRarityLimits fixed(int level) {
        int safe = Math.max(1, level);
        return new ItemEditorRarityLimits(safe, safe);
    }

    /** Reads item-editor.rarity.min-level/max-level with a level-1 fallback. */
    public static ItemEditorRarityLimits fromConfiguration(FileConfiguration configuration) {
        if (configuration == null) return new ItemEditorRarityLimits(DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL);
        int min = configuration.getInt("item-editor.rarity.min-level", DEFAULT_MIN_LEVEL);
        int max = configuration.getInt("item-editor.rarity.max-level", DEFAULT_MAX_LEVEL);
        Map<Integer, Integer> countLimits = new LinkedHashMap<>();
        org.bukkit.configuration.ConfigurationSection section =
                configuration.getConfigurationSection("item-editor.rarity.limits");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try { countLimits.put(Integer.parseInt(key), section.getInt(key, -1)); }
                catch (NumberFormatException ignored) { }
            }
        }
        return new ItemEditorRarityLimits(min, max, countLimits);
    }

    /** Reads the shared BaseLibrary configuration without making startup mandatory. */
    public static ItemEditorRarityLimits fromLibraryConfig() {
        try {
            BaseLibrary library = BaseLibrary.getInstance();
            return library == null
                    ? new ItemEditorRarityLimits(DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL)
                    : fromConfiguration(library.getConfig());
        } catch (Throwable ignored) {
            return new ItemEditorRarityLimits(DEFAULT_MIN_LEVEL, DEFAULT_MAX_LEVEL);
        }
    }

    public int getMinLevel() { return minLevel; }

    public int getMaxLevel() { return maxLevel; }

    public int clamp(int level) {
        return Math.max(minLevel, Math.min(maxLevel, level));
    }

    /** Maximum number of selected items for a level; -1 means unlimited. */
    public int getItemCountLimit(int level) {
        return itemCountLimits.getOrDefault(level, -1);
    }

    public Map<Integer, Integer> getItemCountLimits() { return itemCountLimits; }
}
