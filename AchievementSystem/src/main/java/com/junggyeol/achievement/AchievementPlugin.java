package com.junggyeol.achievement;

import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class AchievementPlugin extends JavaPlugin {
    private static AchievementPlugin instance;
    private AchievementManager achievementManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        achievementManager = new AchievementManager(this);
        achievementManager.loadAchievements();
        getCommand("업적").setExecutor(new AchievementCommand(this));
        getCommand("칭호").setExecutor(new TitleCommand(this));
        getServer().getPluginManager().registerEvents(new AchievementListener(this), this);
        getServer().getPluginManager().registerEvents(new AchievementGUI(this), this);
        getLogger().info("AchievementSystem 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (achievementManager != null) {
            achievementManager.saveAllPlayerData();
        }
        getLogger().info("AchievementSystem 플러그인이 비활성화되었습니다.");
    }

    public static AchievementPlugin getInstance() { return instance; }
    public AchievementManager getAchievementManager() { return achievementManager; }
}
