package com.junggyeol.secretdimension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.*;

public class DimensionListener implements Listener {
    private final SecretDimensionPlugin plugin;

    public DimensionListener(SecretDimensionPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        if (block == null) return;

        // Crystal Cave: 4 diamond blocks + right-click with ender eye
        if (block.getType() == Material.DIAMOND_BLOCK
                && player.getInventory().getItemInMainHand().getType() == Material.ENDER_EYE) {
            if (plugin.getManager().checkDiamondPortal(block)) {
                event.setCancelled(true);
                player.getInventory().getItemInMainHand().setAmount(
                        player.getInventory().getItemInMainHand().getAmount() - 1);
                plugin.getManager().enterDimension(player, "crystal_cave");
            }
        }

        // Dark Labyrinth: soul sand + wither skeleton skulls in inventory, at night
        if (block.getType() == Material.SOUL_SAND) {
            long time = block.getWorld().getTime();
            boolean isNight = time > 12000 && time < 24000;
            if (isNight && hasWitherSkulls(player, 3)) {
                event.setCancelled(true);
                removeWitherSkulls(player, 3);
                plugin.getManager().enterDimension(player, "dark_labyrinth");
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // Sky Island: Y >= 200 + gliding (placeholder for firework-based trigger)
        Player player = event.getPlayer();
        if (player.getLocation().getY() >= 200
                && player.isGliding()
                && !plugin.getManager().getActiveSessions().containsKey(player.getUniqueId())) {
            // Simplified trigger placeholder — full firework detection can be added here
        }
    }

    @EventHandler
    public void onElytraToggle(EntityToggleGlideEvent event) {
        // Sky Island entry: handled via PlayerInteractEvent for firework at high altitude
    }

    private boolean hasWitherSkulls(Player player, int count) {
        int found = 0;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WITHER_SKELETON_SKULL) {
                found += item.getAmount();
            }
        }
        return found >= count;
    }

    private void removeWitherSkulls(Player player, int count) {
        int remaining = count;
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.WITHER_SKELETON_SKULL && remaining > 0) {
                int take = Math.min(item.getAmount(), remaining);
                item.setAmount(item.getAmount() - take);
                remaining -= take;
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.getManager().getActiveSessions().containsKey(player.getUniqueId())) {
            plugin.getManager().exitDimension(player, false);
        }
        PlayerDimensionData data = plugin.getManager().getPlayerData(player);
        plugin.getManager().savePlayerData(data);
    }
}
