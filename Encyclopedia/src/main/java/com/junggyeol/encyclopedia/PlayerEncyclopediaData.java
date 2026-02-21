package com.junggyeol.encyclopedia;

import java.util.*;

public class PlayerEncyclopediaData {
    private final UUID uuid;
    private final Map<String, Set<String>> discovered = new HashMap<>();
    private final Set<Integer> claimedMilestones = new HashSet<>();

    public PlayerEncyclopediaData(UUID uuid) { this.uuid = uuid; }

    public UUID getUuid() { return uuid; }

    public Set<String> getDiscovered(String category) {
        return discovered.computeIfAbsent(category, k -> new HashSet<>());
    }

    public boolean isDiscovered(String category, String id) {
        return getDiscovered(category).contains(id);
    }

    public void discover(String category, String id) {
        getDiscovered(category).add(id);
    }

    public Map<String, Set<String>> getAllDiscovered() { return discovered; }

    public Set<Integer> getClaimedMilestones() { return claimedMilestones; }

    public void claimMilestone(int percent) { claimedMilestones.add(percent); }
}
