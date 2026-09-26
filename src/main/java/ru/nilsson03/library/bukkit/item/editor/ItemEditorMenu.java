package ru.nilsson03.library.bukkit.item.editor;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.item.builder.impl.SpigotItemBuilder;
import ru.nilsson03.library.invui.gui.Gui;
import ru.nilsson03.library.invui.gui.PagedGui;
import ru.nilsson03.library.invui.gui.ScrollGui;
import ru.nilsson03.library.invui.gui.structure.Markers;
import ru.nilsson03.library.invui.inventory.VirtualInventory;
import ru.nilsson03.library.invui.item.Item;
import ru.nilsson03.library.invui.item.ItemProvider;
import ru.nilsson03.library.invui.item.impl.AutoUpdateItem;
import ru.nilsson03.library.invui.item.impl.SuppliedItem;
import ru.nilsson03.library.invui.item.impl.controlitem.ScrollItem;
import ru.nilsson03.library.invui.window.Window;
import ru.nilsson03.library.text.messeger.UniversalMessenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * Reusable in-game item editor. Its layout and interaction intentionally match
 * the editor used by dangeons: left click edits, right click removes, the 45
 * slot add screen supports multiple items, and a pending replacement item is
 * returned to the player when the window closes.
 *
 * @param <C> category key type (world, rarity, event, case, ...)
 */
public final class ItemEditorMenu<C> {
    private final ItemEditorRepository<C> repository;
    private final ItemEditorCategoryProvider<C> categories;
    private final ItemEditorRarityLimits rarityLimits;

    public ItemEditorMenu(ItemEditorRepository<C> repository,
                           ItemEditorCategoryProvider<C> categories) {
        this(repository, categories, ItemEditorRarityLimits.fromLibraryConfig());
    }

    /** Creates an editor with explicit rarity bounds, independent of global config. */
    public ItemEditorMenu(ItemEditorRepository<C> repository,
                           ItemEditorCategoryProvider<C> categories,
                           ItemEditorRarityLimits rarityLimits) {
        if (repository == null || categories == null) throw new IllegalArgumentException("editor dependencies cannot be null");
        this.repository = repository;
        this.categories = categories;
        this.rarityLimits = rarityLimits == null ? ItemEditorRarityLimits.fromLibraryConfig() : rarityLimits;
    }

    /** Opens the category selector. Use {@link #openItems(Player, Object)} when a plugin has no selector. */
    public void open(Player player) {
        List<Item> content = new ArrayList<>();
        java.util.Collection<? extends C> available = categories.getCategories(player);
        if (available == null) available = java.util.Collections.emptyList();
        for (C category : available) {
            content.add(new SuppliedItem(
                    () -> (ItemProvider) locale -> categories.createIcon(category, safeItems(category).size()),
                    click -> {
                        openItems(player, category);
                        return true;
                    }));
        }

        SuppliedItem close = button(Material.BARRIER, "§cЗакрыть",
                lore("§7Нажмите, чтобы закрыть меню"), player::closeInventory);

        PagedGui<Item> gui = PagedGui.items()
                .setStructure(
                        "f f f f f f f f f",
                        "f x x x x x x x f",
                        "f x x x x x x x f",
                        "f x x x x x x x f",
                        "f f f f c f f f f")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('f', filler())
                .addIngredient('c', close)
                .setContent(content)
                .build();

        Window.single().setViewer(player).setTitle(categories.getCategoryTitle()).setGui(gui).build().open();
    }

    /** Opens one category and starts at the first line. */
    public void openItems(Player player, C category) {
        openItems(player, category, 0, () -> open(player));
    }

    /** Opens one category preserving the scroll line when returning from a child menu. */
    public void openItems(Player player, C category, int scrollLine) {
        openItems(player, category, scrollLine, () -> open(player));
    }

