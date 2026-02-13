package ru.nilsson03.library.bukkit.persistense.block;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Accessors(fluent = true)
public class BlockData {

    @NotNull
    private final String blockKey;
    @NotNull
    private final Map<String, Object> data;

    public BlockData(@NotNull  Block block) {
        Objects.requireNonNull(block, "Block cant be null!");
        Location location = block.getLocation();
        this.blockKey = location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
        data = new HashMap<>();
    }

    public void set(@NotNull String key, @Nullable Object value) {
        if (key.isEmpty() || value == null) {
            ConsoleLogger.warn("baselibrary", "Key or value is null in BlockData!");
            return;
        }
        data.put(key, value);
    }

    public void setString(@NotNull String key, @NotNull String value) {
        set(key, value);
    }

    public void setInt(@NotNull String key, int value) {
        set(key, value);
    }

    public void setDouble(@NotNull String key, double value) {
        set(key, value);
    }

    public void setBoolean(@NotNull String key, boolean value) {
        set(key, value);
    }

    @Nullable
    public Object get(@NotNull String key) {
        if (key.isEmpty()) {
            ConsoleLogger.warn("baselibrary", "Key cant be empty in BlockData!");
            return null;
        }
        return data.get(key);
    }

    @Nullable
    public String getString(@NotNull String key) {
        Object value = get(key);
        return value instanceof String ? (String) value : null;
    }

    @Nullable
    public Integer getInt(@NotNull String key) {
        Object value = get(key);
        return value instanceof Integer ? (Integer) value : null;
    }

    @Nullable
    public Double getDouble(@NotNull String key) {
        Object value = get(key);
        return value instanceof Double ? (Double) value : null;
    }

    @Nullable
    public Boolean getBoolean(@NotNull String key) {
        Object value = get(key);
        return value instanceof Boolean ? (Boolean) value : null;
    }

    public boolean equals(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cant be null!");
        String parsedLocation = parseLocation(block.getLocation());
        return blockKey.equals(parsedLocation);
    }

    @NotNull
    private String parseLocation(@NotNull Location location) {
        return location.getWorld().getName() + ":" +
                location.getBlockX() + ":" +
                location.getBlockY() + ":" +
                location.getBlockZ();
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof BlockData other)) return false;
        return blockKey.equals(other.blockKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockKey);
    }
}
