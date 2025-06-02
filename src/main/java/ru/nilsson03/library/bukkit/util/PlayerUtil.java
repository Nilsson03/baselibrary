package ru.nilsson03.library.bukkit.util;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PlayerUtil {

    public static boolean hasItem(Player player, ItemStack itemStack, int amount) {
        Preconditions.checkArgument(player != null && player.isOnline(), "Player can't be null and must be online");
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");
        Objects.requireNonNull(itemStack, "ItemStack can't be null");

        if (player.getInventory().isEmpty()) return false;

        return player.getInventory().containsAtLeast(itemStack, amount);
    }

    public static void giveItem(Player player, ItemStack itemStack) {
        Preconditions.checkArgument(player != null && player.isOnline(), "Player can't be null and must be online");
        Objects.requireNonNull(itemStack, "ItemStack can't be null");

        if (itemStack.getType() == Material.AIR) {
            ConsoleLogger.debug("baselibrary", "The item given to the player cannot be air.");
            return;
        }

        if (player.getInventory().firstEmpty() == -1)
            Objects.requireNonNull(Bukkit.getWorld(player.getWorld().getName())).dropItem(player.getLocation(), itemStack);
        else
            player.getInventory().addItem(itemStack);
    }

    public static int getItemsAmount(Player player, ItemStack itemStack) {
        Preconditions.checkArgument(player != null && player.isOnline(), "Player can't be null and must be online");
        Objects.requireNonNull(itemStack, "ItemStack can't be null");

        return Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item ->  item.isSimilar(itemStack))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }


    public static void giveItems(Player player, List<ItemStack> itemStacks) {
        Preconditions.checkArgument(player != null && player.isOnline(), "Player can't be null and must be online");
        Objects.requireNonNull(itemStacks, "ItemStacks can't be null");

        if (itemStacks.isEmpty()) {
            ConsoleLogger.debug("baselibrary", "The list of items passed to the giveItem() method is empty, and the action has been canceled.");
            return;
        }

        itemStacks.stream()
                .filter(Objects::nonNull)
                .filter(itemStack -> itemStack.getType() != Material.AIR)
                .forEach(itemStack -> {
            if (player.getInventory().firstEmpty() == -1)
                Objects.requireNonNull(Bukkit.getWorld(player.getWorld().getName())).dropItem(player.getLocation(), itemStack);
            else
                player.getInventory().addItem(itemStack);
        });
    }

    public static boolean removeItem(Player player, ItemStack itemStack, int amount) {
        Preconditions.checkArgument(player != null && player.isOnline(), "Player can't be null and must be online");
        Objects.requireNonNull(itemStack, "ItemStack can't be null");
        Preconditions.checkArgument(amount > 0, "Amount must be greater than 0");

        PlayerInventory inventory = player.getInventory();

        if (!inventory.containsAtLeast(itemStack, amount)) {
            return false;
        }

        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(itemStack)) {
                int itemAmount = item.getAmount();
                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    player.getInventory().removeItem(item);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
        return true;
    }
}
