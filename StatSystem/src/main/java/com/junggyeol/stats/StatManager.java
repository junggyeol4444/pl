package com.junggyeol.stats;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class StatManager {
    private final StatPlugin plugin;
    private final Map<UUID, PlayerStatData> playerData = new HashMap<>();
    private final File dataFolder;

    public StatManager(StatPlugin plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("데이터 폴더 생성 실패");
        }
    }

    public PlayerStatData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(),
                uuid -> loadPlayerData(uuid, player.getName()));
    }

    public PlayerStatData getPlayerData(UUID uuid, String name) {
        return playerData.computeIfAbsent(uuid, u -> loadPlayerData(u, name));
    }

    private PlayerStatData loadPlayerData(UUID uuid, String name) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerStatData data = new PlayerStatData(uuid, name);
        if (!file.exists()) return data;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        data.setLoginStreak(yaml.getInt("loginStreak", 0));
        data.setLastLoginDate(yaml.getLong("lastLoginDate", 0L));
        ConfigurationSection statSec = yaml.getConfigurationSection("stats");
        if (statSec != null) {
            for (String key : statSec.getKeys(false)) {
                data.setStat(key, yaml.getLong("stats." + key, 0L));
            }
        }
        return data;
    }

    public void savePlayerData(PlayerStatData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("playerName", data.getPlayerName());
        yaml.set("loginStreak", data.getLoginStreak());
        yaml.set("lastLoginDate", data.getLastLoginDate());
        for (Map.Entry<String, Long> entry : data.getAllStats().entrySet()) {
            yaml.set("stats." + entry.getKey(), entry.getValue());
        }
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("저장 실패: " + e.getMessage()); }
    }

    public void saveAll() {
        // First, update play times
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerStatData data = getPlayerData(p);
            if (data.getPlayTimeStart() > 0) {
                long sessionTime = (System.currentTimeMillis() - data.getPlayTimeStart()) / 1000;
                data.addStat("play_time", sessionTime);
                data.setPlayTimeStart(System.currentTimeMillis());
            }
        }
        for (PlayerStatData data : playerData.values()) savePlayerData(data);
    }

    public void addStat(Player player, String statId, long amount) {
        PlayerStatData data = getPlayerData(player);
        data.addStat(statId, amount);
        checkAchievements(player, statId, data.getStat(statId));
    }

    public long getStat(Player player, String statId) {
        return getPlayerData(player).getStat(statId);
    }

    private void checkAchievements(Player player, String statId, long value) {
        // Trigger achievements based on stat milestones
        try {
            Class<?> ac = Class.forName("com.junggyeol.achievement.AchievementAPI");
            if (statId.equals("mobs_killed") && value >= 1000) {
                ac.getMethod("trigger", Player.class, String.class).invoke(null, player, "hunter");
            }
            if (statId.equals("blocks_mined") && value >= 1000) {
                ac.getMethod("trigger", Player.class, String.class).invoke(null, player, "miner");
            }
        } catch (Exception ignored) {}
    }

    public List<Map.Entry<String, Long>> getTopPlayers(String statId, int limit) {
        Map<String, Long> allValues = new HashMap<>();
        for (PlayerStatData data : playerData.values()) {
            allValues.put(data.getPlayerName(), data.getStat(statId));
        }
        return allValues.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public String getStatDisplayName(String statId) {
        return plugin.getConfig().getString("stat-categories." + statId, statId);
    }

    public Map<String, PlayerStatData> getAllPlayerData() { return new HashMap<>(playerData); }
}