    /** Opens a category with an integration-specific back action (for example, a plugin setup menu). */
    public void openItems(Player player, C category, int scrollLine, Runnable backAction) {
        AtomicInteger currentLine = new AtomicInteger(Math.max(0, scrollLine));
        List<Item> content = new ArrayList<>();
        for (ItemEditorEntry entry : safeItems(category)) {
            content.add(new SuppliedItem(
                    () -> (ItemProvider) locale -> display(entry),
                    click -> {
                        ClickType type = click.getClickType();
                        if (type == ClickType.RIGHT || type == ClickType.SHIFT_RIGHT) {
                            repository.remove(category, entry);
                            UniversalMessenger.send(player, "§a✔ §fПредмет удалён");
                            openItems(player, category, currentLine.get());
                        } else {
                            openEditor(player, category, entry,
                                    () -> openItems(player, category, currentLine.get()));
                        }
                        return true;
                    }));
        }

        SuppliedItem back = button(Material.RED_STAINED_GLASS_PANE, "§6Назад",
                lore("§7Нажмите, чтобы вернуться"), backAction == null ? () -> open(player) : backAction);
        SuppliedItem close = button(Material.BARRIER, "§cЗакрыть",
                lore("§7Нажмите, чтобы закрыть меню"), player::closeInventory);
        SuppliedItem add = button(Material.LIME_CONCRETE, "§aДобавить предметы", lore(
                "", "§7Нажмите, чтобы перейти к добавлению", "§7предметов в эту категорию", "",
                "§8▸ §eНажмите, чтобы перейти"),
                () -> openAdd(player, category, currentLine.get()));

        ScrollGui<Item> gui = ScrollGui.items()
                .setStructure(
                        "f f f f f f f f f",
                        "f x x x x x x x f",
                        "f x x x x x x x f",
                        "f x x x x x x x f",
                        "d f f f f f f f u",
                        "b f f f c f f f a")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('u', scrollButton(-1))
                .addIngredient('d', scrollButton(1))
                .addIngredient('f', filler())
                .addIngredient('b', back)
                .addIngredient('c', close)
                .addIngredient('a', add)
                .setContent(content)
                .build();
        gui.addScrollHandler((from, to) -> currentLine.set(to));
        if (scrollLine > 0) {
            gui.setCurrentLine(Math.min(scrollLine, gui.getMaxLine()));
            currentLine.set(gui.getCurrentLine());
        }
        Window.single().setViewer(player).setTitle(categories.getItemsTitle(category)).setGui(gui).build().open();
    }

