package com.junggyeol.skilltree;

public class Skill {
    private final String id;
    private final String treeId;
    private final String name;
    private final String description;
    private final int maxLevel;
    private final int cost;
    private final String requiresSkillId;
    private final int requiresLevel;

    public Skill(String id, String treeId, String name, String description, int maxLevel, int cost, String requiresSkillId, int requiresLevel) {
        this.id = id; this.treeId = treeId; this.name = name; this.description = description;
        this.maxLevel = maxLevel; this.cost = cost; this.requiresSkillId = requiresSkillId;
        this.requiresLevel = requiresLevel;
    }

    public String getId() { return id; }
    public String getTreeId() { return treeId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getMaxLevel() { return maxLevel; }
    public int getCost() { return cost; }
    public String getRequiresSkillId() { return requiresSkillId; }
    public int getRequiresLevel() { return requiresLevel; }
}
