package com.junggyeol.quest;

import org.bukkit.plugin.java.JavaPlugin;

public class QuestPlugin extends JavaPlugin {
    private static QuestPlugin instance;
    private QuestManager questManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        questManager = new QuestManager(this);
        questManager.loadQuests();
        getCommand("퀘스트").setExecutor(new QuestCommand(this));
        getServer().getPluginManager().registerEvents(new QuestListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestGUI(this), this);
        getLogger().info("QuestSystem 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (questManager != null) questManager.saveAll();
        getLogger().info("QuestSystem 플러그인이 비활성화되었습니다.");
    }

    public static QuestPlugin getInstance() { return instance; }
    public QuestManager getQuestManager() { return questManager; }
}