    /** Opens the detailed editor for an existing entry. */
    public void openEditor(Player player, C category, ItemEditorEntry entry, Runnable openAfterClose) {
        AtomicReference<Double> chance = new AtomicReference<>(entry.getChance());
        AtomicInteger rarity = new AtomicInteger(rarityLimits.clamp(entry.getRarity()));
        AtomicInteger minAmount = new AtomicInteger(entry.getMinAmount());
        AtomicInteger maxAmount = new AtomicInteger(entry.getMaxAmount());
        VirtualInventory replacement = new VirtualInventory(1);

        ItemProvider replacementHint = locale -> new SpigotItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .setDisplayName("§6Положите новый предмет в данный слот")
                .setLore(lore("", "§7Затем нажмите §eОбновить предмет")).build();
        SuppliedItem preview = new SuppliedItem(() -> (ItemProvider) locale -> entry.getItem(), null);
        AutoUpdateItem info = new AutoUpdateItem(10, () -> (ItemProvider) locale -> new SpigotItemBuilder(Material.BOOK)
                .setDisplayName("§6Текущие настройки")
                .setLore(lore("", "§7Параметры предмета слева", "", "§8▪ §fШанс: §e" + formatChance(chance.get()) + "%",
                        "§8▪ §fРедкость: §e" + rarity.get(), "§8▪ §fКоличество: §e" + minAmount.get() + "-" + maxAmount.get())).build());

        SuppliedItem back = button(Material.RED_STAINED_GLASS_PANE, "§6Назад", lore("§7Нажмите, чтобы вернуться"), openAfterClose);
        SuppliedItem delete = button(Material.BARRIER, "§cУдалить предмет",
                lore("", "§7Нажмите, чтобы удалить предмет из категории"), () -> {
                    repository.remove(category, entry);
                    UniversalMessenger.send(player, "§a✔ §fПредмет удалён");
                    openAfterClose.run();
                });
        SuppliedItem save = button(Material.EMERALD, "§aСохранить", lore("", "§7Нажмите, чтобы сохранить изменения"), () -> {
            applyValues(entry, chance, rarity, minAmount, maxAmount);
            repository.save(category, entry);
            UniversalMessenger.send(player, "§a✔ §fПредмет сохранён §7(шанс §e" + formatChance(chance.get())
                    + "%§7, редкость §e" + rarity.get() + "§7, кол-во §e" + minAmount.get() + "-" + maxAmount.get() + "§7)");
            openAfterClose.run();
        });
        SuppliedItem updateItem = new SuppliedItem(
                () -> (ItemProvider) locale -> new SpigotItemBuilder(Material.ANVIL).setDisplayName("§6Обновить предмет")
                        .setLore(lore("", "§7Положите новый предмет", "§7в специальный слот рядом", "", "§7Шанс, редкость и количество сохранятся")).build(),
                click -> {
                    ItemStack newItem = replacement.getItem(0);
                    if (newItem == null || newItem.getType().isAir()) {
                        UniversalMessenger.send(player, "§cПоложите новый предмет в данный слот");
                        return true;
                    }
                    entry.setItem(newItem);
                    applyValues(entry, chance, rarity, minAmount, maxAmount);
                    replacement.setItem(null, 0, null);
                    repository.save(category, entry);
                    UniversalMessenger.send(player, "§a✔ §fПредмет обновлён, настройки сохранены");
                    openAfterClose.run();
                    return true;
                });

        ItemStack filler = filler();
        Gui gui = Gui.normal().setStructure(
                        "f f f f f f f f f", "f 1 5 T F . i . f", "f a e t u . s . f",
                        "f R r . n m . N M", "f f f f f f f f f", "f . k v p U q . f")
                .addIngredient('f', filler).addIngredient('i', preview).addIngredient('s', info)
                .addIngredient('1', chanceButton(Material.LIME_STAINED_GLASS_PANE, "§a+1% шанс", 1, chance))
                .addIngredient('5', chanceButton(Material.LIME_STAINED_GLASS_PANE, "§a+5% шанс", 5, chance))
                .addIngredient('T', chanceButton(Material.LIME_STAINED_GLASS_PANE, "§a+10% шанс", 10, chance))
                .addIngredient('F', chanceButton(Material.LIME_STAINED_GLASS_PANE, "§a+25% шанс", 25, chance))
                .addIngredient('a', chanceButton(Material.RED_STAINED_GLASS_PANE, "§c-1% шанс", -1, chance))
                .addIngredient('e', chanceButton(Material.RED_STAINED_GLASS_PANE, "§c-5% шанс", -5, chance))
                .addIngredient('t', chanceButton(Material.RED_STAINED_GLASS_PANE, "§c-10% шанс", -10, chance))
                .addIngredient('u', chanceButton(Material.RED_STAINED_GLASS_PANE, "§c-25% шанс", -25, chance))
                .addIngredient('R', intButton(Material.LIME_DYE, "§a+1 редкость", rarity,
                        v -> rarityLimits.clamp(v + 1)))
                .addIngredient('r', intButton(Material.RED_DYE, "§c-1 редкость", rarity,
                        v -> rarityLimits.clamp(v - 1)))
                .addIngredient('n', amountButton(Material.LIME_CONCRETE, "§a+1 min", minAmount, maxAmount, true, 1))
                .addIngredient('m', amountButton(Material.RED_CONCRETE, "§c-1 min", minAmount, maxAmount, true, -1))
                .addIngredient('N', amountButton(Material.LIME_WOOL, "§a+1 max", minAmount, maxAmount, false, 1))
                .addIngredient('M', amountButton(Material.RED_WOOL, "§c-1 max", minAmount, maxAmount, false, -1))
                .addIngredient('k', back).addIngredient('p', delete).addIngredient('q', save)
                .addIngredient('U', updateItem).addIngredient('v', replacement, replacementHint).build();

        Window.single().setViewer(player).setTitle("§8Редактирование предмета").setGui(gui)
                .addCloseHandler(() -> returnPendingItem(player, replacement)).build().open();
    }

    /** Opens the 45-slot multi-add screen. */
    public void openAdd(Player player, C category, int returnScrollLine) {
        VirtualInventory inventory = new VirtualInventory(45);
        SuppliedItem back = button(Material.RED_STAINED_GLASS_PANE, "§6Назад", lore("§7Нажмите, чтобы вернуться"),
                () -> openItems(player, category, returnScrollLine));
        SuppliedItem save = button(Material.EMERALD, "§aСохранить предметы", lore(
                "", "§7Нажмите, чтобы сохранить выбранные", "§7предметы в эту категорию", "", "§8▸ §eНажмите, чтобы сохранить"), () -> {
            int added = 0;
            for (ItemStack item : inventory.getItems()) {
                if (item != null && !item.getType().isAir()) {
                    repository.add(category, item.clone());
                    added++;
                }
            }
            UniversalMessenger.send(player, "§a✔ §fДобавлено предметов: §e" + added);
            openItems(player, category, returnScrollLine);
        });
        Gui gui = Gui.normal().setStructure(
                        "v v v v v v v v v", "v v v v v v v v v", "v v v v v v v v v",
                        "v v v v v v v v v", "v v v v v v v v v", ". . . . b . s . .")
                .addIngredient('v', inventory).addIngredient('b', back).addIngredient('s', save).build();
        Window.single().setViewer(player).setTitle("§8Добавление предметов §7| §f" + categories.getName(category))
                .setGui(gui).addCloseHandler(() -> returnPendingItems(player, inventory)).build().open();
    }

