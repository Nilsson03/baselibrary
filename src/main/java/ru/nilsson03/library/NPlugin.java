package ru.nilsson03.library;

import org.bukkit.plugin.java.JavaPlugin;
import ru.nilsson03.library.bukkit.file.BukkitDirectory;
import ru.nilsson03.library.bukkit.file.FileRepository;
import ru.nilsson03.library.bukkit.integration.Integration;
import ru.nilsson03.library.bukkit.notify.PlayerNotificationService;
import ru.nilsson03.library.bukkit.scheduler.TaskScheduler;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Optional;

public abstract class NPlugin extends JavaPlugin {
    private BaseLibrary baseLibrary;
    private static Integration integration;
    private PlayerNotificationService notificationService;
    private TaskScheduler taskScheduler;
    private FileRepository fileRepository;

    @Override
    public final void onEnable() {
        try {
            this.baseLibrary = BaseLibrary.getInstance();
            if (baseLibrary == null) {
                getLogger().severe("Failed to load NPlugin because BaseLibrary is not initialized");
                throw new IllegalStateException("BaseLibrary not initialized");
            }

            boolean writeLogs = getConfig().getBoolean("writeLogs", false);
            ConsoleLogger.register(this, writeLogs);
            integration = new Integration(this);
            fileRepository = new FileRepository(this);
            notificationService = new PlayerNotificationService();
            taskScheduler = new TaskScheduler(this);
            enable();
            ConsoleLogger.info(this, "%s plugin loaded successfully.", getDescription().getName());
        } catch (Exception e) {
            ConsoleLogger.error(baseLibrary, "Failed to enable %s plugin, reason: %s", getDescription().getName(), e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onDisable() {
        try {
            disable();
            ConsoleLogger.unregister(this);
            getLogger().info(getDescription().getName() + " disabled!");
        } catch (Exception e) {
            ConsoleLogger.error(baseLibrary, "Failed to disable %s plugin, reason: %s", getDescription().getName(), e.getMessage());
        }
    }

    public BukkitDirectory getDirectory(String name) {
        Optional<BukkitDirectory> optionalBukkitDirectory = this.fileRepository.getDirectoryOrLoad(name);
        if (optionalBukkitDirectory.isPresent()) {
            return optionalBukkitDirectory.get();
        }

        ConsoleLogger.debug(this, "Couldn't load %s directory", name);
        throw new IllegalStateException("Couldn't load directory " + name);
    }

    /**
     * Получить экземпляр BaseLibrary
     */
    protected BaseLibrary getBaseLibrary() {
        return baseLibrary;
    }

    public Integration integration() {
        return integration;
    }

    /**
     * Абстрактный метод для включения плагина
     */
    protected abstract void enable();

    /**
     * Метод для отключения плагина (может быть переопределен)
     */
    protected void disable() {}

    public PlayerNotificationService notificationService() {
        return notificationService;
    }

    public TaskScheduler taskScheduler() {
        return taskScheduler;
    }

    public FileRepository fileRepository() {
        return fileRepository;
    }
}
