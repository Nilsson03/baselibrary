package ru.nilsson03.library.bukkit.item.skull.impl.versioned;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.inventory.meta.SkullMeta;
import ru.nilsson03.library.bukkit.item.skull.AbstractSkullTextureHandler;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class SpigotSkullTextureHandler_v1_19 extends AbstractSkullTextureHandler {
    private static final Field PROFILE_FIELD;

    static {
        try {
            Class<?> metaSkull = Class.forName("org.bukkit.craftbukkit.inventory.CraftMetaSkull");
            PROFILE_FIELD = metaSkull.getDeclaredField("profile");
            PROFILE_FIELD.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Spigot 1.16-1.19 skull handler", e);
        }
    }

    @Override
    public void applyTexture(SkullMeta meta, String textureUrl) {
        validateInput(meta, textureUrl);
        try {
            PROFILE_FIELD.set(meta, createProfile(textureUrl));
        } catch (Exception e) {
            throw new RuntimeException("Failed to apply texture to skull", e);
        }
    }

    @Override
    public String getTexture(SkullMeta meta) {
        try {
            GameProfile profile = (GameProfile) PROFILE_FIELD.get(meta);
            return profile.getProperties().get(TEXTURES_PROPERTY).iterator().next().getValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get texture from skull", e);
        }
    }

    private GameProfile createProfile(String textureUrl) {
        String textureJson = String.format("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}", textureUrl);
        String encoded = Base64.getEncoder().encodeToString(textureJson.getBytes());

        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        profile.getProperties().put(TEXTURES_PROPERTY, new Property(TEXTURES_PROPERTY, encoded));
        return profile;
    }
}