    private java.util.Collection<? extends ItemEditorEntry> safeItems(C category) {
        java.util.Collection<? extends ItemEditorEntry> result = repository.getItems(category);
        return result == null ? java.util.Collections.emptyList() : result;
    }

    private void applyValues(ItemEditorEntry entry, AtomicReference<Double> chance, AtomicInteger rarity,
                             AtomicInteger minAmount, AtomicInteger maxAmount) {
        entry.setChance(chance.get());
        entry.setRarity(rarityLimits.clamp(rarity.get()));
        entry.setAmounts(minAmount.get(), maxAmount.get());
    }

    private void returnPendingItem(Player player, VirtualInventory inventory) {
        ItemStack pending = inventory.getItem(0);
        if (pending == null) return;
        inventory.setItem(null, 0, null);
        player.getInventory().addItem(pending).values().stream().findFirst().ifPresent(player::setItemOnCursor);
    }

    private void returnPendingItems(Player player, VirtualInventory inventory) {
        for (ItemStack pending : inventory.getItems()) {
            if (pending == null || pending.getType().isAir()) continue;
            player.getInventory().addItem(pending).values().stream().findFirst().ifPresent(player::setItemOnCursor);
        }
    }

    private SuppliedItem chanceButton(Material material, String name, double delta, AtomicReference<Double> chance) {
        DoubleUnaryOperator op = value -> Math.max(0, Math.min(100, value + delta));
        return button(material, name, lore("", "§7Нажмите, чтобы изменить шанс на §f"
                + (delta > 0 ? "+" : "") + formatChance(delta) + "%"), () -> chance.updateAndGet(op::applyAsDouble));
    }

    private SuppliedItem intButton(Material material, String name, AtomicInteger value, IntUnaryOperator op) {
        return button(material, name, lore("", "§7Нажмите, чтобы изменить значение"), () -> value.updateAndGet(op));
    }

    private SuppliedItem amountButton(Material material, String name, AtomicInteger min, AtomicInteger max,
                                      boolean editMin, int delta) {
        return button(material, name, lore("", "§7Нажмите, чтобы изменить количество"), () -> {
            if (editMin) {
                int next = Math.max(1, min.get() + delta);
                min.set(next);
                if (max.get() < next) max.set(next);
            } else {
                max.set(Math.max(1, Math.max(min.get(), max.get() + delta)));
            }
        });
    }

    private SuppliedItem button(Material material, String name, List<String> lore, Runnable action) {
        return new SuppliedItem(() -> (ItemProvider) locale -> new SpigotItemBuilder(material)
                .setDisplayName(name).setLore(new ArrayList<>(lore)).build(), click -> {
            action.run();
            return true;
        });
    }

    private static ScrollItem scrollButton(int direction) {
        return new ScrollItem(direction) {
            @Override public ItemProvider getItemProvider(ScrollGui<?> gui) {
                return locale -> new SpigotItemBuilder(Material.ARROW)
                        .setDisplayName(direction < 0 ? "§6Вверх" : "§6Вниз").build();
            }
        };
    }

    private static ItemStack display(ItemEditorEntry entry) {
        return new SpigotItemBuilder(entry.getItem()).addLine("")
                .addLine("§8▪ §fШанс: §e" + formatChance(entry.getChance()) + "%")
                .addLine("§8▪ §fРедкость: §e" + entry.getRarity())
                .addLine("§8▪ §fКоличество: §e" + entry.getMinAmount() + "-" + entry.getMaxAmount())
                .addLine("").addLine("§8▹ §eЛКМ — редактировать").addLine("§8▹ §cПКМ — удалить").build();
    }

    private static ItemStack filler() { return new SpigotItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(" ").build(); }
    private static List<String> lore(String... lines) { return new ArrayList<>(Arrays.asList(lines)); }
    private static String formatChance(double value) { return Math.rint(value) == value ? String.valueOf((int) value) : String.format("%.1f", value); }
}
