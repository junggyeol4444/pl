package com.junggyeol.encyclopedia;

import org.bukkit.plugin.java.JavaPlugin;

public class EncyclopediaPlugin extends JavaPlugin {
    private static EncyclopediaPlugin instance;
    private EncyclopediaManager manager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        manager = new EncyclopediaManager(this);
        getCommand("도감").setExecutor(new EncyclopediaCommand(this));
        getServer().getPluginManager().registerEvents(new EncyclopediaListener(this), this);
        getServer().getPluginManager().registerEvents(new EncyclopediaGUI(this), this);
        getLogger().info("Encyclopedia 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.saveAll();
        getLogger().info("Encyclopedia 플러그인이 비활성화되었습니다.");
    }

    public static EncyclopediaPlugin getInstance() { return instance; }
    public EncyclopediaManager getManager() { return manager; }
}
