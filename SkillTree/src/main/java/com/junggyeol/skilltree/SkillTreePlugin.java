package com.junggyeol.skilltree;

import org.bukkit.plugin.java.JavaPlugin;

public class SkillTreePlugin extends JavaPlugin {
    private static SkillTreePlugin instance;
    private SkillTreeManager manager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        manager = new SkillTreeManager(this);
        manager.loadTrees();
        getCommand("스킬").setExecutor(new SkillCommand(this));
        getServer().getPluginManager().registerEvents(new SkillGUI(this), this);
        getServer().getPluginManager().registerEvents(new SkillEffectListener(this), this);
        // Apply effects to all already-online players (reload case)
        for (org.bukkit.entity.Player p : getServer().getOnlinePlayers()) {
            manager.applyPassiveEffects(p, manager.getPlayerData(p));
        }
        getLogger().info("SkillTree 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.saveAll();
        getLogger().info("SkillTree 플러그인이 비활성화되었습니다.");
    }

    public static SkillTreePlugin getInstance() { return instance; }
    public SkillTreeManager getManager() { return manager; }
}
