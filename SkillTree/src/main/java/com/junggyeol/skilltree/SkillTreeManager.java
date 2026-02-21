package com.junggyeol.skilltree;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.*;

public class SkillTreeManager {
    private final SkillTreePlugin plugin;
    private final Map<String, SkillTree> trees = new LinkedHashMap<>();
    private final Map<UUID, PlayerSkillData> playerDataMap = new HashMap<>();
    private final File dataFolder;

    public SkillTreeManager(SkillTreePlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("데이터 폴더 생성 실패");
        }
    }

    public void loadTrees() {
        trees.clear();
        ConfigurationSection treeSec = plugin.getConfig().getConfigurationSection("trees");
        if (treeSec == null) return;
        for (String treeId : treeSec.getKeys(false)) {
            ConfigurationSection tc = treeSec.getConfigurationSection(treeId);
            if (tc == null) continue;
            String treeName = tc.getString("name", treeId);
            SkillTree tree = new SkillTree(treeId, treeName);
            ConfigurationSection skillsSec = tc.getConfigurationSection("skills");
            if (skillsSec != null) {
                for (String skillId : skillsSec.getKeys(false)) {
                    ConfigurationSection sc = skillsSec.getConfigurationSection(skillId);
                    if (sc == null) continue;
                    String name = sc.getString("name", skillId);
                    String desc = sc.getString("description", "");
                    int maxLevel = sc.getInt("max_level", 5);
                    int cost = sc.getInt("cost", 1);
                    String requiresRaw = sc.getString("requires", null);
                    String requiresId = null;
                    int requiresLevel = 0;
                    if (requiresRaw != null && !requiresRaw.equals("null")) {
                        String[] parts = requiresRaw.split(":");
                        requiresId = parts[0];
                        if (parts.length > 1) requiresLevel = Integer.parseInt(parts[1]);
                    }
                    tree.addSkill(new Skill(skillId, treeId, name, desc, maxLevel, cost, requiresId, requiresLevel));
                }
            }
            trees.put(treeId, tree);
        }
        plugin.getLogger().info(trees.size() + "개의 스킬 트리를 로드했습니다.");
    }

    public Map<String, SkillTree> getTrees() { return trees; }

    public PlayerSkillData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), this::loadPlayerData);
    }

    private PlayerSkillData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerSkillData data = new PlayerSkillData(uuid);
        if (!file.exists()) return data;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        data.setSkillPoints(yaml.getInt("skillPoints", 0));
        ConfigurationSection levels = yaml.getConfigurationSection("skills");
        if (levels != null) {
            for (String key : levels.getKeys(false)) {
                data.setSkillLevel(key, levels.getInt(key));
            }
        }
        return data;
    }

    public void savePlayerData(PlayerSkillData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("skillPoints", data.getSkillPoints());
        for (Map.Entry<String, Integer> e : data.getSkillLevels().entrySet()) {
            yaml.set("skills." + e.getKey(), e.getValue());
        }
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("플레이어 데이터 저장 실패: " + data.getUuid() + ": " + e.getMessage()); }
    }

    public void saveAll() {
        for (PlayerSkillData data : playerDataMap.values()) savePlayerData(data);
    }

    public boolean upgradeSkill(Player player, String treeId, String skillId) {
        SkillTree tree = trees.get(treeId);
        if (tree == null) return false;
        Skill skill = tree.getSkills().get(skillId);
        if (skill == null) return false;
        PlayerSkillData data = getPlayerData(player);
        int currentLevel = data.getSkillLevel(skillId);
        if (currentLevel >= skill.getMaxLevel()) {
            player.sendMessage(org.bukkit.ChatColor.RED + "이미 최대 레벨입니다.");
            return false;
        }
        // Check requirements
        if (skill.getRequiresSkillId() != null) {
            if (data.getSkillLevel(skill.getRequiresSkillId()) < skill.getRequiresLevel()) {
                player.sendMessage(org.bukkit.ChatColor.RED + "선행 스킬 조건을 충족하지 못했습니다: " + skill.getRequiresSkillId() + " Lv." + skill.getRequiresLevel());
                return false;
            }
        }
        if (!data.spendSkillPoints(skill.getCost())) {
            player.sendMessage(org.bukkit.ChatColor.RED + "스킬 포인트가 부족합니다. 필요: " + skill.getCost() + ", 보유: " + data.getSkillPoints());
            return false;
        }
        data.setSkillLevel(skillId, currentLevel + 1);
        player.sendMessage(org.bukkit.ChatColor.GREEN + "스킬 [" + skill.getName() + "] Lv." + (currentLevel + 1) + " 해금!");
        player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        savePlayerData(data);
        applyPassiveEffects(player, data);
        notifyIntegrations(player, skill);
        return true;
    }

    public void applyPassiveEffects(Player player, PlayerSkillData data) {
        // Night vision
        int nightVisionLevel = data.getSkillLevel("night_vision");
        if (nightVisionLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, true, false));
        }
        // Natural heal
        int naturalHealLevel = data.getSkillLevel("natural_heal");
        if (naturalHealLevel > 0) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, naturalHealLevel - 1, true, false));
        }
    }

    private void notifyIntegrations(Player player, Skill skill) {
        try {
            Class<?> ac = Class.forName("com.junggyeol.achievement.AchievementAPI");
            ac.getMethod("trigger", Player.class, String.class).invoke(null, player, "skill_" + skill.getId());
        } catch (Exception ignored) {}
        try {
            Class<?> ec = Class.forName("com.junggyeol.encyclopedia.EncyclopediaAPI");
            ec.getMethod("discoverEntry", Player.class, String.class, String.class)
                    .invoke(null, player, "SKILL", skill.getId());
        } catch (Exception ignored) {}
    }

    public int getSkillLevel(Player player, String skillId) {
        return getPlayerData(player).getSkillLevel(skillId);
    }

    public void addSkillPoints(Player player, int amount) {
        PlayerSkillData data = getPlayerData(player);
        data.addSkillPoints(amount);
        savePlayerData(data);
        player.sendMessage(org.bukkit.ChatColor.AQUA + "스킬 포인트 +" + amount + " (보유: " + data.getSkillPoints() + ")");
    }
}
