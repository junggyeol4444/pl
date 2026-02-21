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

public class RankingGUI implements Listener {
    private static final String MAIN_TITLE = "랭킹 메뉴";
    private static final String RANK_PREFIX = "랭킹: ";
    private final StatPlugin plugin;

    public RankingGUI(StatPlugin plugin) { this.plugin = plugin; }

    public static void openMain(Player player, StatPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_TITLE);
        List<String> statIds = new ArrayList<>(plugin.getConfig().getConfigurationSection("stat-categories").getKeys(false));
        for (int i = 0; i < statIds.size() && i < 45; i++) {
            String statId = statIds.get(i);
            String displayName = plugin.getStatManager().getStatDisplayName(statId);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + displayName);
            meta.setLore(List.of(ChatColor.GRAY + "클릭하여 랭킹 확인", ChatColor.DARK_GRAY + "stat:" + statId));
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
        player.openInventory(inv);
    }

    public static void open(Player player, StatPlugin plugin, String statId) {
        String displayName = plugin.getStatManager().getStatDisplayName(statId);
        Inventory inv = Bukkit.createInventory(null, 54, RANK_PREFIX + statId);
        List<Map.Entry<String, Long>> top = plugin.getStatManager().getTopPlayers(statId, 10);

        for (int i = 0; i < top.size(); i++) {
            Map.Entry<String, Long> entry = top.get(i);
            ItemStack item = new ItemStack(i == 0 ? Material.GOLD_INGOT : (i == 1 ? Material.IRON_INGOT : Material.COPPER_INGOT));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + (i + 1) + ". " + entry.getKey());
            meta.setLore(List.of(ChatColor.WHITE + displayName + ": " + StatGUI.formatStat(statId, entry.getValue())));
            item.setItemMeta(meta);
            inv.setItem(i * 5 + 2, item);
        }

        ItemStack title = new ItemStack(Material.GOLDEN_HELMET);
        ItemMeta tm = title.getItemMeta();
        tm.setDisplayName(ChatColor.GOLD + displayName + " 랭킹");
        title.setItemMeta(tm);
        inv.setItem(4, title);

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(ChatColor.WHITE + "뒤로");
        back.setItemMeta(bm);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        List<String> lore = meta.getLore();

        if (title.equals(MAIN_TITLE) && lore != null) {
            for (String line : lore) {
                String stripped = ChatColor.stripColor(line);
                if (stripped.startsWith("stat:")) {
                    open(player, plugin, stripped.substring(5));
                    return;
                }
            }
        } else if (title.startsWith(RANK_PREFIX)) {
            String name = ChatColor.stripColor(meta.getDisplayName());
            if (name.equals("뒤로")) openMain(player, plugin);
        }
    }
}
