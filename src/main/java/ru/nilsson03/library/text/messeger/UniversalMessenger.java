package ru.nilsson03.library.text.messeger;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.nilsson03.library.bukkit.bar.UniversalActionBar;
import ru.nilsson03.library.bukkit.util.SoundUtils;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ChatFormatter;
import ru.nilsson03.library.text.util.ReplaceData;

public class UniversalMessenger {
    private static final Pattern SOUND_PATTERN = Pattern.compile("^sound:(.+)$");
    private static final Pattern TITLE_PATTERN = Pattern.compile("^title:(.+)$");
    private static final Pattern ACTIONBAR_PATTERN = Pattern.compile("^actionbar:(.+)$");

    public static void send(CommandSender sender, String message, ReplaceData... replacements) {
        for (ReplaceData replacement : replacements) {
            message = message.replace(replacement.getKey(), replacement.getObject().toString());
        }
        
        if (sender instanceof Player player) {
            sendToPlayer(player, message);
        } else {
            sender.sendMessage(UniversalTextApi.colorize(message));
        }
    }

    public static void send(CommandSender sender, String message) {
        if (sender instanceof Player player) {
            sendToPlayer(player, message);
        } else {
            sender.sendMessage(UniversalTextApi.colorize(message));
        }
    }

    public static void send(Player sender, String message) {
        sendToPlayer(sender, message);
    }

    public static void send(CommandSender sender, List<String> messages) {
        messages.forEach(msg -> send(sender, msg));
    }

    private static void sendToPlayer(Player player, String message) {
        String[] parts = message.split(";");

        for (String part : parts) {
            if (SOUND_PATTERN.matcher(part).matches()) {
                handleSound(player, part.substring(6));
            } else if (TITLE_PATTERN.matcher(part).matches()) {
                handleTitle(player, part.substring(6));
            } else if (ACTIONBAR_PATTERN.matcher(part).matches()) {
                handleActionBar(player, part.substring(10));
            } else if (!part.trim().isEmpty()) {
                part = ChatFormatter.centerText(part);
                player.sendMessage(UniversalTextApi.colorize(part));
            }
        }
    }

    private static void handleSound(Player player, String soundData) {
        String[] args = soundData.split(",", 3);
        String soundName = args[0].trim();

        float volume = args.length > 1 ? Float.parseFloat(args[1].trim()) : 2.0f;
        float pitch = args.length > 2 ? Float.parseFloat(args[2].trim()) : 1.0f;

        SoundUtils.playSound(player, soundName, volume, pitch);
    }

    private static void handleTitle(Player player, String text) {
        String[] parts = text.split("\\|", 3);
        String title = parts[0];
        String subtitle = parts.length > 1 ? parts[1] : "";
        int fadeIn = parts.length > 2 ? Integer.parseInt(parts[2]) : 10;
        int stay = parts.length > 3 ? Integer.parseInt(parts[3]) : 40;
        int fadeOut = parts.length > 4 ? Integer.parseInt(parts[4]) : 10;

        player.sendTitle(
                UniversalTextApi.colorize(title),
                UniversalTextApi.colorize(subtitle),
                fadeIn, stay, fadeOut);
    }

    private static void handleActionBar(Player player, String text) {
        UniversalActionBar.send(player, UniversalTextApi.colorize(text));
    }
}
