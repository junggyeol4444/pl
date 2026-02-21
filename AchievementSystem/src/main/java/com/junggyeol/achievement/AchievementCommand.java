package com.junggyeol.achievement;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class AchievementCommand implements CommandExecutor, TabCompleter {
    private final AchievementPlugin plugin;

    public AchievementCommand(AchievementPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        if (args.length > 0 && args[0].equals("랭킹")) {
            List<Map.Entry<String, Integer>> ranking = plugin.getAchievementManager().getRanking();
            player.sendMessage(ChatColor.GOLD + "=== 업적 랭킹 ===");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : ranking) {
                player.sendMessage(ChatColor.YELLOW + rank + ". " + entry.getKey() + " - " + entry.getValue() + "개");
                rank++;
                if (rank > 10) break;
            }
            return true;
        }
        AchievementGUI.open(player, plugin, 0);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return Collections.singletonList("랭킹");
        return Collections.emptyList();
    }
}
