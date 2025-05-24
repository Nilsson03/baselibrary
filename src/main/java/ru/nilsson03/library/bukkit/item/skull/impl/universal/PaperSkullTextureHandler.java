package ru.nilsson03.library.bukkit.item.skull.impl.universal;

import org.bukkit.inventory.meta.SkullMeta;
import ru.nilsson03.library.bukkit.item.skull.AbstractSkullTextureHandler;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

public class PaperSkullTextureHandler extends AbstractSkullTextureHandler {

    private static final String PROFILE_CLASS_PATH = "org.bukkit.craftbukkit.%s.CraftProfile";
    private static final String TEXTURES_METHOD = "setTextures";
    private static final String PROFILE_FIELD = "profile";

    @Override
    public void applyTexture(SkullMeta skullMeta, String textureUrl) {
        validateInput(skullMeta, textureUrl);

        try {
            URL url = validateAndCreateUrl(textureUrl);
            Class<?> profileClass = Class.forName(String.format(PROFILE_CLASS_PATH, ServerVersionUtils.NMS_VERSION));

            Object profile = profileClass.getConstructor().newInstance();
            Method setTexturesMethod = profileClass.getDeclaredMethod(TEXTURES_METHOD, URL.class);
            setTexturesMethod.setAccessible(true);
            setTexturesMethod.invoke(profile, url);

            Field profileField = skullMeta.getClass().getDeclaredField(PROFILE_FIELD);
            profileField.setAccessible(true);
            profileField.set(skullMeta, profile);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid texture URL format", e);
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "Failed to apply texture to skull, reason: ", e.getMessage());
            throw new RuntimeException("Failed to process skull texture", e);
        }
    }

    @Override
    public String getTexture(SkullMeta skullMeta) {
        try {
            Field profileField = skullMeta.getClass().getDeclaredField(PROFILE_FIELD);
            profileField.setAccessible(true);
            Object profile = profileField.get(skullMeta);

            Method getTexturesMethod = profile.getClass().getDeclaredMethod(TEXTURES_METHOD);
            getTexturesMethod.setAccessible(true);
            URL textureUrl = (URL) getTexturesMethod.invoke(profile);

            return textureUrl != null ? textureUrl.toString() : null;
        } catch (Exception e) {
            ConsoleLogger.error("baselibrary", "Failed to retrieve texture from skull, reason: ", e.getMessage());
            return null;
        }
    }
}
