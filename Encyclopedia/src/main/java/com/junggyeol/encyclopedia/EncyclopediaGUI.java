package com.junggyeol.encyclopedia;

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

public class EncyclopediaGUI implements Listener {
    private static final String MAIN_TITLE = "도감 메뉴";
    private static final String CAT_PREFIX = "도감: ";
    private final EncyclopediaPlugin plugin;

    public EncyclopediaGUI(EncyclopediaPlugin plugin) { this.plugin = plugin; }

    public static void openMain(Player player, EncyclopediaPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, MAIN_TITLE);
        String[] categories = {"ITEM", "MOB", "BIOME", "ACHIEVEMENT", "QUEST", "SKILL", "DIMENSION", "SUPPLY"};
        Material[] icons = {Material.DIAMOND, Material.ZOMBIE_HEAD, Material.GRASS_BLOCK,
                Material.GOLDEN_APPLE, Material.PAPER, Material.BLAZE_POWDER,
                Material.END_PORTAL_FRAME, Material.CHEST};
        int[] slots = {10, 12, 14, 16, 28, 30, 32, 34};

        for (int i = 0; i < categories.length; i++) {
            String cat = categories[i];
            Material icon;
            try { icon = icons[i]; } catch (ArrayIndexOutOfBoundsException e) { icon = Material.BOOK; }
            ItemStack item = new ItemStack(icon);
            ItemMeta meta = item.getItemMeta();
            String display = plugin.getManager().getCategoryDisplay(cat);
            meta.setDisplayName(ChatColor.AQUA + display);
            int total = plugin.getManager().getTotalEntries(cat);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "등록 항목: " + total + "개");
            meta.setLore(lore);
            item.setItemMeta(meta);
            if (i < slots.length) inv.setItem(slots[i], item);
        }

        // Info item
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "도감 안내");
        im.setLore(List.of(ChatColor.GRAY + "카테고리를 선택하세요."));
        info.setItemMeta(im);
        inv.setItem(49, info);

        player.openInventory(inv);
    }

    public static void openCategory(Player player, EncyclopediaPlugin plugin, String category, int page) {
        Map<String, EncyclopediaEntry> entries = plugin.getManager().getRegistry().getOrDefault(category, new LinkedHashMap<>());
        List<EncyclopediaEntry> list = new ArrayList<>(entries.values());
        int pageSize = 45;
        int totalPages = Math.max(1, (list.size() + pageSize - 1) / pageSize);
        page = Math.min(page, totalPages - 1);

        String catDisplay = plugin.getManager().getCategoryDisplay(category);
        Inventory inv = Bukkit.createInventory(null, 54, CAT_PREFIX + catDisplay + " (" + (page + 1) + "/" + totalPages + ")");

        int start = page * pageSize;
        int end = Math.min(start + pageSize, list.size());
        for (int i = start; i < end; i++) {
            EncyclopediaEntry entry = list.get(i);
            boolean discovered = plugin.getManager().isDiscovered(player, category, entry.getId());
            Material mat = discovered ? (entry.getIcon() != null ? entry.getIcon() : Material.LIME_WOOL) : Material.RED_STAINED_GLASS_PANE;
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((discovered ? ChatColor.GREEN : ChatColor.RED) + (discovered ? entry.getName() : "???"));
            List<String> lore = new ArrayList<>();
            if (discovered) {
                lore.add(ChatColor.GRAY + entry.getDescription());
                if (!entry.getHint().isEmpty()) lore.add(ChatColor.YELLOW + "힌트: " + entry.getHint());
            } else {
                lore.add(ChatColor.DARK_GRAY + "아직 발견하지 못한 항목입니다.");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(i - start, item);
        }

        // Navigation
        if (page > 0) {
            ItemStack prev = createNavItem(Material.ARROW, "이전 페이지");
            inv.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = createNavItem(Material.ARROW, "다음 페이지");
            inv.setItem(53, next);
        }
        ItemStack back = createNavItem(Material.BARRIER, "뒤로");
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    private static ItemStack createNavItem(Material mat, String name) {
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
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        String name = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());

        if (title.equals(MAIN_TITLE)) {
            String[] categories = {"ITEM", "MOB", "BIOME", "ACHIEVEMENT", "QUEST", "SKILL", "DIMENSION", "SUPPLY"};
            for (String cat : categories) {
                if (name.equals(plugin.getManager().getCategoryDisplay(cat))) {
                    openCategory(player, plugin, cat, 0);
                    return;
                }
            }
        } else if (title.startsWith(CAT_PREFIX)) {
            if (name.equals("뒤로")) { openMain(player, plugin); return; }
            if (name.equals("이전 페이지") || name.equals("다음 페이지")) {
                String catPart = title.substring(CAT_PREFIX.length());
                int parenIdx = catPart.lastIndexOf('(');
                String catDisplay = parenIdx > 0 ? catPart.substring(0, parenIdx).trim() : catPart;
                int currentPage = 0;
                try {
                    String pageStr = catPart.substring(parenIdx + 1, catPart.length() - 1).split("/")[0].trim();
                    currentPage = Integer.parseInt(pageStr) - 1;
                } catch (Exception ignored) {}
                String[] categories = {"ITEM", "MOB", "BIOME", "ACHIEVEMENT", "QUEST", "SKILL", "DIMENSION", "SUPPLY"};
                for (String cat : categories) {
                    if (plugin.getManager().getCategoryDisplay(cat).equals(catDisplay)) {
                        if (name.equals("이전 페이지")) openCategory(player, plugin, cat, currentPage - 1);
                        else openCategory(player, plugin, cat, currentPage + 1);
                        return;
                    }
                }
            }
        }
    }
}
