package com.junggyeol.stats;

import org.bukkit.plugin.java.JavaPlugin;

public class StatPlugin extends JavaPlugin {
    private static StatPlugin instance;
    private StatManager statManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        statManager = new StatManager(this);
        getCommand("스탯").setExecutor(new StatCommand(this));
        getCommand("랭킹").setExecutor(new RankingCommand(this));
        getServer().getPluginManager().registerEvents(new StatListener(this), this);
        getServer().getPluginManager().registerEvents(new StatGUI(this), this);
        getServer().getPluginManager().registerEvents(new RankingGUI(this), this);
        // Auto-save task
        long interval = getConfig().getLong("save-interval-minutes", 5) * 60 * 20L;
        getServer().getScheduler().runTaskTimerAsynchronously(this,
                () -> getServer().getScheduler().runTask(this, () -> statManager.saveAll()),
                interval, interval);
        getLogger().info("StatSystem 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (statManager != null) statManager.saveAll();
        getLogger().info("StatSystem 플러그인이 비활성화되었습니다.");
    }

    public static StatPlugin getInstance() { return instance; }
    public StatManager getStatManager() { return statManager; }
}
