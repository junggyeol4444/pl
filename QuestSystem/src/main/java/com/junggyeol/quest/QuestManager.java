package com.junggyeol.quest;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class QuestManager {
    private final QuestPlugin plugin;
    private final Map<String, Quest> quests = new LinkedHashMap<>();
    private final Map<UUID, PlayerQuestData> playerDataMap = new HashMap<>();
    private final File dataFolder;

    public QuestManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("데이터 폴더 생성 실패");
        }
    }

    public void loadQuests() {
        quests.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("quests");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection qc = section.getConfigurationSection(id);
            if (qc == null) continue;
            String name = qc.getString("name", id);
            String description = qc.getString("description", "");
            QuestPeriod period;
            try { period = QuestPeriod.valueOf(qc.getString("period", "SPECIAL")); }
            catch (Exception e) { period = QuestPeriod.SPECIAL; }
            QuestType type;
            try { type = QuestType.valueOf(qc.getString("type", "KILL_MOB")); }
            catch (Exception e) { type = QuestType.KILL_MOB; }
            String target = qc.getString("target", "");
            int required = qc.getInt("required_count", 1);
            ConfigurationSection rSec = qc.getConfigurationSection("reward");
            int xp = 0; List<String> items = new ArrayList<>(); int sp = 0;
            if (rSec != null) {
                xp = rSec.getInt("xp", 0);
                items = rSec.getStringList("items");
                sp = rSec.getInt("skill_points", 0);
            }
            quests.put(id, new Quest(id, name, description, period, type, target, required, new QuestReward(xp, items, sp)));
        }
        plugin.getLogger().info(quests.size() + "개의 퀘스트를 로드했습니다.");
    }

    public Map<String, Quest> getQuests() { return quests; }

    public PlayerQuestData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), this::loadPlayerData);
    }

    private PlayerQuestData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerQuestData data = new PlayerQuestData(uuid);
        if (!file.exists()) return data;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String id : yaml.getStringList("completed")) data.addCompleted(id);
        for (String id : yaml.getStringList("completedToday")) data.getCompletedToday().add(id);
        for (String id : yaml.getStringList("completedThisWeek")) data.getCompletedThisWeek().add(id);
        data.setLastDailyReset(yaml.getLong("lastDailyReset", 0L));
        data.setLastWeeklyReset(yaml.getLong("lastWeeklyReset", 0L));
        ConfigurationSection prog = yaml.getConfigurationSection("progress");
        if (prog != null) {
            for (String key : prog.getKeys(false)) data.setProgress(key, prog.getInt(key));
        }
        return data;
    }

    public void savePlayerData(PlayerQuestData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("completed", new ArrayList<>(data.getCompletedQuests()));
        yaml.set("completedToday", new ArrayList<>(data.getCompletedToday()));
        yaml.set("completedThisWeek", new ArrayList<>(data.getCompletedThisWeek()));
        yaml.set("lastDailyReset", data.getLastDailyReset());
        yaml.set("lastWeeklyReset", data.getLastWeeklyReset());
        for (Map.Entry<String, Quest> e : quests.entrySet()) {
            yaml.set("progress." + e.getKey(), data.getProgress(e.getKey()));
        }
        try { yaml.save(file); } catch (IOException ex) { plugin.getLogger().warning("저장 실패: " + ex.getMessage()); }
    }

    public void saveAll() {
        for (PlayerQuestData data : playerDataMap.values()) savePlayerData(data);
    }

    public void incrementProgress(Player player, QuestType type, String target, int amount) {
        PlayerQuestData data = getPlayerData(player);
        checkReset(data);
        for (Quest quest : quests.values()) {
            if (quest.getType() != type) continue;
            if (isQuestCompleteForPeriod(data, quest)) continue;
            if (!quest.getTarget().isEmpty() && !quest.getTarget().equals(target)) continue;
            data.setProgress(quest.getId(), data.getProgress(quest.getId()) + amount);
            if (data.getProgress(quest.getId()) >= quest.getRequiredCount()) {
                completeQuest(player, quest, data);
            }
        }
        savePlayerData(data);
    }

    private boolean isQuestCompleteForPeriod(PlayerQuestData data, Quest quest) {
        return switch (quest.getPeriod()) {
            case DAILY -> data.getCompletedToday().contains(quest.getId());
            case WEEKLY -> data.getCompletedThisWeek().contains(quest.getId());
            case SPECIAL -> data.isCompleted(quest.getId());
        };
    }

    private void checkReset(PlayerQuestData data) {
        long now = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long todayStart = cal.getTimeInMillis();
        if (data.getLastDailyReset() < todayStart) {
            data.getCompletedToday().clear();
            data.setLastDailyReset(now);
            quests.values().stream().filter(q -> q.getPeriod() == QuestPeriod.DAILY)
                    .forEach(q -> data.setProgress(q.getId(), 0));
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        long weekStart = cal.getTimeInMillis();
        if (data.getLastWeeklyReset() < weekStart) {
            data.getCompletedThisWeek().clear();
            data.setLastWeeklyReset(now);
            quests.values().stream().filter(q -> q.getPeriod() == QuestPeriod.WEEKLY)
                    .forEach(q -> data.setProgress(q.getId(), 0));
        }
    }

    private void completeQuest(Player player, Quest quest, PlayerQuestData data) {
        data.addCompleted(quest.getId());
        switch (quest.getPeriod()) {
            case DAILY -> data.getCompletedToday().add(quest.getId());
            case WEEKLY -> data.getCompletedThisWeek().add(quest.getId());
            case SPECIAL -> {}
        }
        // Give rewards
        QuestReward reward = quest.getReward();
        player.giveExp(reward.getXp());
        for (String itemStr : reward.getItems()) {
            String[] parts = itemStr.split(":");
            try {
                Material mat = Material.valueOf(parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                player.getInventory().addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }
        // Announce
        String msg = ChatColor.YELLOW + "✦ " + player.getName() + " 님이 퀘스트 [" + quest.getName() + "]을 완료했습니다! ✦";
        Bukkit.broadcastMessage(msg);
        player.sendTitle(ChatColor.YELLOW + "퀘스트 완료!", ChatColor.WHITE + quest.getName(), 10, 60, 20);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.2f);

        // Integrations
        notifyIntegrations(player, quest, reward.getSkillPoints());
    }

    private void notifyIntegrations(Player player, Quest quest, int skillPoints) {
        try {
            Class<?> ac = Class.forName("com.junggyeol.achievement.AchievementAPI");
            ac.getMethod("trigger", Player.class, String.class).invoke(null, player, "quest_" + quest.getId());
        } catch (Exception ignored) {}
        try {
            Class<?> ec = Class.forName("com.junggyeol.encyclopedia.EncyclopediaAPI");
            ec.getMethod("discoverEntry", Player.class, String.class, String.class)
                    .invoke(null, player, "QUEST", quest.getId());
        } catch (Exception ignored) {}
        if (skillPoints > 0) {
            try {
                Class<?> sc = Class.forName("com.junggyeol.skilltree.SkillTreeAPI");
                sc.getMethod("addSkillPoints", Player.class, int.class).invoke(null, player, skillPoints);
            } catch (Exception ignored) {}
        }
        try {
            Class<?> stc = Class.forName("com.junggyeol.stats.StatAPI");
            stc.getMethod("addStat", Player.class, String.class, int.class)
                    .invoke(null, player, "quests_completed", 1);
        } catch (Exception ignored) {}
    }
}
