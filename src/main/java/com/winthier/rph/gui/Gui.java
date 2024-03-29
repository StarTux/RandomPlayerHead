package com.winthier.rph.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class Gui implements InventoryHolder {
    public static final int OUTSIDE = -999;
    private static JavaPlugin plugin;
    private Inventory inventory;
    private Map<Integer, Slot> slots = new HashMap<>();
    private Consumer<InventoryCloseEvent> onClose = null;
    private Consumer<InventoryOpenEvent> onOpen = null;
    @Getter @Setter private boolean editable = false;
    @Getter private int size = 3 * 9;
    @Getter private Component title = Component.empty();
    private boolean locked = false;
    private static final Map<UUID, Gui> GUI_MAP = new HashMap<>();

    @RequiredArgsConstructor @AllArgsConstructor
    private static final class Slot {
        final int index;
        ItemStack item;
        Consumer<InventoryClickEvent> onClick;
    }

    public Gui() { }

    public Gui title(Component newTitle) {
        title = newTitle;
        return this;
    }

    public Gui size(int newSize) {
        if (newSize <= 0 || newSize % 9 != 0) {
            throw new IllegalArgumentException("newSize=" + newSize);
        }
        size = newSize;
        return this;
    }

    public Gui rows(int rowCount) {
        if (rowCount <= 0) throw new IllegalArgumentException("rowCount=" + rowCount);
        size = rowCount * 9;
        return this;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.getServer().createInventory(this, size, title);
            for (int i = 0; i < size; i += 1) {
                Slot slot = slots.get(i);
                if (slot != null) {
                    inventory.setItem(i, slot.item);
                }
            }
        }
        return inventory;
    }

    public ItemStack getItem(int index) {
        if (index < 0) index = OUTSIDE;
        Slot slot = slots.get(index);
        return slot != null
            ? slot.item
            : null;
    }

    public void setItem(int index, ItemStack item) {
        setItem(index, item, null);
    }

    public void setItem(int index, ItemStack item, Consumer<InventoryClickEvent> responder) {
        if (inventory != null && index >= 0 && inventory.getSize() > index) {
            inventory.setItem(index, item);
        }
        if (index < 0) index = OUTSIDE;
        Slot slot = new Slot(index, item, responder);
        slots.put(index, slot);
    }

    public void setItem(int column, int row, ItemStack item, Consumer<InventoryClickEvent> responder) {
        if (column < 0 || column > 8) {
            throw new IllegalArgumentException("column=" + column);
        }
        if (row < 0) throw new IllegalArgumentException("row=" + row);
        setItem(column + row * 9, item, responder);
    }

    public Gui open(Player player) {
        player.openInventory(getInventory());
        return this;
    }

    public Gui reopen(Player player) {
        player.closeInventory();
        inventory = null;
        player.openInventory(getInventory());
        return this;
    }

    public Gui onClose(Consumer<InventoryCloseEvent> responder) {
        onClose = responder;
        return this;
    }

    public Gui onOpen(Consumer<InventoryOpenEvent> responder) {
        onOpen = responder;
        return this;
    }

    public Gui clear() {
        if (inventory != null) inventory.clear();
        slots.clear();
        onOpen = null;
        onClose = null;
        return this;
    }

    private void onInventoryOpen(InventoryOpenEvent event) {
        if (onOpen != null) {
            Bukkit.getScheduler().runTask(plugin, () -> onOpen.accept(event));
        }
    }

    private void onInventoryClose(InventoryCloseEvent event) {
        if (onClose != null) {
            Bukkit.getScheduler().runTask(plugin, () -> onClose.accept(event));
        }
    }

    private void onInventoryClick(InventoryClickEvent event) {
        if (!editable) {
            event.setCancelled(true);
        }
        if (locked) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != null && !event.getClickedInventory().equals(inventory)) {
            return;
        }
        Slot slot = slots.get(event.getSlot());
        if (slot != null && slot.onClick != null) {
            locked = true;
            Bukkit.getScheduler().runTask(plugin, () -> {
                    locked = false;
                    slot.onClick.accept(event);
                });
        }
    }

    private void onInventoryDrag(InventoryDragEvent event) {
        if (!editable) {
            event.setCancelled(true);
        }
    }

    @RequiredArgsConstructor
    public static final class EventListener implements Listener {
        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryOpen(final InventoryOpenEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryOpen(event);
            } else if (event.getPlayer() instanceof Player player) {
                Gui gui = Gui.of(player);
                if (gui != null) gui.onInventoryOpen(event);
            }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryClose(final InventoryCloseEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryClose(event);
            } else if (event.getPlayer() instanceof Player player) {
                Gui gui = Gui.of(player);
                if (gui != null) {
                    GUI_MAP.remove(player.getUniqueId());
                    gui.onInventoryClose(event);
                }
            }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryClick(final InventoryClickEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryClick(event);
            } else if (event.getWhoClicked() instanceof Player player) {
                Gui gui = Gui.of(player);
                if (gui != null) gui.onInventoryClick(event);
            }
        }

        @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
        void onInventoryDrag(final InventoryDragEvent event) {
            if (event.getInventory().getHolder() instanceof Gui) {
                ((Gui) event.getInventory().getHolder()).onInventoryDrag(event);
            } else if (event.getWhoClicked() instanceof Player player) {
                Gui gui = Gui.of(player);
                if (gui != null) gui.onInventoryDrag(event);
            }
        }

        @EventHandler
        void onPluginDisable(PluginDisableEvent event) {
            if (event.getPlugin() == plugin) {
                Gui.disable();
            }
        }
    }

    public void doNotOpen(Player player) {
        player.closeInventory();
        GUI_MAP.put(player.getUniqueId(), this);
    }

    public static Gui of(Player player) {
        Gui mapped = GUI_MAP.get(player.getUniqueId());
        if (mapped != null) return mapped;
        InventoryView view = player.getOpenInventory();
        if (view == null) return null;
        Inventory topInventory = view.getTopInventory();
        if (topInventory == null) return null;
        InventoryHolder holder = topInventory.getHolder();
        if (holder instanceof Gui gui) {
            return gui;
        }
        return null;
    }

    public static void enable(JavaPlugin thePlugin) {
        plugin = thePlugin;
        Bukkit.getPluginManager().registerEvents(new EventListener(), plugin);
    }

    public static void disable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            Gui gui = Gui.of(player);
            if (gui != null) player.closeInventory();
        }
    }
}
