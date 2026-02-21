package com.junggyeol.encyclopedia;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class EncyclopediaCommand implements CommandExecutor {
    private final EncyclopediaPlugin plugin;
    public EncyclopediaCommand(EncyclopediaPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        EncyclopediaGUI.openMain(player, plugin);
        return true;
    }
}
