package com.junggyeol.achievement.api;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement;
import com.junggyeol.achievement.model.PlayerAchievement;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * AchievementSystem 공개 API
 * 다른 플러그인에서 이 클래스를 통해 업적 시스템과 연동할 수 있습니다.
 *
 * <pre>
 * // 다른 플러그인에서 사용 예시:
 * AchievementAPI api = AchievementAPI.getInstance();
 * if (api != null) {
 *     api.grantAchievement(player, "mine_diamond");
 *     boolean hasIt = api.hasAchievement(player.getUniqueId(), "mine_diamond");
 * }
 * </pre>
 */
public class AchievementAPI {

    private static AchievementAPI instance;
    private final AchievementPlugin plugin;

    public AchievementAPI(AchievementPlugin plugin) {
        this.plugin = plugin;
        instance = this;
    }

    /**
     * API 인스턴스를 반환합니다.
     * AchievementSystem 플러그인이 활성화되지 않았으면 null을 반환합니다.
     */
    public static AchievementAPI getInstance() {
        return instance;
    }

    /**
     * 플레이어에게 업적을 부여합니다.
     *
     * @param player      대상 플레이어
     * @param achievementId 업적 ID (config.yml 키)
     * @return 성공하면 true, 업적을 찾을 수 없거나 이미 달성된 경우 false
     */
    public boolean grantAchievement(Player player, String achievementId) {
        return plugin.getAchievementManager().grantAchievementById(player, achievementId);
    }

    /**
     * 플레이어가 특정 업적을 달성했는지 확인합니다.
     *
     * @param uuid          플레이어 UUID
     * @param achievementId 업적 ID
     * @return 달성했으면 true
     */
    public boolean hasAchievement(UUID uuid, String achievementId) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(uuid);
        return data.hasCompleted(achievementId);
    }

    /**
     * 플레이어가 달성한 모든 업적 ID를 반환합니다.
     *
     * @param uuid 플레이어 UUID
     * @return 달성한 업적 ID 집합 (수정 불가)
     */
    public Set<String> getCompletedAchievements(UUID uuid) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(uuid);
        return data.getCompletedAchievements();
    }

    /**
     * 특정 업적 정보를 반환합니다.
     *
     * @param achievementId 업적 ID
     * @return Achievement 객체 (없으면 null)
     */
    public Achievement getAchievement(String achievementId) {
        return plugin.getAchievementManager().getAchievement(achievementId);
    }

    /**
     * 등록된 모든 업적 목록을 반환합니다.
     */
    public Collection<Achievement> getAllAchievements() {
        return plugin.getAchievementManager().getAllAchievements();
    }

    /**
     * 플레이어의 현재 장착 칭호를 반환합니다.
     *
     * @param uuid 플레이어 UUID
     * @return 칭호 문자열 (없으면 null)
     */
    public String getActiveTitle(UUID uuid) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(uuid);
        return data.getActiveTitle();
    }

    /**
     * 플레이어의 보유 칭호 목록을 반환합니다.
     *
     * @param uuid 플레이어 UUID
     * @return 칭호 집합
     */
    public Set<String> getOwnedTitles(UUID uuid) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(uuid);
        return data.getOwnedTitles();
    }

    /**
     * 플레이어의 특정 업적 진행도를 강제로 설정합니다.
     * 이 메서드를 호출한 후 checkAchievements를 호출하여 달성 여부를 체크하세요.
     *
     * @param uuid          플레이어 UUID
     * @param achievementId 업적 ID
     * @param value         진행도 값
     */
    public void setProgress(UUID uuid, String achievementId, long value) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(uuid);
        data.setProgress(achievementId, value);
    }

    /**
     * 플레이어의 모든 업적 달성 여부를 다시 체크합니다.
     *
     * @param player 대상 플레이어
     */
    public void checkAchievements(Player player) {
        plugin.getAchievementManager().checkAllAchievements(player);
    }

    /**
     * API 인스턴스를 무효화합니다 (플러그인 비활성화 시 호출).
     */
    public static void invalidate() {
        instance = null;
    }
}
