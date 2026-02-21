package com.junggyeol.achievement.listener;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement.ConditionType;
import com.junggyeol.achievement.model.PlayerAchievement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * 전투 관련 이벤트 리스너 (몹 처치, 플레이어 처치)
 */
public class CombatListener implements Listener {

    private final AchievementPlugin plugin;

    public CombatListener(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(killer.getUniqueId());

        if (event.getEntity() instanceof Player) {
            // 플레이어 처치
            data.incrementPlayerKills();
            plugin.getAchievementManager().checkAchievementsByType(killer, ConditionType.KILL_PLAYER);
        } else {
            // 몹 처치
            String mobType = event.getEntity().getType().name();
            data.incrementMobKills(mobType);
            plugin.getAchievementManager().checkAchievementsByType(killer, ConditionType.KILL_MOB);
        }
    }
}
