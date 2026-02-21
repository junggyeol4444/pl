package com.junggyeol.achievement;

public class Achievement {
    private final String id;
    private final String name;
    private final String description;
    private final AchievementCategory category;
    private final AchievementReward reward;
    private final String trigger;
    private final String targetBlock;
    private final String targetItem;
    private final int requiredCount;

    public Achievement(String id, String name, String description, AchievementCategory category,
                       AchievementReward reward, String trigger, String targetBlock, String targetItem, int requiredCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.reward = reward;
        this.trigger = trigger;
        this.targetBlock = targetBlock;
        this.targetItem = targetItem;
        this.requiredCount = requiredCount;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public AchievementCategory getCategory() { return category; }
    public AchievementReward getReward() { return reward; }
    public String getTrigger() { return trigger; }
    public String getTargetBlock() { return targetBlock; }
    public String getTargetItem() { return targetItem; }
    public int getRequiredCount() { return requiredCount; }
}
