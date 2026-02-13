package ru.nilsson03.library.bukkit.persistense.block;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

@Getter
public class BlockPersistence {

    @NotNull
    private static final Map<NPlugin, Set<BlockData>> dataContainer = new HashMap<>();

    @NotNull
    private final NPlugin plugin;
    @Nullable
    private final Consumer<BlockData> consumerOnDelete;
    @Nullable
    private final Runnable actionOnLoad;

    public BlockPersistence(@NotNull NPlugin plugin, @Nullable Consumer<BlockData> consumerOnDelete, @Nullable Runnable actionOnLoad) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cant be null!");
        this.consumerOnDelete = consumerOnDelete;
        this.actionOnLoad = actionOnLoad;
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

    public void set(@NotNull Block block, @NotNull String key, @NotNull String value) {
        Objects.requireNonNull(block, "Block cant be null!");
        Objects.requireNonNull(key, "Key cant be null!");
        Objects.requireNonNull(value, "Value cant be null!");

        if (key.isEmpty() || value.isEmpty()) {
            throw new IllegalArgumentException("Key or value cant be empty!");
        }

        Set<BlockData> blockDataSet = dataContainer.computeIfAbsent(plugin, k -> new HashSet<>());
        
        BlockData existingData = blockDataSet.stream()
                .filter(bd -> bd.equals(block))
                .findFirst()
                .orElse(null);

        if (existingData != null) {
            existingData.set(key, value);
        } else {
            BlockData newData = new BlockData(block);
            newData.set(key, value);
            blockDataSet.add(newData);
        }
    }

    @Nullable
    public BlockData get(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cant be null!");

        Set<BlockData> blockDataSet = dataContainer.get(plugin);
        if (blockDataSet == null) {
            return null;
        }

        return blockDataSet.stream()
                .filter(bd -> bd.equals(block))
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public BlockData remove(@NotNull Block block) {
        Objects.requireNonNull(block, "Block cant be null!");
        
        Set<BlockData> blockDataSet = dataContainer.get(plugin);
        if (blockDataSet == null) {
            return null;
        }

        BlockData foundData = blockDataSet.stream()
                .filter(bd -> bd.equals(block))
                .findFirst()
                .orElse(null);

        if (foundData != null) {
            blockDataSet.remove(foundData);
        }

        return foundData;
    }

    public boolean has(@NotNull Block block) {
        return get(block) != null;
    }

    public void save() {
        File dataFile = new File(plugin.getDataFolder(), "blockdata.dat");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
            Set<BlockData> allData = dataContainer.get(plugin);
            if (allData == null) {
                oos.writeObject(new HashSet<SerializableBlockData>());
                return;
            }

            Set<SerializableBlockData> serializableData = new HashSet<>();
            for (BlockData blockData : allData) {
                serializableData.add(new SerializableBlockData(blockData));
            }
            oos.writeObject(serializableData);
            ConsoleLogger.info(plugin.getName(), "Saved %s block data entries", serializableData.size());
        } catch (IOException e) {
            ConsoleLogger.info(plugin.getName(), "Failed to save block data %s", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {
        File dataFile = new File(plugin.getDataFolder(), "blockdata.dat");
        if (!dataFile.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
            Set<SerializableBlockData> serializableData = (Set<SerializableBlockData>) ois.readObject();
            Set<BlockData> blockDataSet = dataContainer.computeIfAbsent(plugin, k -> new HashSet<>());

            for (SerializableBlockData sbd : serializableData) {
                BlockData blockData = sbd.toBlockData();
                if (blockData != null) {
                    blockDataSet.add(blockData);
                }
            }
            if (hasActionOnLoad()) {
                actionOnLoad.run();
            }
            ConsoleLogger.info(plugin.getName(), "Loaded %s block data entries", blockDataSet.size());
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
