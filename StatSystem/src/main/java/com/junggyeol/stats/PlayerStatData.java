package com.junggyeol.stats;

import java.util.*;

public class PlayerStatData {
    private final UUID uuid;
    private final String playerName;
    private final Map<String, Long> stats = new HashMap<>();
    private long playTimeStart = 0L;
    private int loginStreak = 0;
    private long lastLoginDate = 0L;

    public PlayerStatData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
    }

    public UUID getUuid() { return uuid; }
    public String getPlayerName() { return playerName; }

    public long getStat(String statId) { return stats.getOrDefault(statId, 0L); }

    public void setStat(String statId, long value) { stats.put(statId, value); }

    public void addStat(String statId, long amount) {
        stats.merge(statId, amount, Long::sum);
    }

    public Map<String, Long> getAllStats() { return stats; }

    public long getPlayTimeStart() { return playTimeStart; }
    public void setPlayTimeStart(long t) { this.playTimeStart = t; }

    public int getLoginStreak() { return loginStreak; }
    public void setLoginStreak(int streak) { this.loginStreak = streak; }

    public long getLastLoginDate() { return lastLoginDate; }
    public void setLastLoginDate(long date) { this.lastLoginDate = date; }
}
