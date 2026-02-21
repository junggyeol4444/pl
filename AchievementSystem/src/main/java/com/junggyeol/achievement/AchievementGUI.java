package com.junggyeol.achievement;

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

import java.util.*;

public class AchievementGUI implements Listener {
    private static final String TITLE_PREFIX = "업적 목록";
    private final AchievementPlugin plugin;

    public AchievementGUI(AchievementPlugin plugin) { this.plugin = plugin; }

    public static void open(Player player, AchievementPlugin plugin, int page) {
        List<Achievement> list = new ArrayList<>(plugin.getAchievementManager().getAchievements().values());
        int pageSize = 45;
        int totalPages = Math.max(1, (list.size() + pageSize - 1) / pageSize);
        page = Math.min(page, totalPages - 1);

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + " (" + (page + 1) + "/" + totalPages + ")");
        PlayerData data = plugin.getAchievementManager().getPlayerData(player);

        int start = page * pageSize;
        int end = Math.min(start + pageSize, list.size());
        for (int i = start; i < end; i++) {
            Achievement ac = list.get(i);
            boolean completed = data.isCompleted(ac.getId());
            Material mat = completed ? Material.LIME_WOOL : Material.RED_WOOL;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((completed ? ChatColor.GREEN : ChatColor.RED) + ac.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + ac.getDescription());
            lore.add(ChatColor.YELLOW + "카테고리: " + ac.getCategory().name());
            if (!completed && ac.getRequiredCount() > 1) {
                lore.add(ChatColor.AQUA + "진행: " + data.getProgress(ac.getId()) + "/" + ac.getRequiredCount());
            }
            if (completed) lore.add(ChatColor.GREEN + "✔ 달성 완료");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i - start, item);
        }

        // Navigation
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pm = prev.getItemMeta();
            pm.setDisplayName(ChatColor.WHITE + "이전 페이지");
            prev.setItemMeta(pm);
            inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nm = next.getItemMeta();
            nm.setDisplayName(ChatColor.WHITE + "다음 페이지");
            next.setItemMeta(nm);
            inv.setItem(53, next);
        }

        // Stats
        long completedCount = plugin.getAchievementManager().getAchievements().values().stream()
                .filter(a -> data.isCompleted(a.getId())).count();
        ItemStack stats = new ItemStack(Material.BOOK);
        ItemMeta sm = stats.getItemMeta();
        sm.setDisplayName(ChatColor.GOLD + "내 업적 현황");
        sm.setLore(List.of(ChatColor.YELLOW + "달성: " + completedCount + "/" + plugin.getAchievementManager().getAchievements().size()));
        stats.setItemMeta(sm);
        inv.setItem(49, stats);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE_PREFIX)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String name = clicked.getItemMeta().getDisplayName();
        if (name.equals(ChatColor.WHITE + "이전 페이지") || name.equals(ChatColor.WHITE + "다음 페이지")) {
            int currentPage = 0;
            try {
                String[] parts = title.split("[(/)]");
                currentPage = Integer.parseInt(parts[parts.length - 2].trim()) - 1;
            } catch (Exception ignored) {}
            if (name.contains("이전")) open(player, plugin, currentPage - 1);
            else open(player, plugin, currentPage + 1);
        }
    }
}
