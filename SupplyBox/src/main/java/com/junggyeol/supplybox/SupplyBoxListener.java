package com.junggyeol.supplybox;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class SupplyBoxListener implements Listener {
    private final SupplyBoxPlugin plugin;

    public SupplyBoxListener(SupplyBoxPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onOpenInventory(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (event.getInventory().getType() != InventoryType.CHEST) return;
        if (event.getInventory().getLocation() == null) return;
        Location loc = event.getInventory().getLocation().toBlockLocation();
        if (plugin.getManager().getActiveBoxes().containsKey(loc)) {
            plugin.getManager().handleOpen(player, loc);
        }
    }
}
