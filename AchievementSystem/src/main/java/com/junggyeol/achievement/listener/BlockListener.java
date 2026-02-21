package com.junggyeol.achievement.listener;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement.ConditionType;
import com.junggyeol.achievement.model.PlayerAchievement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * 블록 관련 이벤트 리스너 (채굴, 건축)
 */
public class BlockListener implements Listener {

    private final AchievementPlugin plugin;

    public BlockListener(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        String blockType = event.getBlock().getType().name();
        data.incrementBlocksMined(blockType);

        plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.MINE_BLOCK);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        data.addBlocksPlaced(1);

        plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.PLACE_BLOCK);
    }
}
