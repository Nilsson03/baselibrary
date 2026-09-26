package ru.nilsson03.library.bukkit.item.builder.impl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpigotItemBuilderTest {

    @Test
    void buildDoesNotRoundTripUnchangedItemMeta() {
        ItemStack source = mock(ItemStack.class);
        ItemStack storedClone = mock(ItemStack.class);
        ItemStack resultClone = mock(ItemStack.class);
        when(source.clone()).thenReturn(storedClone);
        when(storedClone.clone()).thenReturn(resultClone);

        ItemStack result = new SpigotItemBuilder(source).build();

        assertSame(resultClone, result);
        verify(source, never()).getItemMeta();
        verify(storedClone, never()).getItemMeta();
        verify(storedClone, never()).setItemMeta(any());
    }

    @Test
    void buildWritesExplicitlyReplacedMeta() {
        ItemStack source = mock(ItemStack.class);
        ItemStack storedClone = mock(ItemStack.class);
        ItemStack resultClone = mock(ItemStack.class);
        ItemMeta replacement = mock(ItemMeta.class);
        when(source.clone()).thenReturn(storedClone);
        when(storedClone.clone()).thenReturn(resultClone);

        ItemStack result = new SpigotItemBuilder(source)
                .setMeta(replacement)
                .build();

        assertSame(resultClone, result);
        verify(storedClone).setItemMeta(replacement);
    }
}
