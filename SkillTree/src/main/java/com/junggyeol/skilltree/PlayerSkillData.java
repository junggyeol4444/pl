package com.junggyeol.skilltree;

import java.util.*;

public class PlayerSkillData {
    private final UUID uuid;
    private final Map<String, Integer> skillLevels = new HashMap<>();
    private int skillPoints = 0;

    public PlayerSkillData(UUID uuid) { this.uuid = uuid; }

    public UUID getUuid() { return uuid; }
    public int getSkillLevel(String skillId) { return skillLevels.getOrDefault(skillId, 0); }
    public void setSkillLevel(String skillId, int level) { skillLevels.put(skillId, level); }
    public Map<String, Integer> getSkillLevels() { return skillLevels; }
    public int getSkillPoints() { return skillPoints; }
    public void setSkillPoints(int points) { this.skillPoints = points; }
    public void addSkillPoints(int amount) { this.skillPoints = Math.max(0, this.skillPoints + amount); }
    public boolean spendSkillPoints(int amount) {
        if (skillPoints < amount) return false;
        skillPoints -= amount;
        return true;
    }
}
