package com.junggyeol.quest;
import java.util.List;
public class QuestReward {
    private final int xp;
    private final List<String> items;
    private final int skillPoints;
    public QuestReward(int xp, List<String> items, int skillPoints) {
        this.xp = xp;
        this.items = items;
        this.skillPoints = skillPoints;
    }
    public int getXp() { return xp; }
    public List<String> getItems() { return items; }
    public int getSkillPoints() { return skillPoints; }
}
