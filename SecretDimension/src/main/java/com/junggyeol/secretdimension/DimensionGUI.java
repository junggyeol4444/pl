package com.junggyeol.secretdimension;

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

public class DimensionGUI implements Listener {
    private static final String TITLE = "비밀 차원 목록";
    private final SecretDimensionPlugin plugin;

    public DimensionGUI(SecretDimensionPlugin plugin) { this.plugin = plugin; }

    public static void open(Player player, SecretDimensionPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        PlayerDimensionData data = plugin.getManager().getPlayerData(player);
        List<SecretDimension> dims = new ArrayList<>(plugin.getManager().getDimensions().values());
        Material[] icons = {Material.AMETHYST_SHARD, Material.ELYTRA, Material.WITHER_SKELETON_SKULL};
        int[] slots = {11, 13, 15};

        for (int i = 0; i < dims.size() && i < 3; i++) {
            SecretDimension dim = dims.get(i);
            boolean discovered = data.hasDiscovered(dim.getId());
            Material icon = i < icons.length ? icons[i] : Material.ENDER_EYE;
            ItemStack item = new ItemStack(discovered ? icon : Material.BARRIER);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName((discovered ? ChatColor.LIGHT_PURPLE : ChatColor.DARK_GRAY) +
                    (discovered ? dim.getName() : "???"));
            List<String> lore = new ArrayList<>();
            if (discovered) {
                lore.add(ChatColor.GRAY + dim.getDescription());
                lore.add("");
                lore.add(ChatColor.YELLOW + "제한 시간: " + dim.getTimeLimitMinutes() + "분");
                lore.add(ChatColor.GOLD + "보상 경험치: " + dim.getRewardXp());
                lore.add("");
                lore.add(ChatColor.GREEN + "✔ 발견됨");
            } else {
                lore.add(ChatColor.DARK_GRAY + "아직 발견하지 못한 차원입니다.");
                lore.add(ChatColor.GRAY + "힌트를 찾아보세요...");
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
            inv.setItem(slots[i], item);
        }

        // Info item showing discovery count
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta im = info.getItemMeta();
        long discovered = dims.stream().filter(d -> data.hasDiscovered(d.getId())).count();
        im.setDisplayName(ChatColor.GOLD + "차원 수집: " + discovered + "/" + dims.size());
        info.setItemMeta(im);
        inv.setItem(4, info);

        player.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);
    }
}
