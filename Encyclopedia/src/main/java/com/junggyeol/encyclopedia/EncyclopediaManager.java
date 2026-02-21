package com.junggyeol.encyclopedia;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.*;

public class EncyclopediaManager {
    private final EncyclopediaPlugin plugin;
    private final Map<String, Map<String, EncyclopediaEntry>> registry = new LinkedHashMap<>();
    private final Map<UUID, PlayerEncyclopediaData> playerData = new HashMap<>();
    private final File dataFolder;

    public EncyclopediaManager(EncyclopediaPlugin plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("데이터 폴더 생성 실패: " + dataFolder.getPath());
        }
    }

    public void registerEntry(EncyclopediaEntry entry) {
        registry.computeIfAbsent(entry.getCategory(), k -> new LinkedHashMap<>())
                .put(entry.getId(), entry);
    }

    public void discoverEntry(Player player, String category, String id) {
        PlayerEncyclopediaData data = getPlayerData(player);
        if (data.isDiscovered(category, id)) return;
        data.discover(category, id);
        notifyDiscovery(player, category, id);
        checkMilestones(player, data);
        savePlayerData(data);
    }

    private void notifyDiscovery(Player player, String category, String id) {
        Map<String, EncyclopediaEntry> catMap = registry.get(category);
        String name = catMap != null && catMap.containsKey(id) ? catMap.get(id).getName() : id;
        String catDisplay = plugin.getConfig().getString("categories." + category, category);
        player.sendMessage(ChatColor.LIGHT_PURPLE + "[도감] 새로운 항목 발견: " + ChatColor.WHITE + name + ChatColor.LIGHT_PURPLE + " (" + catDisplay + ")");
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private void checkMilestones(Player player, PlayerEncyclopediaData data) {
        int total = getTotalEntries();
        if (total == 0) return;
        int discovered = getTotalDiscovered(data);
        int percent = (int) ((discovered * 100L) / total);
        int[] milestones = {25, 50, 75, 100};
        for (int milestone : milestones) {
            if (percent >= milestone && !data.getClaimedMilestones().contains(milestone)) {
                data.claimMilestone(milestone);
                ConfigurationSection sec = plugin.getConfig().getConfigurationSection("milestone-rewards." + milestone);
                if (sec != null) {
                    int xp = sec.getInt("xp", 0);
                    String msg = sec.getString("message", "");
                    player.giveExp(xp);
                    player.sendMessage(ChatColor.GOLD + "[도감] " + msg);
                }
            }
        }
    }

    private int getTotalEntries() {
        return registry.values().stream().mapToInt(Map::size).sum();
    }

    private int getTotalDiscovered(PlayerEncyclopediaData data) {
        return data.getAllDiscovered().values().stream().mapToInt(Set::size).sum();
    }

    public boolean isDiscovered(Player player, String category, String id) {
        return getPlayerData(player).isDiscovered(category, id);
    }

    public double getDiscoveryRate(Player player, String category) {
        Map<String, EncyclopediaEntry> catMap = registry.get(category);
        if (catMap == null || catMap.isEmpty()) return 0.0;
        int total = catMap.size();
        int found = getPlayerData(player).getDiscovered(category).size();
        return (double) found / total * 100.0;
    }

    public PlayerEncyclopediaData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), uuid -> loadPlayerData(uuid));
    }

    private PlayerEncyclopediaData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerEncyclopediaData data = new PlayerEncyclopediaData(uuid);
        if (!file.exists()) return data;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection disc = yaml.getConfigurationSection("discovered");
        if (disc != null) {
            for (String cat : disc.getKeys(false)) {
                List<String> ids = yaml.getStringList("discovered." + cat);
                for (String id : ids) data.discover(cat, id);
            }
        }
        for (int m : yaml.getIntegerList("milestones")) data.claimMilestone(m);
        return data;
    }

    public void savePlayerData(PlayerEncyclopediaData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        for (Map.Entry<String, Set<String>> entry : data.getAllDiscovered().entrySet()) {
            yaml.set("discovered." + entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        yaml.set("milestones", new ArrayList<>(data.getClaimedMilestones()));
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("저장 실패: " + e.getMessage()); }
    }

    public void saveAll() {
        for (PlayerEncyclopediaData data : playerData.values()) savePlayerData(data);
    }

    public Map<String, Map<String, EncyclopediaEntry>> getRegistry() { return registry; }

    public String getCategoryDisplay(String category) {
        return plugin.getConfig().getString("categories." + category, category);
    }

    public int getTotalEntries(String category) {
        Map<String, EncyclopediaEntry> m = registry.get(category);
        return m == null ? 0 : m.size();
    }

    public int getDiscoveredCount(Player player, String category) {
        return getPlayerData(player).getDiscovered(category).size();
    }
}
