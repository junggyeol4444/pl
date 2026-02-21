package com.junggyeol.secretdimension;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.*;

public class DimensionManager {
    private final SecretDimensionPlugin plugin;
    private final Map<String, SecretDimension> dimensions = new LinkedHashMap<>();
    private final Map<UUID, DimensionEntry> activeSessions = new HashMap<>();
    private final Map<UUID, PlayerDimensionData> playerData = new HashMap<>();
    private final File dataFolder;

    public DimensionManager(SecretDimensionPlugin plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder(), "data");
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("데이터 폴더 생성 실패");
        }
    }

    public void loadDimensions() {
        dimensions.clear();
        ConfigurationSection dimSec = plugin.getConfig().getConfigurationSection("dimensions");
        if (dimSec == null) return;
        for (String id : dimSec.getKeys(false)) {
            ConfigurationSection dc = dimSec.getConfigurationSection(id);
            if (dc == null) continue;
            String name = dc.getString("name", id);
            String desc = dc.getString("description", "");
            String condition = dc.getString("entry_condition", "");
            int timeLimit = dc.getInt("time_limit_minutes", 10);
            ConfigurationSection rSec = dc.getConfigurationSection("reward");
            int xp = rSec != null ? rSec.getInt("xp", 0) : 0;
            List<String> items = rSec != null ? rSec.getStringList("items") : new ArrayList<>();
            dimensions.put(id, new SecretDimension(id, name, desc, condition, timeLimit, xp, items));
        }
        plugin.getLogger().info(dimensions.size() + "개의 비밀 차원을 로드했습니다.");
    }

    public Map<String, SecretDimension> getDimensions() { return dimensions; }

    public void enterDimension(Player player, String dimensionId) {
        if (activeSessions.containsKey(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "이미 차원에 입장해 있습니다.");
            return;
        }
        SecretDimension dim = dimensions.get(dimensionId);
        if (dim == null) return;

        Location returnLoc = player.getLocation().clone();
        activeSessions.put(player.getUniqueId(), new DimensionEntry(dimensionId, returnLoc, System.currentTimeMillis()));

        // Teleport to dimension world (use the overworld at special coords as dimension simulation)
        World world = Bukkit.getWorlds().get(0);
        int dimX = getDimOffset(dimensionId);
        Location dimLoc = new Location(world, dimX, 100, 0);
        player.teleport(dimLoc);

        player.sendMessage(ChatColor.LIGHT_PURPLE + "✦ " + dim.getName() + "에 입장했습니다! ✦");
        player.sendMessage(ChatColor.GRAY + "제한 시간: " + dim.getTimeLimitMinutes() + "분");
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 1.0f);
        player.sendTitle(ChatColor.LIGHT_PURPLE + dim.getName(), ChatColor.GRAY + "제한 시간: " + dim.getTimeLimitMinutes() + "분", 10, 80, 20);

        applyDimensionEffects(player, dimensionId);

        // Discover dimension
        PlayerDimensionData data = getPlayerData(player);
        if (!data.hasDiscovered(dimensionId)) {
            data.discover(dimensionId);
            savePlayerData(data);
            notifyDiscovery(player, dimensionId, dim);
        }

        // Schedule timer
        long timeoutTicks = dim.getTimeLimitMinutes() * 60 * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (activeSessions.containsKey(player.getUniqueId())) {
                exitDimension(player, false);
            }
        }, timeoutTicks);

        startBossbar(player, dim);
    }

    private int getDimOffset(String dimensionId) {
        return switch (dimensionId) {
            case "crystal_cave" -> 100000;
            case "sky_island" -> 200000;
            case "dark_labyrinth" -> 300000;
            default -> 500000;
        };
    }

    private void applyDimensionEffects(Player player, String dimensionId) {
        switch (dimensionId) {
            case "crystal_cave" -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
            case "sky_island" -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 0));
            case "dark_labyrinth" -> player.addPotionEffect(new org.bukkit.potion.PotionEffect(
                    org.bukkit.potion.PotionEffectType.BLINDNESS, 100, 0));
        }
    }

    private void startBossbar(Player player, SecretDimension dim) {
        org.bukkit.boss.BossBar bar = Bukkit.createBossBar(
                dim.getName() + " - " + dim.getTimeLimitMinutes() + "분 남음",
                org.bukkit.boss.BarColor.PURPLE,
                org.bukkit.boss.BarStyle.SEGMENTED_10
        );
        bar.addPlayer(player);
        long totalTicks = dim.getTimeLimitMinutes() * 60 * 20L;
        final long[] remaining = {totalTicks};
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!activeSessions.containsKey(player.getUniqueId())) {
                bar.removeAll();
                task.cancel();
                return;
            }
            remaining[0] -= 20;
            if (remaining[0] <= 0) {
                bar.removeAll();
                task.cancel();
                return;
            }
            double progress = (double) remaining[0] / totalTicks;
            bar.setProgress(Math.max(0, Math.min(1, progress)));
            long secs = remaining[0] / 20;
            bar.setTitle(dim.getName() + " - " + (secs / 60) + "분 " + (secs % 60) + "초 남음");
        }, 20L, 20L);
    }

    public void exitDimension(Player player, boolean cleared) {
        DimensionEntry entry = activeSessions.remove(player.getUniqueId());
        if (entry == null) return;
        player.getActivePotionEffects().forEach(e -> player.removePotionEffect(e.getType()));
        player.teleport(entry.getReturnLocation());
        SecretDimension dim = dimensions.get(entry.getDimensionId());
        if (cleared && dim != null) {
            player.sendMessage(ChatColor.GOLD + "✦ " + dim.getName() + " 클리어! 보상을 획득했습니다. ✦");
            giveReward(player, dim);
            notifyIntegrations(player, entry.getDimensionId(), dim);
        } else {
            player.sendMessage(ChatColor.RED + "차원에서 귀환했습니다.");
        }
        player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 1.0f, 0.8f);
    }

    private void giveReward(Player player, SecretDimension dim) {
        player.giveExp(dim.getRewardXp());
        for (String itemStr : dim.getRewardItems()) {
            String[] parts = itemStr.split(":");
            try {
                Material mat = Material.valueOf(parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                player.getInventory().addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }
    }

    private void notifyDiscovery(Player player, String dimensionId, SecretDimension dim) {
        try {
            Class<?> ec = Class.forName("com.junggyeol.encyclopedia.EncyclopediaAPI");
            ec.getMethod("discoverEntry", Player.class, String.class, String.class)
                    .invoke(null, player, "DIMENSION", dimensionId);
        } catch (Exception ignored) {}
    }

    private void notifyIntegrations(Player player, String dimensionId, SecretDimension dim) {
        try {
            Class<?> ac = Class.forName("com.junggyeol.achievement.AchievementAPI");
            ac.getMethod("trigger", Player.class, String.class)
                    .invoke(null, player, "dimension_" + dimensionId);
        } catch (Exception ignored) {}
        try {
            Class<?> stc = Class.forName("com.junggyeol.stats.StatAPI");
            stc.getMethod("addStat", Player.class, String.class, int.class)
                    .invoke(null, player, "dimensions_cleared", 1);
        } catch (Exception ignored) {}
    }

    public PlayerDimensionData getPlayerData(Player player) {
        return playerData.computeIfAbsent(player.getUniqueId(), this::loadPlayerData);
    }

    private PlayerDimensionData loadPlayerData(UUID uuid) {
        File file = new File(dataFolder, uuid + ".yml");
        PlayerDimensionData data = new PlayerDimensionData(uuid);
        if (!file.exists()) return data;
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (String id : yaml.getStringList("discovered")) data.discover(id);
        return data;
    }

    public void savePlayerData(PlayerDimensionData data) {
        File file = new File(dataFolder, data.getUuid() + ".yml");
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("discovered", new ArrayList<>(data.getDiscoveredDimensions()));
        try { yaml.save(file); } catch (IOException e) { plugin.getLogger().warning("저장 실패: " + e.getMessage()); }
    }

    public void saveAll() {
        for (PlayerDimensionData data : playerData.values()) savePlayerData(data);
    }

    public Map<UUID, DimensionEntry> getActiveSessions() { return activeSessions; }

    public boolean checkDiamondPortal(Block centerBlock) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (centerBlock.getRelative(dx, 0, dz).getType() == Material.DIAMOND_BLOCK) count++;
            }
        }
        return count >= 4;
    }
}
