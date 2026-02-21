package com.junggyeol.quest;

import java.util.*;

public class PlayerQuestData {
    private final UUID uuid;
    private final Map<String, Integer> progress = new HashMap<>();
    private final Set<String> completedQuests = new HashSet<>();
    private final Set<String> completedToday = new HashSet<>();
    private final Set<String> completedThisWeek = new HashSet<>();
    private long lastDailyReset = 0L;
    private long lastWeeklyReset = 0L;

    public PlayerQuestData(UUID uuid) { this.uuid = uuid; }

    public UUID getUuid() { return uuid; }
    public int getProgress(String questId) { return progress.getOrDefault(questId, 0); }
    public void setProgress(String questId, int value) { progress.put(questId, value); }
    public void incrementProgress(String questId) { progress.merge(questId, 1, Integer::sum); }
    public Set<String> getCompletedQuests() { return completedQuests; }
    public boolean isCompleted(String questId) { return completedQuests.contains(questId); }
    public void addCompleted(String questId) { completedQuests.add(questId); }
    public Set<String> getCompletedToday() { return completedToday; }
    public Set<String> getCompletedThisWeek() { return completedThisWeek; }
    public long getLastDailyReset() { return lastDailyReset; }
    public void setLastDailyReset(long t) { this.lastDailyReset = t; }
    public long getLastWeeklyReset() { return lastWeeklyReset; }
    public void setLastWeeklyReset(long t) { this.lastWeeklyReset = t; }
}
