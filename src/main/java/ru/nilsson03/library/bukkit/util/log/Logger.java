package ru.nilsson03.library.bukkit.util.log;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import ru.nilsson03.library.BaseLibrary;
import ru.nilsson03.library.bukkit.util.ServerVersion;
import ru.nilsson03.library.bukkit.util.ServerVersionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Logger {

    private static final Map<JavaPlugin, Logger> initializationMap = new ConcurrentHashMap<>();

    private final JavaPlugin plugin;

    private BufferedWriter writer;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public Logger(JavaPlugin plugin) {

        if (initializationMap.containsKey(plugin)) {
            throw new IllegalStateException("Another instance of " + plugin.getName() + " is already initialized.");
        }

        this.plugin = plugin;

        initializationMap.put(plugin, this);
    }

    public void initialize() {
        File logsFolder = new File(plugin.getDataFolder(), "logs");
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        String fileName = "logs_" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
        File logFile = new File(logsFolder, fileName);

        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(logFile, true));
            log(LogLevel.INFO, "Successful integration of the logging system with the %s plugin. Server Details: %s version, server core: %s", plugin.getName(), ServerVersionUtils.getServerVersion().name(), Bukkit.getName());
        } catch (IOException e) {
            ConsoleLogger.error(plugin, "The log file could not be created due to %s", e.getMessage());
        }
    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            ConsoleLogger.error(plugin, "An error occurred when closing the event log file, the reason is: %s", e.getMessage());
        }
    }

    public void log(LogLevel level, String message, Object... objects) {
        try {
            String formattedMessage = String.format(
                    "[%s] [%s] %s",
                    dateFormat.format(new Date()),
                    level.name(),
                    String.format(message, objects)
            );

            writer.write(formattedMessage);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            Bukkit.getConsoleSender().sendMessage("An error occurred when writing to the event log: " +  e.getMessage());
        }
    }
}
