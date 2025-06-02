package ru.nilsson03.library.bukkit.util.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import ru.nilsson03.library.bukkit.integration.PluginDependency;
import ru.nilsson03.library.bukkit.util.loc.Cuboid;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class RegionRestorer {
    private final Plugin plugin;
    private final File backupsFolder;
    private final ConcurrentHashMap<String, BukkitTask> pendingRestores = new ConcurrentHashMap<>();

    public RegionRestorer(Plugin plugin) {
        this.plugin = plugin;
        this.backupsFolder = new File(plugin.getDataFolder(), "region_backups");
        if (!backupsFolder.exists()) {
            backupsFolder.mkdirs();
        }
    }

    /**
     * Запланировать восстановление региона
     * @param cuboid Регион для восстановления
     * @param delayMinutes время до восстановления в минутах
     */
    @PluginDependency(name = "WorldEdit", minVersion = "7.2.9", skipIfUnavailable = true)
    public void scheduleRegionRestore(Cuboid cuboid, String uniqueName, long delayMinutes) {
        Location center = cuboid.getCenter();
        World world = cuboid.getWorld();
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        cancelPendingRestore(uniqueName);

        File backupFile = createBackupFile(uniqueName);
        saveRegionToFile(cuboid, backupFile);

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            restoreRegionFromFile(weWorld, uniqueName, backupFile);
            pendingRestores.remove(uniqueName);
        }, delayMinutes * 60 * 20L);

        pendingRestores.put(uniqueName, task);
    }

    private File createBackupFile(String uniqueName) {
        String timestamp = Instant.now().toString().replace(":", "-");
        return new File(backupsFolder, uniqueName + "_" + timestamp + ".schem");
    }

    @PluginDependency(name = "WorldEdit", minVersion = "7.2.9", skipIfUnavailable = true)
    private void saveRegionToFile(Cuboid cuboid, File backupFile) {
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(cuboid.getWorld());
        BlockVector3 min = BlockVector3.at(cuboid.getMinX(), cuboid.getMinY(), cuboid.getMinZ());
        BlockVector3 max = BlockVector3.at(cuboid.getMaxX(), cuboid.getMaxY(), cuboid.getMaxZ());
        Region region = new CuboidRegion(
                world,
                min,
                max
        );

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(region.getWorld())) {
            ForwardExtentCopy copy = new ForwardExtentCopy(
                    editSession, region, clipboard, region.getMinimumPoint()
            );
            Operations.complete(copy);

            try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(Files.newOutputStream(backupFile.toPath()))) {
                writer.write(clipboard);
            }
        } catch (IOException | WorldEditException e) {
            ConsoleLogger.error("baselobrary", "Failed to save region backup (ex %s)", e.getMessage());
        }
    }

    @PluginDependency(name = "WorldEdit", minVersion = "7.2.9", skipIfUnavailable = true)
    private void restoreRegionFromFile(com.sk89q.worldedit.world.World world, String uniqueName, File backupFile) {
        ClipboardFormat format = ClipboardFormats.findByFile(backupFile);
        if (format == null) {
            ConsoleLogger.warn("baselibrary", "Invalid backup format for region %s", uniqueName);
            return;
        }

        try (ClipboardReader reader = format.getReader(Files.newInputStream(backupFile.toPath()))) {
            Clipboard clipboard = reader.read();
            BlockVector3 pastePos = clipboard.getOrigin();

            try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(pastePos)
                        .ignoreAirBlocks(false)
                        .build();

                Operations.complete(operation);
            }

            backupFile.delete();
        } catch (Exception e) {
            ConsoleLogger.error("baselobrary", "Failed to restore region %s (ex %s)", uniqueName, e.getMessage());
        }
    }

    /**
     * Отменяет запланированное восстановление
     */
    public void cancelPendingRestore(String uniqueName) {
        BukkitTask task = pendingRestores.get(uniqueName);
        if (task != null) {
            task.cancel();
            pendingRestores.remove(uniqueName);
        }
    }

    /**
     * Очищает все запланированные восстановления
     */
    public void cancelAllRestores() {
        pendingRestores.values().forEach(BukkitTask::cancel);
        pendingRestores.clear();
    }
}
