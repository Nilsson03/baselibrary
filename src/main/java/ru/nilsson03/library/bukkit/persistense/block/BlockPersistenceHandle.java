package ru.nilsson03.library.bukkit.persistense.block;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

public class BlockPersistenceHandle implements Listener {

    @NotNull
    private final BlockPersistence persistence;

    public BlockPersistenceHandle(@NotNull  BlockPersistence blockPersistence) {
        this.persistence = blockPersistence;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (persistence.has(block)) {
            BlockData blockData = persistence.remove(block);
            if (persistence.hasConsumerOnDelete()) {
                persistence.getConsumerOnDelete().accept(blockData);
            }
        }
    }
}
