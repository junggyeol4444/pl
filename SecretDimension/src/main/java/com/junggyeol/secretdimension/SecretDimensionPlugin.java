package com.junggyeol.secretdimension;

import org.bukkit.plugin.java.JavaPlugin;

public class SecretDimensionPlugin extends JavaPlugin {
    private static SecretDimensionPlugin instance;
    private DimensionManager manager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        manager = new DimensionManager(this);
        manager.loadDimensions();
        getCommand("차원").setExecutor(new DimensionCommand(this));
        getServer().getPluginManager().registerEvents(new DimensionListener(this), this);
        getServer().getPluginManager().registerEvents(new DimensionGUI(this), this);
        getLogger().info("SecretDimension 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        for (java.util.UUID uuid : new java.util.ArrayList<>(manager.getActiveSessions().keySet())) {
            org.bukkit.entity.Player player = getServer().getPlayer(uuid);
            if (player != null) manager.exitDimension(player, false);
        }
        manager.saveAll();
        getLogger().info("SecretDimension 플러그인이 비활성화되었습니다.");
    }

    public static SecretDimensionPlugin getInstance() { return instance; }
    public DimensionManager getManager() { return manager; }
}
