package com.junggyeol.supplybox;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SupplyCommand implements CommandExecutor {
    private final SupplyBoxPlugin plugin;

    public SupplyCommand(SupplyBoxPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        long nextDrop = plugin.getManager().getNextDropTime();
        long remaining = nextDrop - System.currentTimeMillis();
        if (remaining <= 0) {
            sender.sendMessage(ChatColor.YELLOW + "[보급상자] 보급 상자가 곧 떨어집니다!");
        } else {
            long minutes = remaining / 60000;
            long seconds = (remaining % 60000) / 1000;
            sender.sendMessage(ChatColor.YELLOW + "[보급상자] 다음 보급까지: " + minutes + "분 " + seconds + "초");
        }
        // Show active boxes if admin
        if (sender instanceof Player player && player.hasPermission("supplybox.admin")) {
            if (!plugin.getManager().getActiveBoxes().isEmpty()) {
                sender.sendMessage(ChatColor.GOLD + "현재 활성 보급 상자: " + plugin.getManager().getActiveBoxes().size() + "개");
            }
        }
        return true;
    }
}
