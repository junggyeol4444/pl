package com.junggyeol.stats;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public class RankingCommand implements CommandExecutor, TabCompleter {
    private final StatPlugin plugin;

    public RankingCommand(StatPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("플레이어만 사용 가능합니다.");
            return true;
        }
        if (args.length > 0) {
            RankingGUI.open(player, plugin, args[0]);
        } else {
            RankingGUI.openMain(player, plugin);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return new ArrayList<>(plugin.getConfig().getConfigurationSection("stat-categories").getKeys(false));
        }
        return Collections.emptyList();
    }
}
