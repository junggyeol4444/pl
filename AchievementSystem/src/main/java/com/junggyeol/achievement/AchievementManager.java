package com.junggyeol.achievement;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.io.*;
import java.util.*;

import org.bukkit.configuration.file.YamlConfiguration;

public class AchievementManager {
    private final AchievementPlugin plugin;
    private final Map<String, Achievement> achievements = new LinkedHashMap<>();
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();
    private final File dataFolder;

    public AchievementManager(AchievementPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        dataFolder.mkdirs();
    }

    public void loadAchievements() {
        achievements.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("achievements");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection ac = section.getConfigurationSection(id);
            if (ac == null) continue;
            String name = ac.getString("name", id);
            String description = ac.getString("description", "");
            AchievementCategory category;
            try {
                category = AchievementCategory.valueOf(ac.getString("category", "OTHER"));
            } catch (Exception e) {
                category = AchievementCategory.OTHER;
            }
            ConfigurationSection rewardSec = ac.getConfigurationSection("reward");
            int xp = 0;
            List<String> items = new ArrayList<>();
            String title = "";
            if (rewardSec != null) {
                xp = rewardSec.getInt("xp", 0);
                items = rewardSec.getStringList("items");
                title = rewardSec.getString("title", "");
            }
            AchievementReward reward = new AchievementReward(xp, items, title);
            String trigger = ac.getString("trigger", "MANUAL");
            String targetBlock = ac.getString("target_block", "");
            String targetItem = ac.getString("target_item", "");
            int requiredCount = ac.getInt("required_count", 1);
            achievements.put(id, new Achievement(id, name, description, category, reward, trigger, targetBlock, targetItem, requiredCount));
        }
        plugin.getLogger().info(achievements.size() + "개의 업적을 로드했습니다.");
    }

    public Map<String, Achievement> getAchievements() { return achievements; }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> loadPlayerData(uuid));
    }

    private PlayerData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerData data = new PlayerData(uuid);
        if (!file.exists()) return data;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        List<String> completed = yaml.getStringList("completed");
        for (String id : completed) data.addCompleted(id);
        List<String> biomes = yaml.getStringList("biomes");
        for (String b : biomes) data.addBiome(b);
        List<String> titles = yaml.getStringList("titles");
        for (String t : titles) data.addTitle(t);
        data.setEquippedTitle(yaml.getString("equippedTitle", ""));
        ConfigurationSection progressSec = yaml.getConfigurationSection("progress");
        if (progressSec != null) {
            for (String key : progressSec.getKeys(false)) {
                data.setProgress(key, progressSec.getInt(key));
            }
        }
        return data;
    }

    public void savePlayerData(PlayerData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("completed", new ArrayList<>(data.getCompletedAchievements()));
        yaml.set("biomes", new ArrayList<>(data.getVisitedBiomes()));
        yaml.set("titles", new ArrayList<>(data.getTitles()));
        yaml.set("equippedTitle", data.getEquippedTitle());
        for (String key : achievements.keySet()) {
            int prog = data.getProgress(key);
            if (prog > 0) yaml.set("progress." + key, prog);
        }
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("데이터 저장 실패: " + e.getMessage()); }
    }

    public void saveAllPlayerData() {
        for (PlayerData data : playerDataMap.values()) savePlayerData(data);
    }

    public void triggerAchievement(Player player, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) return;
        PlayerData data = getPlayerData(player);
        if (data.isCompleted(achievementId)) return;
        data.addCompleted(achievementId);
        giveReward(player, achievement.getReward());
        announceAchievement(player, achievement);
        savePlayerData(data);
        notifyIntegrations(player, achievement);
    }

    public void incrementProgress(Player player, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) return;
        PlayerData data = getPlayerData(player);
        if (data.isCompleted(achievementId)) return;
        data.incrementProgress(achievementId);
        if (data.getProgress(achievementId) >= achievement.getRequiredCount()) {
            triggerAchievement(player, achievementId);
        } else {
            savePlayerData(data);
        }
    }

    private void giveReward(Player player, AchievementReward reward) {
        player.giveExpLevels(reward.getXp() / 100);
        player.giveExp(reward.getXp());
        for (String itemStr : reward.getItems()) {
            String[] parts = itemStr.split(":");
            try {
                Material mat = Material.valueOf(parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                player.getInventory().addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }
        if (!reward.getTitle().isEmpty()) {
            PlayerData data = getPlayerData(player);
            data.addTitle(reward.getTitle());
            player.sendMessage(ChatColor.GOLD + "새로운 칭호를 획득했습니다: " + ChatColor.YELLOW + "[" + reward.getTitle() + "]");
        }
    }

    private void announceAchievement(Player player, Achievement achievement) {
        String msg = ChatColor.GOLD + "✦ " + player.getName() + " 님이 업적 [" + achievement.getName() + "]을 달성했습니다! ✦";
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(msg);
            p.sendTitle(ChatColor.GOLD + "업적 달성!", ChatColor.YELLOW + achievement.getName(), 10, 60, 20);
            p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }
    }

    private void notifyIntegrations(Player player, Achievement achievement) {
        try {
            Class<?> apiClass = Class.forName("com.junggyeol.encyclopedia.EncyclopediaAPI");
            apiClass.getMethod("discoverEntry", Player.class, String.class, String.class)
                    .invoke(null, player, "ACHIEVEMENT", achievement.getId());
        } catch (Exception ignored) {}
        try {
            Class<?> apiClass = Class.forName("com.junggyeol.stats.StatAPI");
            apiClass.getMethod("addStat", Player.class, String.class, int.class)
                    .invoke(null, player, "achievements_completed", 1);
        } catch (Exception ignored) {}
    }

    public List<Map.Entry<String, Integer>> getRanking() {
        Map<String, Integer> counts = new HashMap<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerData data = getPlayerData(p);
            counts.put(p.getName(), data.getCompletedAchievements().size());
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        return list;
    }
}
