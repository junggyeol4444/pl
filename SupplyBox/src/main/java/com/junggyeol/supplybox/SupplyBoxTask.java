package com.junggyeol.supplybox;

import org.bukkit.scheduler.BukkitRunnable;

public class SupplyBoxTask extends BukkitRunnable {
    private final SupplyBoxPlugin plugin;

    public SupplyBoxTask(SupplyBoxPlugin plugin) { this.plugin = plugin; }

    @Override
    public void run() {
        if (System.currentTimeMillis() >= plugin.getManager().getNextDropTime()) {
            plugin.getManager().spawnSupplyBox();
        }
    }
}
