package com.junggyeol.achievement;

import org.bukkit.entity.Player;

public class AchievementAPI {
    public static void trigger(Player player, String achievementId) {
        AchievementPlugin plugin = AchievementPlugin.getInstance();
        if (plugin == null) return;
        plugin.getAchievementManager().triggerAchievement(player, achievementId);
    }

    public static boolean isCompleted(Player player, String achievementId) {
        AchievementPlugin plugin = AchievementPlugin.getInstance();
        if (plugin == null) return false;
        PlayerData data = plugin.getAchievementManager().getPlayerData(player);
        return data != null && data.isCompleted(achievementId);
    }
}
