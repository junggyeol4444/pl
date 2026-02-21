package com.junggyeol.quest;
public class Quest {
    private final String id;
    private final String name;
    private final String description;
    private final QuestPeriod period;
    private final QuestType type;
    private final String target;
    private final int requiredCount;
    private final QuestReward reward;

    public Quest(String id, String name, String description, QuestPeriod period, QuestType type,
                 String target, int requiredCount, QuestReward reward) {
        this.id = id; this.name = name; this.description = description;
        this.period = period; this.type = type; this.target = target;
        this.requiredCount = requiredCount; this.reward = reward;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public QuestPeriod getPeriod() { return period; }
    public QuestType getType() { return type; }
    public String getTarget() { return target != null ? target : ""; }
    public int getRequiredCount() { return requiredCount; }
    public QuestReward getReward() { return reward; }
}
