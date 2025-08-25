package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import ru.nilsson03.library.bukkit.item.builder.SkullItemBuilder;
import ru.nilsson03.library.bukkit.item.skull.SkullTextureHandler;
import ru.nilsson03.library.text.api.UniversalTextApi;

import java.util.ArrayList;
import java.util.List;

public class UniversalSkullBuilder implements SkullItemBuilder {
    private final ItemStack skullItem;
    private final SkullMeta skullMeta;
    private final SkullTextureHandler textureHandler;

    public UniversalSkullBuilder(SkullTextureHandler textureHandler) {
        this.textureHandler = textureHandler;
        this.skullItem = new ItemStack(Material.PLAYER_HEAD);
        this.skullMeta = (SkullMeta) skullItem.getItemMeta();
    }

    @Override
    public SkullItemBuilder setSkinTexture(String textureUrl) {
        textureHandler.applyTexture(skullMeta, textureUrl);
        return this;
    }

    @Override
    public SkullItemBuilder setDisplayName(String displayName) {
        skullMeta.setDisplayName(UniversalTextApi.colorize(displayName));
        return this;
    }

    @Override
    public SkullItemBuilder setLore(List<String> lore) {
        skullMeta.setLore(UniversalTextApi.colorize(lore));
        return this;
    }

    @Override
    public SkullItemBuilder addLoreLine(String line) {
        List<String> lore = skullMeta.getLore() != null ? skullMeta.getLore() : new ArrayList<>();
        lore.add(UniversalTextApi.colorize(line));
        skullMeta.setLore(lore);
        return this;
    }

    @Override
    public SkullItemBuilder setOwner(OfflinePlayer owner) {
        skullMeta.setOwningPlayer(owner);
        return this;
    }

    @Override
    public ItemStack build() {
        skullItem.setItemMeta(skullMeta);
        return skullItem;
    }
}
