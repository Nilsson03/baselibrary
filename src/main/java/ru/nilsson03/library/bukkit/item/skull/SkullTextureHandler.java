package ru.nilsson03.library.bukkit.item.skull;

import org.bukkit.inventory.meta.SkullMeta;

public interface SkullTextureHandler {
    void applyTexture(SkullMeta meta, String textureUrl);
    String getTexture(SkullMeta meta);
}
