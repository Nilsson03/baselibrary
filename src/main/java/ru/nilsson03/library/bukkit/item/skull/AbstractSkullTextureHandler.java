package ru.nilsson03.library.bukkit.item.skull;

import org.bukkit.inventory.meta.SkullMeta;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class AbstractSkullTextureHandler implements SkullTextureHandler {
    protected static final String TEXTURES_PROPERTY = "textures";

    @Override
    public abstract void applyTexture(SkullMeta meta, String textureUrl);

    @Override
    public abstract String getTexture(SkullMeta meta);

    protected void validateInput(SkullMeta meta, String textureUrl) {
        if (meta == null) throw new IllegalArgumentException("SkullMeta cannot be null");
        if (textureUrl == null || textureUrl.isEmpty()) {
            throw new IllegalArgumentException("Texture URL cannot be null or empty");
        }
    }

    protected URL validateAndCreateUrl(String textureUrl) throws MalformedURLException {
        if (!textureUrl.startsWith("http://") && !textureUrl.startsWith("https://")) {
            textureUrl = "https://" + textureUrl;
        }
        return new URL(textureUrl);
    }
}
