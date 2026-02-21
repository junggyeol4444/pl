package com.junggyeol.achievement;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;

public class AchievementListener implements Listener {
    private final AchievementPlugin plugin;

    public AchievementListener(AchievementPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getAchievementManager().getPlayerData(player);
        if (!data.isCompleted("first_step")) {
            plugin.getAchievementManager().triggerAchievement(player, "first_step");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockName = event.getBlock().getType().name();
        for (Achievement ac : plugin.getAchievementManager().getAchievements().values()) {
            if (!"MINE_BLOCK".equals(ac.getTrigger())) continue;
            String target = ac.getTargetBlock();
            if (target.isEmpty()) continue;
            if (blockName.equals(target) || blockName.equals("DEEPSLATE_" + target)) {
                plugin.getAchievementManager().incrementProgress(player, ac.getId());
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        for (Achievement ac : plugin.getAchievementManager().getAchievements().values()) {
            if ("KILL_MOB".equals(ac.getTrigger())) {
                plugin.getAchievementManager().incrementProgress(player, ac.getId());
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        Player player = event.getPlayer();
        String biome = player.getLocation().getBlock().getBiome().name();
        PlayerData data = plugin.getAchievementManager().getPlayerData(player);
        if (data.getVisitedBiomes().contains(biome)) return;
        data.addBiome(biome);
        boolean progressChanged = false;
        for (Achievement ac : plugin.getAchievementManager().getAchievements().values()) {
            if ("VISIT_BIOME".equals(ac.getTrigger())) {
                data.setProgress(ac.getId(), data.getVisitedBiomes().size());
                progressChanged = true;
                if (data.getProgress(ac.getId()) >= ac.getRequiredCount()) {
                    plugin.getAchievementManager().triggerAchievement(player, ac.getId());
                    progressChanged = false; // triggerAchievement saves data
                }
            }
        }
        if (progressChanged) {
            plugin.getAchievementManager().savePlayerData(data);
        }
    }
}
