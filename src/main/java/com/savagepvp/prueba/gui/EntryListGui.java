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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EntryListGui implements Listener {
    private static final String TITLE = ChatColor.DARK_AQUA + "Registros guardados";
    private static final int PAGE_SIZE = 45;

    private final JavaPlugin plugin;

    public EntryListGui(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void open(Player player, List<KeyValueEntry> entries, int page) {
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) PAGE_SIZE));
        int safePage = Math.max(0, Math.min(page, totalPages - 1));
        Inventory inventory = Bukkit.createInventory(null, 54, TITLE + ChatColor.GRAY + " " + (safePage + 1) + "/" + totalPages);

        int start = safePage * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, entries.size());
        for (int i = start; i < end; i++) {
            KeyValueEntry entry = entries.get(i);
            inventory.setItem(i - start, createEntryItem(entry));
        }

        inventory.setItem(48, createItem(Material.ARROW, ChatColor.YELLOW + "Página anterior",
                Collections.singletonList(ChatColor.GRAY + "Clic para volver.")));
        inventory.setItem(49, createItem(Material.BOOK, ChatColor.AQUA + "Registros",
                Collections.singletonList(ChatColor.GRAY + "Total: " + entries.size())));
        inventory.setItem(50, createItem(Material.ARROW, ChatColor.YELLOW + "Página siguiente",
                Collections.singletonList(ChatColor.GRAY + "Clic para avanzar.")));
        player.openInventory(inventory);
    }

    private ItemStack createEntryItem(KeyValueEntry entry) {
        String value = entry.getValue();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Value:");
        addWrapped(lore, value, 45);
        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Clic para ver en el chat.");
        return createItem(Material.PAPER, ChatColor.GOLD + entry.getKey(), lore);
    }

    private void addWrapped(List<String> lines, String text, int width) {
        if (text.isEmpty()) {
            lines.add(ChatColor.WHITE + "");
            return;
        }
        for (int index = 0; index < text.length(); index += width) {
            lines.add(ChatColor.WHITE + text.substring(index, Math.min(index + width, text.length())));
            if (lines.size() >= 6) break;
        }
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
        int slot = event.getRawSlot();
        if (slot < 0 || slot >= event.getInventory().getSize()) return;

        List<KeyValueEntry> entries = plugin.getKeyValueService().findAll().join();
        int totalPages = Math.max(1, (int) Math.ceil(entries.size() / (double) PAGE_SIZE));
        int currentPage = parsePage(event.getInventory().getTitle());

        if (slot == 48 && currentPage > 0) {
            open(player, entries, currentPage - 1);
        } else if (slot == 50 && currentPage + 1 < totalPages) {
            open(player, entries, currentPage + 1);
        } else if (slot < PAGE_SIZE && slot < entries.size() - currentPage * PAGE_SIZE) {
            KeyValueEntry entry = entries.get(currentPage * PAGE_SIZE + slot);
            MessageUtil.send(player, "&6" + entry.getKey() + " &8» &f" + entry.getValue());
        }
    }

    private int parsePage(String title) {
        int space = title.lastIndexOf(' ');
        int slash = title.lastIndexOf('/');
        if (space < 0 || slash < space) return 0;
        try { return Math.max(0, Integer.parseInt(title.substring(space + 1, slash)) - 1); }
        catch (NumberFormatException ignored) { return 0; }
    }
}
