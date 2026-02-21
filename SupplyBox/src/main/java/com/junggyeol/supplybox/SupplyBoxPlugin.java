package com.junggyeol.supplybox;

import org.bukkit.plugin.java.JavaPlugin;

public class SupplyBoxPlugin extends JavaPlugin {
    private static SupplyBoxPlugin instance;
    private SupplyBoxManager manager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        manager = new SupplyBoxManager(this);
        manager.scheduleNextDrop();
        new SupplyBoxTask(this).runTaskTimer(this, 20L, 20L);
        getCommand("보급").setExecutor(new SupplyCommand(this));
        getServer().getPluginManager().registerEvents(new SupplyBoxListener(this), this);
        getLogger().info("SupplyBox 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("SupplyBox 플러그인이 비활성화되었습니다.");
    }

    public static SupplyBoxPlugin getInstance() { return instance; }
    public SupplyBoxManager getManager() { return manager; }
}
