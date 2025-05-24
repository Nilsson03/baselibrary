package ru.nilsson03.library.bukkit.util;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class SoundUtils {

    public static void playSound(Player player, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            try {
                player.playSound(player.getLocation(), soundName, volume, pitch);
            } catch (Exception ex) {
                ConsoleLogger.warn("baselibrary", "Couldn't find the %s sound. Check the configuration.", soundName);
            }
        }
    }

    public static void playSound(Player player, String soundName) {
        playSound(player, soundName, 1.0f, 1.0f);
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void playSound(Player player, Sound sound) {
        playSound(player, sound, 1.0f, 1.0f);
    }
}
