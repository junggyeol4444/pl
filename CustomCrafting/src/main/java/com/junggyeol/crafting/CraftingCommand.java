package com.junggyeol.crafting;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class CraftingCommand implements CommandExecutor {
    private final CustomCraftingPlugin plugin;
    public CraftingCommand(CustomCraftingPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        CraftingGUI.open(player, plugin);
        return true;
    }
}
