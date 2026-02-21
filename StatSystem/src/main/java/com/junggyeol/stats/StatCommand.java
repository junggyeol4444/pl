package com.junggyeol.stats;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class StatCommand implements CommandExecutor, TabCompleter {
    private final StatPlugin plugin;

    public StatCommand(StatPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        if (args.length > 0) {
            // Look up another player
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                // Try offline
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
                if (!offlinePlayer.hasPlayedBefore()) {
                    player.sendMessage(ChatColor.RED + "해당 플레이어를 찾을 수 없습니다: " + args[0]);
                    return true;
                }
                PlayerStatData data = plugin.getStatManager().getPlayerData(offlinePlayer.getUniqueId(), args[0]);
                showStats(player, data, args[0]);
                return true;
            }
            PlayerStatData data = plugin.getStatManager().getPlayerData(target);
            showStats(player, data, target.getName());
        } else {
            StatGUI.open(player, plugin);
        }
        return true;
    }

    private void showStats(Player viewer, PlayerStatData data, String targetName) {
        viewer.sendMessage(ChatColor.AQUA + "=== " + targetName + "의 통계 ===");
        List<String> statIds = new ArrayList<>(plugin.getConfig().getConfigurationSection("stat-categories").getKeys(false));
        for (String statId : statIds) {
            long value = data.getStat(statId);
            String displayName = plugin.getStatManager().getStatDisplayName(statId);
            viewer.sendMessage(ChatColor.GRAY + displayName + ": " + ChatColor.WHITE + formatStat(statId, value));
        }
    }

    private String formatStat(String statId, long value) {
        if (statId.equals("play_time")) {
            long hours = value / 3600;
            long minutes = (value % 3600) / 60;
            return hours + "시간 " + minutes + "분";
        }
        if (statId.equals("walk_distance")) return value + " 블록";
        return String.valueOf(value);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return Collections.emptyList();
    }
}
