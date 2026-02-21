package com.junggyeol.secretdimension;

import java.util.List;

public class SecretDimension {
    private final String id;
    private final String name;
    private final String description;
    private final String entryCondition;
    private final int timeLimitMinutes;
    private final int rewardXp;
    private final List<String> rewardItems;

    public SecretDimension(String id, String name, String description, String entryCondition,
                            int timeLimitMinutes, int rewardXp, List<String> rewardItems) {
        this.id = id; this.name = name; this.description = description;
        this.entryCondition = entryCondition; this.timeLimitMinutes = timeLimitMinutes;
        this.rewardXp = rewardXp; this.rewardItems = rewardItems;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getEntryCondition() { return entryCondition; }
    public int getTimeLimitMinutes() { return timeLimitMinutes; }
    public int getRewardXp() { return rewardXp; }
    public List<String> getRewardItems() { return rewardItems; }
}
