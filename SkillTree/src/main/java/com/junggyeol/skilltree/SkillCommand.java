package com.junggyeol.skilltree;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class SkillCommand implements CommandExecutor {
    private final SkillTreePlugin plugin;
    public SkillCommand(SkillTreePlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        SkillGUI.openMain(player, plugin);
        return true;
    }
}
