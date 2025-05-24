package ru.nilsson03.library.bukkit.item.builder;

import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public interface SkullItemBuilder {
    SkullItemBuilder setSkinTexture(String texture);
    SkullItemBuilder setDisplayName(String displayName);
    SkullItemBuilder setLore(List<String> lore);
    SkullItemBuilder addLoreLine(String line);
    SkullItemBuilder setOwner(OfflinePlayer owner);
    ItemStack build();
}
