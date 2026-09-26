package ru.nilsson03.library.bukkit.item;

import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Access to Paper's raw ItemStack byte serialization while retaining Spigot API compatibility.
 */
public final class ItemStackBytes {

    private ItemStackBytes() {
    }

    public static byte[] serialize(ItemStack item) {
        try {
            Method method = ItemStack.class.getMethod("serializeAsBytes");
            return (byte[]) method.invoke(item);
        } catch (NoSuchMethodException exception) {
            return null;
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalArgumentException("Cannot serialize ItemStack as raw bytes", exception);
        }
    }

    public static ItemStack deserialize(byte[] bytes) {
        try {
            Method method = ItemStack.class.getMethod("deserializeBytes", byte[].class);
            return (ItemStack) method.invoke(null, (Object) bytes);
        } catch (NoSuchMethodException exception) {
            throw new IllegalStateException("Server does not support raw ItemStack byte serialization", exception);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalArgumentException("Cannot deserialize ItemStack raw bytes", exception);
        }
    }
}
