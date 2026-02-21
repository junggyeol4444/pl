package com.junggyeol.achievement.storage;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.PlayerAchievement;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * YAML 기반 플레이어 데이터 저장소
 */
public class DataStorage {

    private final AchievementPlugin plugin;
    private final File dataFolder;

    public DataStorage(AchievementPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    /**
     * 플레이어 데이터를 YAML 파일에서 불러옵니다.
     */
    public PlayerAchievement loadPlayer(UUID uuid) {
        File file = getPlayerFile(uuid);
        PlayerAchievement data = new PlayerAchievement(uuid);

        if (!file.exists()) {
            return data;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        // 달성 업적
        List<String> completed = config.getStringList("completed");
        completed.forEach(data::completeAchievement);

        // 진행도
        if (config.isConfigurationSection("progress")) {
            for (String key : config.getConfigurationSection("progress").getKeys(false)) {
                data.setProgress(key, config.getLong("progress." + key));
            }
        }

        // 칭호
        data.setActiveTitle(config.getString("activeTitle", null));
        List<String> titles = config.getStringList("titles");
        titles.forEach(data::addTitle);

        // 접속 데이터
        data.setLastLoginDay(config.getLong("lastLoginDay", -1));
        data.setConsecutiveDays(config.getInt("consecutiveDays", 0));
        data.setTotalDays(config.getInt("totalDays", 0));

        // 이동 거리
        data.setTotalWalkDistance(config.getLong("walkDistance", 0));

        // 블록 설치
        data.setTotalBlocksPlaced(config.getLong("blocksPlaced", 0));

        // 사망 횟수
        data.setDeathCount(config.getInt("deathCount", 0));

        // 플레이어 처치
        data.setPlayerKills(config.getInt("playerKills", 0));

        // 몹 처치
        if (config.isConfigurationSection("mobKills")) {
            for (String mob : config.getConfigurationSection("mobKills").getKeys(false)) {
                data.setMobKills(mob, config.getInt("mobKills." + mob));
            }
        }

        // 블록 채굴
        if (config.isConfigurationSection("blocksMined")) {
            for (String block : config.getConfigurationSection("blocksMined").getKeys(false)) {
                data.setBlocksMined(block, config.getInt("blocksMined." + block));
            }
        }

        // 방문 월드
        List<String> worlds = config.getStringList("visitedWorlds");
        worlds.forEach(data::addVisitedWorld);

        // 첫 접속 여부
        data.setFirstJoin(config.getBoolean("firstJoin", false));

        return data;
    }

    /**
     * 플레이어 데이터를 YAML 파일에 저장합니다.
     */
    public void savePlayer(PlayerAchievement data) {
        File file = getPlayerFile(data.getPlayerUUID());
        FileConfiguration config = new YamlConfiguration();

        // 달성 업적
        config.set("completed", new ArrayList<>(data.getCompletedAchievements()));

        // 진행도
        for (Map.Entry<String, Long> entry : data.getAllProgress().entrySet()) {
            config.set("progress." + entry.getKey(), entry.getValue());
        }

        // 칭호
        config.set("activeTitle", data.getActiveTitle());
        config.set("titles", new ArrayList<>(data.getOwnedTitles()));

        // 접속 데이터
        config.set("lastLoginDay", data.getLastLoginDay());
        config.set("consecutiveDays", data.getConsecutiveDays());
        config.set("totalDays", data.getTotalDays());

        // 이동 거리
        config.set("walkDistance", data.getTotalWalkDistance());

        // 블록 설치
        config.set("blocksPlaced", data.getTotalBlocksPlaced());

        // 사망 횟수
        config.set("deathCount", data.getDeathCount());

        // 플레이어 처치
        config.set("playerKills", data.getPlayerKills());

        // 몹 처치
        for (Map.Entry<String, Integer> entry : data.getMobKills().entrySet()) {
            config.set("mobKills." + entry.getKey(), entry.getValue());
        }

        // 블록 채굴
        for (Map.Entry<String, Integer> entry : data.getBlocksMined().entrySet()) {
            config.set("blocksMined." + entry.getKey(), entry.getValue());
        }

        // 방문 월드
        config.set("visitedWorlds", new ArrayList<>(data.getVisitedWorlds()));

        // 첫 접속 여부
        config.set("firstJoin", data.isFirstJoin());

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "플레이어 데이터 저장 실패: " + data.getPlayerUUID(), e);
        }
    }

    /**
     * 모든 플레이어 UUID 목록을 반환합니다.
     */
    public List<UUID> getAllPlayerUUIDs() {
        List<UUID> uuids = new ArrayList<>();
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    String name = file.getName().replace(".yml", "");
                    uuids.add(UUID.fromString(name));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        return uuids;
    }
}
