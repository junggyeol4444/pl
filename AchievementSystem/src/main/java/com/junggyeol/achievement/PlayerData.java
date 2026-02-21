package com.junggyeol.achievement;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private final Set<String> completedAchievements = new HashSet<>();
    private final Map<String, Integer> progress = new HashMap<>();
    private final Set<String> visitedBiomes = new HashSet<>();
    private final Set<String> titles = new HashSet<>();
    private String equippedTitle = "";

    public PlayerData(UUID uuid) { this.uuid = uuid; }

    public UUID getUuid() { return uuid; }
    public boolean isCompleted(String id) { return completedAchievements.contains(id); }
    public void addCompleted(String id) { completedAchievements.add(id); }
    public Set<String> getCompletedAchievements() { return completedAchievements; }
    public int getProgress(String id) { return progress.getOrDefault(id, 0); }
    public void setProgress(String id, int value) { progress.put(id, value); }
    public void incrementProgress(String id) { progress.merge(id, 1, Integer::sum); }
    public Set<String> getVisitedBiomes() { return visitedBiomes; }
    public void addBiome(String biome) { visitedBiomes.add(biome); }
    public Set<String> getTitles() { return titles; }
    public void addTitle(String title) { titles.add(title); }
    public String getEquippedTitle() { return equippedTitle; }
    public void setEquippedTitle(String title) { this.equippedTitle = title; }
}
