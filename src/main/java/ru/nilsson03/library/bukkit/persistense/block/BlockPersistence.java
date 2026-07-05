package ru.nilsson03.library.bukkit.persistense.block;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

@Getter
public class BlockPersistence {

    @NotNull
    private static final Map<NPlugin, Map<String, BlockData>> dataContainer = new ConcurrentHashMap<>();

    @NotNull
    private final NPlugin plugin;
    @Nullable
    private final Consumer<BlockData> consumerOnDelete;
    @Nullable
    private final Runnable actionOnLoad;

    private final BukkitTask periodSaveTask;

    public BlockPersistence(@NotNull NPlugin plugin, @Nullable Consumer<BlockData> consumerOnDelete,
            @Nullable Runnable actionOnLoad) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cant be null!");
        this.consumerOnDelete = consumerOnDelete;
        this.actionOnLoad = actionOnLoad;
        this.periodSaveTask = startSaveTask();
        Bukkit.getPluginManager().registerEvents(new BlockPersistenceHandle(this), plugin);
        load();
    }

    public BlockPersistence(@NotNull NPlugin plugin, @Nullable Consumer<BlockData> consumerOnDelete) {
        this(plugin, consumerOnDelete, null);
    }

    public BlockPersistence(@NotNull NPlugin plugin, @Nullable Runnable actionOnLoad) {
        this(plugin, null, actionOnLoad);
    }

    public BlockPersistence(@NotNull NPlugin plugin) {
        this(plugin, null, null);
    }

    private BukkitTask startSaveTask() {
        BukkitTask bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            save();
        }, 0, 20 * 60 * 60); // Раз в чаc
        ConsoleLogger.debug(plugin,
                "Запущена периодическая задача сохранения данных кастомных блоков. (Период: 1 час)");
        return bukkitTask;
    }

    public void set(Block block, String key, String value) {
        Objects.requireNonNull(block, "Block cant be null!");
        Objects.requireNonNull(key, "Key cant be null!");
        Objects.requireNonNull(value, "Value cant be null!");

        if (key.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Key or value cant be empty!");
        }

        String blockKey = getBlockKey(block);

        Map<String, BlockData> pluginData = dataContainer.computeIfAbsent(plugin,
                k -> new ConcurrentHashMap<>());

        pluginData.compute(blockKey, (k, existing) -> {
            if (existing == null) {
                existing = new BlockData(block);
            }
            existing.set(key, value);
            return existing;
        });
    }

    @Nullable
    public BlockData get(Block block) {
        Objects.requireNonNull(block, "Block cant be null!");

        String blockKey = getBlockKey(block);
        Map<String, BlockData> pluginData = dataContainer.get(plugin);
        if (pluginData == null) {
            return null;
        }
        return pluginData.get(blockKey);
    }

    @Nullable
    public BlockData remove(Block block) {
        Objects.requireNonNull(block, "Block cant be null!");

        String blockKey = getBlockKey(block);
        Map<String, BlockData> pluginData = dataContainer.get(plugin);
        if (pluginData == null) {
            return null;
        }

        BlockData removed = pluginData.remove(blockKey);
        if (removed != null && consumerOnDelete != null) {
            consumerOnDelete.accept(removed);
        }
        return removed;
    }

    public boolean has(Block block) {
        return get(block) != null;
    }

    private String getBlockKey(Block block) {
        return block.getWorld().getName() + ":" +
                block.getX() + ":" +
                block.getY() + ":" +
                block.getZ();
    }

    private void saveSnapshot(Set<BlockData> snapshot) {
        File dataFile = new File(plugin.getDataFolder(), "blockdata.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            Set<SerializableBlockData> serializableData = new HashSet<>();
            for (BlockData blockData : snapshot) {
                serializableData.add(new SerializableBlockData(blockData));
            }
            oos.writeObject(serializableData);
            ConsoleLogger.info(plugin.getName(), "Saved %s block data entries", serializableData.size());
        } catch (IOException e) {
            ConsoleLogger.info(plugin.getName(), "Failed to save block data %s", e.getMessage());
        }
    }

    public void save() {
        Map<String, BlockData> pluginData = dataContainer.get(plugin);
        if (pluginData == null || pluginData.isEmpty()) {
            saveSnapshot(Collections.emptySet());
            return;
        }
        Set<BlockData> snapshot = new HashSet<>(pluginData.values());
        saveSnapshot(snapshot);
    }

    @SuppressWarnings("unchecked")
    public void load() {
        File dataFile = new File(plugin.getDataFolder(), "blockdata.dat");
        if (!dataFile.exists()) {
            return;
        }

        Map<String, BlockData> pluginData = dataContainer.computeIfAbsent(plugin,
                k -> new ConcurrentHashMap<>());
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Set<SerializableBlockData> serializableData = (Set<SerializableBlockData>) ois.readObject();
            for (SerializableBlockData sbd : serializableData) {
                BlockData blockData = sbd.toBlockData();
                if (blockData != null) {
                    String blockKey = getBlockKey(blockData.toBlock());
                    pluginData.put(blockKey, blockData);
                }
            }
            if (hasActionOnLoad()) {
                actionOnLoad.run();
            }
            ConsoleLogger.info(plugin.getName(), "Loaded %s block data entries", pluginData.size());
        } catch (IOException | ClassNotFoundException e) {
            ConsoleLogger.error(plugin.getName(), "Failed to load block data %s", e.getMessage());
        }
    }

    public boolean hasActionOnLoad() {
        return actionOnLoad != null;
    }

    public boolean hasConsumerOnDelete() {
        return consumerOnDelete != null;
    }

    private static class SerializableBlockData implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        @NotNull
        private final String blockKey;
        @NotNull
        private final Map<String, Object> data;

        public SerializableBlockData(BlockData blockData) {
            this.blockKey = blockData.blockKey();
            this.data = new HashMap<>(blockData.data());
        }

        @Nullable
        public BlockData toBlockData() {
            try {
                String[] parts = blockKey.split(":");
                if (parts.length != 4) {
                    return null;
                }

                String worldName = parts[0];
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);

                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    return null;
                }

                Block block = world.getBlockAt(x, y, z);
                BlockData blockData = new BlockData(block);
                data.forEach(blockData::set);
                return blockData;
            } catch (Exception e) {
                return null;
            }
        }
    }
}
