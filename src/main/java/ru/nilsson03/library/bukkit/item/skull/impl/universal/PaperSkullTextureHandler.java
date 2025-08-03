package ru.nilsson03.library.bukkit.item.skull.impl.universal;

import org.bukkit.inventory.meta.SkullMeta;
import ru.nilsson03.library.bukkit.item.skull.AbstractSkullTextureHandler;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.UUID;

public class PaperSkullTextureHandler extends AbstractSkullTextureHandler {

    private static final Class<?> bukkit;
    private static final Class<?> profilePropertyСlass;
    private static final Method createProfile;
    private static final Class<?> playerProfileClass;
    private static final Method setPlayerProfile;
    private static final Method setProperty;
    private static final Method getPlayerProfile;
    private static final Method getProperties;

    static {
        try {
            bukkit = Class.forName("org.bukkit.Bukkit");
            playerProfileClass = Class.forName("com.destroystokyo.paper.profile.PlayerProfile");
            profilePropertyСlass = Class.forName("com.destroystokyo.paper.profile.ProfileProperty");
            Class<?> skullMeta = SkullMeta.class;

            createProfile = bukkit.getMethod("createProfile", UUID.class, String.class);
            setPlayerProfile = skullMeta.getMethod("setPlayerProfile", playerProfileClass);
            setProperty = playerProfileClass.getMethod("setProperty", profilePropertyСlass);
            getPlayerProfile = skullMeta.getMethod("getPlayerProfile");
            getProperties = playerProfileClass.getMethod("getProperties");

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void applyTexture(SkullMeta skullMeta, String texture) {
        try {
            Object playerProfileObject = createProfile.invoke(null, UUID.randomUUID(), "Steve");
            setProperty.invoke(playerProfileObject, profilePropertyСlass.getConstructor(String.class, String.class).newInstance("textures", texture));
            setPlayerProfile.invoke(skullMeta, playerProfileObject);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
            ConsoleLogger.error("baselibrary", "Failed to apply texture to skull, reason: ", e.getMessage());
            throw new RuntimeException("Failed to process skull texture", e);
        }
    }

    @Override
    public String getTexture(SkullMeta skullMeta) {
        try {
            Object playerProfileObject = getPlayerProfile.invoke(skullMeta);
            if (playerProfileObject != null) {
                Collection<?> properties = (Collection<?>) getProperties.invoke(playerProfileObject);

                for (Object property : properties) {
                    Method getNameMethod = property.getClass().getDeclaredMethod("getName");
                    String name = (String) getNameMethod.invoke(property);

                    if ("textures".equals(name)) {
                        Method getValueMethod = property.getClass().getDeclaredMethod("getValue");
                        return (String) getValueMethod.invoke(property);
                    }
                }
            } else {
                return "Default Minecraft Head"; // if player profile is null
            }
        } catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            ConsoleLogger.error("baselibrary", "Failed to retrieve texture from skull, reason: ", e.getMessage());
        }
        return null;
    }
}
