package com.junggyeol.achievement;

import java.util.List;

public class AchievementReward {
    private final int xp;
    private final List<String> items;
    private final String title;

    public AchievementReward(int xp, List<String> items, String title) {
        this.xp = xp;
        this.items = items;
        this.title = title;
    }

    public int getXp() { return xp; }
    public List<String> getItems() { return items; }
    public String getTitle() { return title; }
}
