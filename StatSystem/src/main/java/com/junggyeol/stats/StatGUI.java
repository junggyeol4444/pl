package com.junggyeol.stats;

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

public class StatGUI implements Listener {
    private static final String TITLE_PREFIX = "통계: ";
    private final StatPlugin plugin;

    public StatGUI(StatPlugin plugin) { this.plugin = plugin; }

    public static void open(Player player, StatPlugin plugin) {
        open(player, plugin, player);
    }

    public static void open(Player viewer, StatPlugin plugin, Player target) {
        PlayerStatData data = plugin.getStatManager().getPlayerData(target);
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + target.getName());
        List<String> statIds = new ArrayList<>(plugin.getConfig().getConfigurationSection("stat-categories").getKeys(false));

        Material[] icons = {
                Material.CLOCK, Material.LEATHER_BOOTS, Material.ZOMBIE_HEAD, Material.SKELETON_SKULL,
                Material.DIAMOND_PICKAXE, Material.BRICKS, Material.CRAFTING_TABLE, Material.DIAMOND_SWORD,
                Material.SHIELD, Material.FISHING_ROD, Material.PLAYER_HEAD, Material.TOTEM_OF_UNDYING,
                Material.PAPER, Material.CHEST, Material.END_PORTAL_FRAME, Material.ENCHANTING_TABLE
        };

        for (int i = 0; i < statIds.size() && i < 45; i++) {
            String statId = statIds.get(i);
            long value = data.getStat(statId);
            String displayName = plugin.getStatManager().getStatDisplayName(statId);
            Material icon = i < icons.length ? icons[i] : Material.PAPER;
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + displayName);
            String formatted = formatStat(statId, value);
            meta.setLore(List.of(ChatColor.WHITE + formatted));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }

        // Play time special display
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + target.getName() + "의 통계");
        im.setLore(List.of(
                ChatColor.GRAY + "연속 접속: " + data.getLoginStreak() + "일",
                ChatColor.GRAY + "총 접속: " + data.getStat("login_count") + "회"
        ));
        info.setItemMeta(im);
        inv.setItem(49, info);

        viewer.openInventory(inv);
    }

    public static String formatStat(String statId, long value) {
        if (statId.equals("play_time")) {
            long hours = value / 3600;
            long minutes = (value % 3600) / 60;
            return hours + "시간 " + minutes + "분";
        }
        if (statId.equals("walk_distance")) return value + " 블록";
        return String.valueOf(value);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getView().getTitle().startsWith(TITLE_PREFIX)) return;
        event.setCancelled(true);
    }
}
