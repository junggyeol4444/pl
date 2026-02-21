package com.junggyeol.stats;

import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;

public class StatListener implements Listener {
    private final StatPlugin plugin;

    public StatListener(StatPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerStatData data = plugin.getStatManager().getPlayerData(player);
        data.setPlayTimeStart(System.currentTimeMillis());
        data.addStat("login_count", 1);
        // Login streak
        long today = System.currentTimeMillis() / 86400000L;
        long lastLogin = data.getLastLoginDate() / 86400000L;
        if (today == lastLogin + 1) {
            data.setLoginStreak(data.getLoginStreak() + 1);
        } else if (today > lastLogin + 1) {
            data.setLoginStreak(1);
        }
        data.setLastLoginDate(System.currentTimeMillis());
        plugin.getStatManager().savePlayerData(data);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerStatData data = plugin.getStatManager().getPlayerData(player);
        if (data.getPlayTimeStart() > 0) {
            long sessionTime = (System.currentTimeMillis() - data.getPlayTimeStart()) / 1000;
            data.addStat("play_time", sessionTime);
            data.setPlayTimeStart(0);
        }
        plugin.getStatManager().savePlayerData(data);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        int dist = (int) event.getFrom().distance(event.getTo());
        if (dist > 0) {
            plugin.getStatManager().addStat(event.getPlayer(), "walk_distance", dist);
        }
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        plugin.getStatManager().addStat(player, "mobs_killed", 1);
        // PVP
        if (event.getEntity() instanceof Player) {
            plugin.getStatManager().addStat(player, "pvp_kills", 1);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        plugin.getStatManager().addStat(player, "deaths", 1);
        if (event.getEntity().getKiller() instanceof Player) {
            plugin.getStatManager().addStat(player, "pvp_deaths", 1);
        }
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        plugin.getStatManager().addStat(event.getPlayer(), "blocks_mined", 1);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        plugin.getStatManager().addStat(event.getPlayer(), "blocks_placed", 1);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        plugin.getStatManager().addStat(player, "items_crafted", 1);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.getStatManager().addStat(event.getPlayer(), "fish_caught", 1);
        }
    }
}
