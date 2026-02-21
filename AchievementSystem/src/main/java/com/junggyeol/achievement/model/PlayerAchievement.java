package com.junggyeol.achievement.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 플레이어별 업적 진행도 모델
 */
public class PlayerAchievement {

    private final UUID playerUUID;

    /** 달성한 업적 ID 집합 */
    private final Set<String> completedAchievements = new HashSet<>();

    /** 업적별 진행도 카운터 (업적ID -> 현재 값) */
    private final Map<String, Long> progress = new HashMap<>();

    /** 현재 장착한 칭호 */
    private String activeTitle = null;

    /** 보유 칭호 목록 */
    private final Set<String> ownedTitles = new HashSet<>();

    /** 연속 접속 일수 추적용 */
    private long lastLoginDay = -1;
    private int consecutiveDays = 0;
    private int totalDays = 0;

    /** 이동 거리 누적 (블록 단위) */
    private long totalWalkDistance = 0;

    /** 블록 설치 카운터 */
    private long totalBlocksPlaced = 0;

    /** 사망 횟수 */
    private int deathCount = 0;

    /** 플레이어 처치 횟수 */
    private int playerKills = 0;

    /** 몹 처치 카운터 (몹 타입 -> 횟수) */
    private final Map<String, Integer> mobKills = new HashMap<>();

    /** 채굴 블록 카운터 (블록 타입 -> 횟수) */
    private final Map<String, Integer> blocksMined = new HashMap<>();

    /** 방문한 월드 목록 */
    private final Set<String> visitedWorlds = new HashSet<>();

    /** 첫 접속 여부 */
    private boolean firstJoin = false;

    public PlayerAchievement(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public UUID getPlayerUUID() { return playerUUID; }

    public Set<String> getCompletedAchievements() { return completedAchievements; }

    public boolean hasCompleted(String achievementId) {
        return completedAchievements.contains(achievementId);
    }

    public void completeAchievement(String achievementId) {
        completedAchievements.add(achievementId);
    }

    public long getProgress(String achievementId) {
        return progress.getOrDefault(achievementId, 0L);
    }

    public void setProgress(String achievementId, long value) {
        progress.put(achievementId, value);
    }

    public Map<String, Long> getAllProgress() { return progress; }

    public String getActiveTitle() { return activeTitle; }

    public void setActiveTitle(String title) { this.activeTitle = title; }

    public Set<String> getOwnedTitles() { return ownedTitles; }

    public void addTitle(String title) { ownedTitles.add(title); }

    public long getLastLoginDay() { return lastLoginDay; }

    public void setLastLoginDay(long day) { this.lastLoginDay = day; }

    public int getConsecutiveDays() { return consecutiveDays; }

    public void setConsecutiveDays(int days) { this.consecutiveDays = days; }

    public int getTotalDays() { return totalDays; }

    public void setTotalDays(int days) { this.totalDays = days; }

    public long getTotalWalkDistance() { return totalWalkDistance; }

    public void addWalkDistance(long distance) { this.totalWalkDistance += distance; }

    public void setTotalWalkDistance(long distance) { this.totalWalkDistance = distance; }

    public long getTotalBlocksPlaced() { return totalBlocksPlaced; }

    public void addBlocksPlaced(long count) { this.totalBlocksPlaced += count; }

    public void setTotalBlocksPlaced(long count) { this.totalBlocksPlaced = count; }

    public int getDeathCount() { return deathCount; }

    public void incrementDeathCount() { this.deathCount++; }

    public void setDeathCount(int count) { this.deathCount = count; }

    public int getPlayerKills() { return playerKills; }

    public void incrementPlayerKills() { this.playerKills++; }

    public void setPlayerKills(int count) { this.playerKills = count; }

    public Map<String, Integer> getMobKills() { return mobKills; }

    public int getMobKills(String mobType) {
        return mobKills.getOrDefault(mobType.toUpperCase(), 0);
    }

    public void incrementMobKills(String mobType) {
        mobKills.merge(mobType.toUpperCase(), 1, Integer::sum);
    }

    public void setMobKills(String mobType, int count) {
        mobKills.put(mobType.toUpperCase(), count);
    }

    public Map<String, Integer> getBlocksMined() { return blocksMined; }

    public int getBlocksMined(String blockType) {
        return blocksMined.getOrDefault(blockType.toUpperCase(), 0);
    }

    public void incrementBlocksMined(String blockType) {
        blocksMined.merge(blockType.toUpperCase(), 1, Integer::sum);
    }

    public void setBlocksMined(String blockType, int count) {
        blocksMined.put(blockType.toUpperCase(), count);
    }

    public Set<String> getVisitedWorlds() { return visitedWorlds; }

    public void addVisitedWorld(String world) { visitedWorlds.add(world.toUpperCase()); }

    public boolean hasVisitedWorld(String world) {
        return visitedWorlds.contains(world.toUpperCase());
    }

    public boolean isFirstJoin() { return firstJoin; }

    public void setFirstJoin(boolean firstJoin) { this.firstJoin = firstJoin; }
}
