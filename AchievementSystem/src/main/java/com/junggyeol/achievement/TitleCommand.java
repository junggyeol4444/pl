package com.junggyeol.achievement;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class TitleCommand implements CommandExecutor {
    private final AchievementPlugin plugin;

    public TitleCommand(AchievementPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        PlayerData data = plugin.getAchievementManager().getPlayerData(player);
        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "=== 내 칭호 목록 ===");
            if (data.getTitles().isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "획득한 칭호가 없습니다.");
            } else {
                for (String title : data.getTitles()) {
                    player.sendMessage(ChatColor.YELLOW + "[" + title + "]" +
                            (title.equals(data.getEquippedTitle()) ? ChatColor.GREEN + " ✔ 장착 중" : ""));
                }
            }
            player.sendMessage(ChatColor.GRAY + "사용법: /칭호 <칭호명>");
            return true;
        }
        String titleName = String.join(" ", args);
        if (data.getTitles().contains(titleName)) {
            data.setEquippedTitle(titleName);
            plugin.getAchievementManager().savePlayerData(data);
            player.sendMessage(ChatColor.GREEN + "칭호 [" + titleName + "]을 장착했습니다.");
        } else {
            player.sendMessage(ChatColor.RED + "해당 칭호를 보유하고 있지 않습니다.");
        }
        return true;
    }
}
