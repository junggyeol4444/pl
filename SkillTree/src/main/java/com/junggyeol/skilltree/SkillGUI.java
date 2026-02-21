package com.junggyeol.skilltree;

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

public class SkillGUI implements Listener {
    private static final String MAIN_TITLE = "스킬 트리";
    private static final String TREE_PREFIX = "스킬: ";
    private final SkillTreePlugin plugin;

    public SkillGUI(SkillTreePlugin plugin) { this.plugin = plugin; }

    public static void openMain(Player player, SkillTreePlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_TITLE);
        Material[] icons = {Material.DIAMOND_PICKAXE, Material.DIAMOND_SWORD, Material.EMERALD};
        int[] slots = {11, 13, 15};
        List<String> treeIds = new ArrayList<>(plugin.getManager().getTrees().keySet());

        PlayerSkillData data = plugin.getManager().getPlayerData(player);
        for (int i = 0; i < treeIds.size() && i < 3; i++) {
            SkillTree tree = plugin.getManager().getTrees().get(treeIds.get(i));
            ItemStack item = new ItemStack(icons[i]);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + tree.getName());
            List<String> lore = new ArrayList<>();
            for (Skill skill : tree.getSkills().values()) {
                int level = data.getSkillLevel(skill.getId());
                lore.add(ChatColor.GRAY + skill.getName() + " Lv." + level + "/" + skill.getMaxLevel());
            }
            lore.add("");
            lore.add(ChatColor.YELLOW + "클릭하여 열기");
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        // Points display
        ItemStack points = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta pm = points.getItemMeta();
        pm.setDisplayName(ChatColor.GREEN + "스킬 포인트: " + data.getSkillPoints());
        points.setItemMeta(pm);
        inv.setItem(4, points);

        player.openInventory(inv);
    }

    public static void openTree(Player player, SkillTreePlugin plugin, String treeId) {
        SkillTree tree = plugin.getManager().getTrees().get(treeId);
        if (tree == null) return;
        Inventory inv = Bukkit.createInventory(null, 54, TREE_PREFIX + treeId);
        PlayerSkillData data = plugin.getManager().getPlayerData(player);

        int slot = 0;
        for (Skill skill : tree.getSkills().values()) {
            int level = data.getSkillLevel(skill.getId());
            boolean maxed = level >= skill.getMaxLevel();
            Material mat = maxed ? Material.LIME_WOOL : (level > 0 ? Material.YELLOW_WOOL : Material.GRAY_WOOL);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.AQUA + skill.getName() + " Lv." + level + "/" + skill.getMaxLevel());
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + skill.getDescription());
            lore.add(ChatColor.YELLOW + "비용: " + skill.getCost() + " 포인트");
            if (skill.getRequiresSkillId() != null) {
                lore.add(ChatColor.RED + "선행: " + skill.getRequiresSkillId() + " Lv." + skill.getRequiresLevel());
            }
            if (!maxed) lore.add(ChatColor.GREEN + "[클릭] 업그레이드");
            else lore.add(ChatColor.GREEN + "✔ 최대 레벨");
            lore.add(ChatColor.DARK_GRAY + "tree:" + treeId);
            lore.add(ChatColor.DARK_GRAY + "skill:" + skill.getId());
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slot++, item);
        }

        // Points display
        ItemStack points = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta pm = points.getItemMeta();
        pm.setDisplayName(ChatColor.GREEN + "스킬 포인트: " + data.getSkillPoints());
        points.setItemMeta(pm);
        inv.setItem(49, points);

        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta bm = back.getItemMeta();
        bm.setDisplayName(ChatColor.WHITE + "뒤로");
        back.setItemMeta(bm);
        inv.setItem(45, back);

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
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        List<String> lore = meta.getLore();

        if (title.equals(MAIN_TITLE)) {
            // Find tree by name
            for (SkillTree tree : plugin.getManager().getTrees().values()) {
                if (displayName.equals(tree.getName())) {
                    openTree(player, plugin, tree.getId());
                    return;
                }
            }
        } else if (title.startsWith(TREE_PREFIX)) {
            String treeId = title.substring(TREE_PREFIX.length());
            if (displayName.equals("뒤로")) { openMain(player, plugin); return; }
            // Check if skill item
            if (lore != null) {
                String skillId = null;
                for (String line : lore) {
                    String stripped = ChatColor.stripColor(line);
                    if (stripped.startsWith("skill:")) skillId = stripped.substring(6);
                }
                if (skillId != null) {
                    plugin.getManager().upgradeSkill(player, treeId, skillId);
                    openTree(player, plugin, treeId); // Refresh
                }
            }
        }
    }
}
