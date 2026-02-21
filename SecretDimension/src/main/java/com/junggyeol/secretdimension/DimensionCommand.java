package com.junggyeol.secretdimension;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class DimensionCommand implements CommandExecutor {
    private final SecretDimensionPlugin plugin;

    public DimensionCommand(SecretDimensionPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        DimensionGUI.open(player, plugin);
        return true;
    }
}
