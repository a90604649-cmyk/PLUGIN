package com.savagepvp.prueba.gui;

import com.savagepvp.prueba.model.KeyValueEntry;
import com.savagepvp.prueba.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class EntryListGui implements Listener {
    private static final String TITLE = ChatColor.DARK_AQUA + "Registros guardados";
    private static final int PAGE_SIZE = 45;
    private final JavaPlugin plugin;
    private final Map<UUID, Session> sessions = new ConcurrentHashMap<>();

    public EntryListGui(JavaPlugin plugin) { this.plugin = plugin; }
    public JavaPlugin getPlugin() { return plugin; }

    public void open(Player player, List<KeyValueEntry> entries, int page) {
        List<KeyValueEntry> snapshot = new ArrayList<>(entries);
        int totalPages = Math.max(1, (int) Math.ceil(snapshot.size() / (double) PAGE_SIZE));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        sessions.put(player.getUniqueId(), new Session(snapshot, safePage));

        Inventory inventory = Bukkit.createInventory(null, 54,
                TITLE + ChatColor.GRAY + " " + (safePage + 1) + "/" + totalPages);
        int start = safePage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, snapshot.size());
        for (int i = start; i < end; i++) inventory.setItem(i - start, createEntryItem(snapshot.get(i)));
        inventory.setItem(48, createItem(Material.ARROW, ChatColor.YELLOW + "Página anterior",
                Collections.singletonList(ChatColor.GRAY + "Clic para volver.")));
        inventory.setItem(49, createItem(Material.BOOK, ChatColor.AQUA + "Registros",
                Collections.singletonList(ChatColor.GRAY + "Total: " + snapshot.size())));
        inventory.setItem(50, createItem(Material.ARROW, ChatColor.YELLOW + "Página siguiente",
                Collections.singletonList(ChatColor.GRAY + "Clic para avanzar.")));
        player.openInventory(inventory);
    }

    private ItemStack createEntryItem(KeyValueEntry entry) {
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Value:");
        addWrapped(lore, entry.getValue(), 45);
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Clic para ver en el chat.");
        return createItem(Material.PAPER, ChatColor.GOLD + entry.getKey(), lore);
    }

    private void addWrapped(List<String> lines, String text, int width) {
        if (text.isEmpty()) { lines.add(ChatColor.WHITE + ""); return; }
        for (int index = 0; index < text.length() && lines.size() < 6; index += width)
            lines.add(ChatColor.WHITE + text.substring(index, Math.min(index + width, text.length())));
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().getTitle().startsWith(TITLE)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        Session session = sessions.get(player.getUniqueId());
        if (session == null) return;
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        int totalPages = Math.max(1, (int) Math.ceil(session.entries.size() / (double) PAGE_SIZE));
        if (slot == 48 && session.page > 0) {
            open(player, session.entries, session.page - 1);
        } else if (slot == 50 && session.page + 1 < totalPages) {
            open(player, session.entries, session.page + 1);
        } else if (slot < PAGE_SIZE) {
            int index = session.page * PAGE_SIZE + slot;
            if (index < session.entries.size()) {
                KeyValueEntry entry = session.entries.get(index);
                MessageUtil.send(player, "&6" + entry.getKey() + " &8» &f" + entry.getValue());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getTitle().startsWith(TITLE)) sessions.remove(event.getPlayer().getUniqueId());
    }

    private static final class Session {
        private final List<KeyValueEntry> entries;
        private final int page;
        private Session(List<KeyValueEntry> entries, int page) { this.entries = entries; this.page = page; }
    }
}
