package com.junggyeol.encyclopedia;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class EncyclopediaListener implements Listener {
    private final EncyclopediaPlugin plugin;
    public EncyclopediaListener(EncyclopediaPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        PlayerEncyclopediaData data = plugin.getManager().getPlayerData(event.getPlayer());
        plugin.getManager().savePlayerData(data);
    }
}
