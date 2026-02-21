package com.junggyeol.achievement.manager;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement;
import com.junggyeol.achievement.model.Achievement.Category;
import com.junggyeol.achievement.model.Achievement.ConditionType;
import com.junggyeol.achievement.model.PlayerAchievement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * 업적 관리 클래스
 * 업적 로드, 진행도 체크, 달성 처리를 담당합니다.
 */
public class AchievementManager {

    private final AchievementPlugin plugin;

    /** 전체 업적 목록 (ID -> Achievement) */
    private final Map<String, Achievement> achievements = new HashMap<>();

    /** 온라인 플레이어 데이터 캐시 (UUID -> PlayerAchievement) */
    private final Map<UUID, PlayerAchievement> playerCache = new HashMap<>();

    public AchievementManager(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * config.yml에서 업적을 로드합니다.
     */
    public void loadAchievements() {
        achievements.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("achievements");
        if (section == null) {
            plugin.getLogger().warning("config.yml에 achievements 섹션이 없습니다.");
            return;
        }

        for (String id : section.getKeys(false)) {
            try {
                Achievement achievement = parseAchievement(id, section.getConfigurationSection(id));
                if (achievement != null) {
                    achievements.put(id, achievement);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "업적 로드 실패: " + id, e);
            }
        }

        plugin.getLogger().info(achievements.size() + "개의 업적을 로드했습니다.");
    }

    private Achievement parseAchievement(String id, ConfigurationSection section) {
        if (section == null) return null;

        String name = section.getString("name", id);
        String description = section.getString("description", "");
        boolean hidden = section.getBoolean("hidden", false);

        // 카테고리
        Category category;
        try {
            category = Category.valueOf(section.getString("category", "SOCIAL").toUpperCase());
        } catch (IllegalArgumentException e) {
            category = Category.SOCIAL;
        }

        // 아이콘
        Material icon = Material.PAPER;
        String iconStr = section.getString("icon", "PAPER");
        try {
            icon = Material.valueOf(iconStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("알 수 없는 아이콘: " + iconStr + " (업적: " + id + ")");
        }

        // 보상
        int rewardExp = section.getInt("reward.exp", 0);
        String rewardTitle = section.getString("reward.title", null);
        List<String> rewardItems = section.getStringList("reward.items");

        // 조건
        ConditionType conditionType;
        try {
            conditionType = ConditionType.valueOf(section.getString("condition.type", "CUSTOM").toUpperCase());
        } catch (IllegalArgumentException e) {
            conditionType = ConditionType.CUSTOM;
        }

        long conditionValue = section.getLong("condition.value", 1);

        // 조건 추가 정보 (블록 타입, 몹 타입, 월드 이름)
        String conditionExtra = section.getString("condition.block",
                section.getString("condition.mob",
                        section.getString("condition.world", null)));

        return new Achievement(id, name, description, category, icon, hidden,
                rewardExp, rewardTitle, rewardItems, conditionType, conditionValue, conditionExtra);
    }

    // ============================================================
    // 플레이어 데이터 관리
    // ============================================================

    public void loadPlayer(UUID uuid) {
        PlayerAchievement data = plugin.getDataStorage().loadPlayer(uuid);
        playerCache.put(uuid, data);
    }

    public void savePlayer(UUID uuid) {
        PlayerAchievement data = playerCache.get(uuid);
        if (data != null) {
            plugin.getDataStorage().savePlayer(data);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        playerCache.remove(uuid);
    }

    public PlayerAchievement getPlayerData(UUID uuid) {
        return playerCache.computeIfAbsent(uuid, k -> plugin.getDataStorage().loadPlayer(k));
    }

    public void saveAllPlayers() {
        for (UUID uuid : playerCache.keySet()) {
            savePlayer(uuid);
        }
    }

    // ============================================================
    // 업적 조회
    // ============================================================

    public Achievement getAchievement(String id) {
        return achievements.get(id);
    }

    public Collection<Achievement> getAllAchievements() {
        return Collections.unmodifiableCollection(achievements.values());
    }

    public List<Achievement> getAchievementsByCategory(Category category) {
        List<Achievement> result = new ArrayList<>();
        for (Achievement a : achievements.values()) {
            if (a.getCategory() == category) {
                result.add(a);
            }
        }
        return result;
    }

    // ============================================================
    // 진행도 업데이트 및 업적 달성 체크
    // ============================================================

    /**
     * 특정 업적의 달성 여부를 체크하고, 달성 시 처리합니다.
     */
    public void checkAndGrant(Player player, Achievement achievement) {
        PlayerAchievement data = getPlayerData(player.getUniqueId());
        if (data.hasCompleted(achievement.getId())) return;

        boolean achieved = false;
        ConditionType type = achievement.getConditionType();
        long required = achievement.getConditionValue();
        String extra = achievement.getConditionExtra();

        switch (type) {
            case FIRST_JOIN -> achieved = data.isFirstJoin();
            case CONSECUTIVE_DAYS -> achieved = data.getConsecutiveDays() >= required;
            case MINE_BLOCK -> {
                if (extra != null) {
                    achieved = data.getBlocksMined(extra) >= required;
                }
            }
            case KILL_MOB -> {
                if (extra != null) {
                    achieved = data.getMobKills(extra) >= required;
                }
            }
            case KILL_PLAYER -> achieved = data.getPlayerKills() >= required;
            case WALK_DISTANCE -> achieved = data.getTotalWalkDistance() >= required;
            case ENTER_WORLD -> {
                if (extra != null) {
                    achieved = data.hasVisitedWorld(extra);
                }
            }
            case PLACE_BLOCK -> achieved = data.getTotalBlocksPlaced() >= required;
            case DEATH_COUNT -> achieved = data.getDeathCount() >= required;
            case CUSTOM -> {}
        }

        if (achieved) {
            grantAchievement(player, achievement);
        }
    }

    /**
     * 모든 업적에 대해 달성 여부를 체크합니다.
     */
    public void checkAllAchievements(Player player) {
        for (Achievement achievement : achievements.values()) {
            checkAndGrant(player, achievement);
        }
    }

    /**
     * 특정 조건 유형에 해당하는 업적만 체크합니다.
     */
    public void checkAchievementsByType(Player player, ConditionType type) {
        for (Achievement achievement : achievements.values()) {
            if (achievement.getConditionType() == type) {
                checkAndGrant(player, achievement);
            }
        }
    }

    /**
     * 업적을 달성 처리합니다.
     */
    public void grantAchievement(Player player, Achievement achievement) {
        PlayerAchievement data = getPlayerData(player.getUniqueId());
        if (data.hasCompleted(achievement.getId())) return;

        data.completeAchievement(achievement.getId());

        // 보상 지급
        giveRewards(player, achievement);

        // 효과
        announceAchievement(player, achievement);
    }

    /**
     * 업적 ID로 업적을 달성 처리합니다 (API용).
     */
    public boolean grantAchievementById(Player player, String achievementId) {
        Achievement achievement = achievements.get(achievementId);
        if (achievement == null) return false;
        grantAchievement(player, achievement);
        return true;
    }

    private void giveRewards(Player player, Achievement achievement) {
        // 경험치 보상
        if (achievement.getRewardExp() > 0) {
            player.giveExp(achievement.getRewardExp());
        }

        // 칭호 보상
        if (achievement.getRewardTitle() != null && !achievement.getRewardTitle().isEmpty()) {
            PlayerAchievement data = getPlayerData(player.getUniqueId());
            data.addTitle(achievement.getRewardTitle());
            // 첫 칭호라면 자동 장착
            if (data.getActiveTitle() == null) {
                data.setActiveTitle(achievement.getRewardTitle());
            }
            player.sendMessage(Component.text("✨ 칭호 획득: ")
                    .color(NamedTextColor.YELLOW)
                    .append(Component.text("[" + achievement.getRewardTitle() + "]")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD)));
        }
    }

    private void announceAchievement(Player player, Achievement achievement) {
        boolean broadcast = plugin.getConfig().getBoolean("settings.broadcast-on-achievement", true);
        boolean showTitle = plugin.getConfig().getBoolean("settings.show-title", true);
        boolean playSound = plugin.getConfig().getBoolean("settings.play-sound", true);
        boolean showParticles = plugin.getConfig().getBoolean("settings.show-particles", true);

        // 타이틀 표시
        if (showTitle) {
            player.showTitle(Title.title(
                    Component.text("🏆 업적 달성!")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD),
                    Component.text(achievement.getName())
                            .color(NamedTextColor.YELLOW),
                    Title.Times.times(
                            Duration.ofMillis(500),
                            Duration.ofSeconds(3),
                            Duration.ofMillis(500)
                    )
            ));
        }

        // 사운드 재생
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
        }

        // 파티클 효과
        if (showParticles) {
            player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, player.getLocation().add(0, 1, 0), 50, 0.5, 1, 0.5);
        }

        // 서버 공지
        if (broadcast) {
            Component message = Component.text("🏆 ")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text(player.getName()).color(NamedTextColor.YELLOW))
                    .append(Component.text(" 님이 업적 ").color(NamedTextColor.WHITE))
                    .append(Component.text("[" + achievement.getName() + "]").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD))
                    .append(Component.text("을(를) 달성했습니다!").color(NamedTextColor.WHITE));
            Bukkit.broadcast(message);
        } else {
            player.sendMessage(Component.text("🏆 업적 달성: ")
                    .color(NamedTextColor.GOLD)
                    .append(Component.text(achievement.getName()).color(NamedTextColor.YELLOW)));
        }
    }

    // ============================================================
    // Getters
    // ============================================================

    public Map<UUID, PlayerAchievement> getPlayerCache() {
        return playerCache;
    }
}
