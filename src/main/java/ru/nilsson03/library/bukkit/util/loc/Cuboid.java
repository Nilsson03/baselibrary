package ru.nilsson03.library.bukkit.util.loc;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.*;

public class Cuboid implements Iterable<Block>, Cloneable {
    private final int xMin;
    private final int xMax;
    private final int yMin;
    private final int yMax;
    private final int zMin;
    private final int zMax;
    private final String worldName;

    public Cuboid(Location one, Location two) {
        if (!one.getWorld().equals(two.getWorld())) {
            throw new IllegalArgumentException("Locations must be in the same world");
        }

        this.worldName = one.getWorld().getName();
        this.xMin = Math.min(one.getBlockX(), two.getBlockX());
        this.yMin = Math.min(one.getBlockY(), two.getBlockY());
        this.zMin = Math.min(one.getBlockZ(), two.getBlockZ());
        this.xMax = Math.max(one.getBlockX(), two.getBlockX());
        this.yMax = Math.max(one.getBlockY(), two.getBlockY());
        this.zMax = Math.max(one.getBlockZ(), two.getBlockZ());
    }

    public int getMaxX() {
        return xMax;
    }

    public int getMinX() {
        return xMin;
    }

    public int getMaxY() {
        return yMax;
    }

    public int getMinY() {
        return yMin;
    }

    public int getMaxZ() {
        return zMax;
    }

    public int getMinZ() {
        return zMin;
    }

    public Location getMinimumPoint() {
        return new Location(Bukkit.getWorld(worldName), xMin, yMin, zMin);
    }

    public Location getMaximumPoint() {
        return new Location(Bukkit.getWorld(worldName), xMax, yMax, zMax);
    }

    // Основной метод проверки содержания
    private boolean contains(String world, int x, int y, int z) {
        return this.worldName.equalsIgnoreCase(world)
                && x >= this.xMin && x <= this.xMax
                && y >= this.yMin && y <= this.yMax
                && z >= this.zMin && z <= this.zMax;
    }

    public boolean contains(Location loc) {
        return loc != null && contains(loc.getWorld().getName(),
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public boolean isInside(Location location) {
        return contains(location);
    }

    public boolean hasPlayerInside(Player player) {
        return player != null && contains(player.getLocation());
    }

    public boolean hasBlockInside(Block block) {
        return block != null && contains(block.getLocation());
    }

    // Оптимизированное получение чанков
    public Set<Chunk> getChunks() {
        Set<Chunk> chunks = new HashSet<>();
        World world = getWorld();
        if (world == null) return chunks;

        int minChunkX = xMin >> 4;
        int maxChunkX = xMax >> 4;
        int minChunkZ = zMin >> 4;
        int maxChunkZ = zMax >> 4;

        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        return chunks;
    }

    @Override
    public Iterator<Block> iterator() {
        return new CuboidBlockIterator(getWorld(), xMin, xMax, yMin, yMax, zMin, zMax);
    }

    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>((xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1));
        for (Block block : this) {
            blocks.add(block);
        }
        return blocks;
    }

    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    public Location getCenter() {
        return new Location(getWorld(),
                xMin + (xMax - xMin) / 2.0,
                yMin + (yMax - yMin) / 2.0,
                zMin + (zMax - zMin) / 2.0);
    }

    // Дополнительные полезные методы
    public boolean intersects(Cuboid other) {
        if (!worldName.equals(other.worldName)) return false;
        return xMin <= other.xMax && xMax >= other.xMin &&
                yMin <= other.yMax && yMax >= other.yMin &&
                zMin <= other.zMax && zMax >= other.zMin;
    }

    public long getVolume() {
        return (long) (xMax - xMin + 1) * (yMax - yMin + 1) * (zMax - zMin + 1);
    }

    @Override
    public String toString() {
        return "Cuboid: " + this.xMin + "," + this.yMin + "," + this.zMin + "=>" + this.xMax + "," + this.yMax + ","
                + this.zMax;
    }

    private static class CuboidBlockIterator implements Iterator<Block> {
        private final World world;
        private final int minY;
        private final int minZ;
        private final int maxX, maxY, maxZ;
        private int x, y, z;

        public CuboidBlockIterator(World world, int xMin, int xMax, int yMin, int yMax, int zMin, int zMax) {
            this.world = world;
            this.minY = yMin;
            this.minZ = zMin;
            this.maxX = xMax;
            this.maxY = yMax;
            this.maxZ = zMax;
            this.x = xMin; // Начинаем с минимальных координат
            this.y = yMin;
            this.z = zMin;
        }

        @Override
        public boolean hasNext() {
            return x <= maxX && y <= maxY && z <= maxZ;
        }

        @Override
        public Block next() {
            if (!hasNext()) throw new NoSuchElementException();

            Block block = world.getBlockAt(x, y, z);

            if (++z > maxZ) {
                z = minZ;
                if (++y > maxY) {
                    y = minY;
                    x++;
                }
            }

            return block;
        }
    }
}
