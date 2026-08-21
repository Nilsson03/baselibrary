package ru.nilsson03.library.bukkit.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.text.api.UniversalTextApi;

/** Optional NeuroTech integration without a hard class-loading dependency. */
public final class NeuroTechItemUtil {
    private static final String ITEM_MANAGER = "com.nexwave.neuro.items.ItemManager";

    private NeuroTechItemUtil() {
    }

    public static String resolveDisplayName(ItemStack item) {
        Plugin neuroTech = Bukkit.getPluginManager().getPlugin("NeuroTech");
        if (item == null || neuroTech == null || !neuroTech.isEnabled()) {
            return null;
        }

        try {
            Class<?> managerClass = neuroTech.getClass().getClassLoader().loadClass(ITEM_MANAGER);
            Object manager = managerClass.getField("INSTANCE").get(null);
            String id = (String) managerClass
                    .getMethod("resolveCustomItemId", ItemStack.class)
                    .invoke(manager, item);
            if (id == null) return null;

            Object customItem = managerClass.getMethod("getItemById", String.class)
                    .invoke(manager, id);
            if (customItem == null) return null;

            String name = (String) customItem.getClass().getMethod("getName").invoke(customItem);
            return name == null || name.isBlank() ? null : UniversalTextApi.colorize(name);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return null;
        }
    }

    public static ItemStack applyDisplayName(ItemStack source) {
        if (source == null) return null;
        ItemStack item = source.clone();
        String displayName = resolveDisplayName(item);
        if (displayName == null) return item;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
        return item;
    }
}
