package com.junggyeol.quest;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class QuestCommand implements CommandExecutor {
    private final QuestPlugin plugin;
    public QuestCommand(QuestPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        QuestGUI.open(player, plugin, "ALL", 0);
        return true;
    }
}
