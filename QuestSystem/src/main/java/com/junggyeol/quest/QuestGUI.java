package com.junggyeol.quest;

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
import java.util.stream.Collectors;

public class QuestGUI implements Listener {
    private static final String TITLE_PREFIX = "퀘스트 목록";
    private final QuestPlugin plugin;
    public QuestGUI(QuestPlugin plugin) { this.plugin = plugin; }

    public static void open(Player player, QuestPlugin plugin, String filter, int page) {
        List<Quest> filtered = plugin.getQuestManager().getQuests().values().stream()
                .filter(q -> filter.equals("ALL") || q.getPeriod().name().equals(filter))
                .collect(Collectors.toList());

        int pageSize = 36;
        int totalPages = Math.max(1, (filtered.size() + pageSize - 1) / pageSize);
        page = Math.min(page, totalPages - 1);

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + ":" + filter + ":" + page);
        PlayerQuestData data = plugin.getQuestManager().getPlayerData(player);

        int start = page * pageSize;
        for (int i = start; i < Math.min(start + pageSize, filtered.size()); i++) {
            Quest quest = filtered.get(i);
            boolean done = isCompletedForPeriod(data, quest);
            Material mat = done ? Material.LIME_WOOL : Material.YELLOW_WOOL;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((done ? ChatColor.GREEN : ChatColor.YELLOW) + quest.getName());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + quest.getDescription());
            lore.add(ChatColor.AQUA + "유형: " + quest.getPeriod().name());
            if (!done) {
                lore.add(ChatColor.WHITE + "진행: " + data.getProgress(quest.getId()) + "/" + quest.getRequiredCount());
            } else {
                lore.add(ChatColor.GREEN + "✔ 완료");
            }
            lore.add(ChatColor.GOLD + "보상: 경험치 " + quest.getReward().getXp() + " + 스킬포인트 " + quest.getReward().getSkillPoints());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i - start, item);
        }

        // Filter buttons
        addFilterButton(inv, 45, Material.RED_WOOL, "일일 퀘스트", "DAILY");
        addFilterButton(inv, 46, Material.BLUE_WOOL, "주간 퀘스트", "WEEKLY");
        addFilterButton(inv, 47, Material.PURPLE_WOOL, "특별 퀘스트", "SPECIAL");
        addFilterButton(inv, 48, Material.WHITE_WOOL, "전체 퀘스트", "ALL");

        if (page > 0) { ItemStack prev = navItem(Material.ARROW, "이전"); inv.setItem(51, prev); }
        if (page < totalPages - 1) { ItemStack next = navItem(Material.ARROW, "다음"); inv.setItem(53, next); }

        player.openInventory(inv);
    }

    private static boolean isCompletedForPeriod(PlayerQuestData data, Quest quest) {
        return switch (quest.getPeriod()) {
            case DAILY -> data.getCompletedToday().contains(quest.getId());
            case WEEKLY -> data.getCompletedThisWeek().contains(quest.getId());
            case SPECIAL -> data.isCompleted(quest.getId());
        };
    }

    private static void addFilterButton(Inventory inv, int slot, Material mat, String name, String filter) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        meta.setLore(List.of(ChatColor.GRAY + "filter:" + filter));
        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }

    private static ItemStack navItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.startsWith(TITLE_PREFIX)) return;
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore != null) {
            for (String line : lore) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("filter:")) {
                    String filter = stripped.substring(7);
                    open(player, plugin, filter, 0);
                    return;
                }
            }
        }
        String[] parts = title.split(":");
        String filter = parts.length > 1 ? parts[1] : "ALL";
        int page = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
        String name = ChatColor.stripColor(meta.getDisplayName());
        if (name.equals("이전")) open(player, plugin, filter, page - 1);
        else if (name.equals("다음")) open(player, plugin, filter, page + 1);
    }
}
