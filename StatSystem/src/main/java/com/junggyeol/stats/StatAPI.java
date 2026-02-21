package com.junggyeol.stats;

import org.bukkit.entity.Player;
import java.util.*;

public class StatAPI {
    public static long getStat(Player player, String statId) {
        StatPlugin plugin = StatPlugin.getInstance();
        if (plugin == null) return 0L;
        return plugin.getStatManager().getStat(player, statId);
    }

    public static void addStat(Player player, String statId, int amount) {
        StatPlugin plugin = StatPlugin.getInstance();
        if (plugin == null) return;
        plugin.getStatManager().addStat(player, statId, amount);
    }

    public static List<Map.Entry<String, Long>> getTopPlayers(String statId, int limit) {
        StatPlugin plugin = StatPlugin.getInstance();
        if (plugin == null) return new ArrayList<>();
        return plugin.getStatManager().getTopPlayers(statId, limit);
    }
}
