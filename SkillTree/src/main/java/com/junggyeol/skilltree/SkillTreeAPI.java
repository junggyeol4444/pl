package com.junggyeol.skilltree;

import org.bukkit.entity.Player;

public class SkillTreeAPI {
    public static int getSkillLevel(Player player, String skillId) {
        SkillTreePlugin plugin = SkillTreePlugin.getInstance();
        if (plugin == null) return 0;
        return plugin.getManager().getSkillLevel(player, skillId);
    }

    public static void addSkillPoints(Player player, int amount) {
        SkillTreePlugin plugin = SkillTreePlugin.getInstance();
        if (plugin == null) return;
        plugin.getManager().addSkillPoints(player, amount);
    }
}
