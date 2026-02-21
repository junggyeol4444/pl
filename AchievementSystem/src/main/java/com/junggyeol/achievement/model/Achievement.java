package com.junggyeol.achievement.model;

import org.bukkit.Material;

import java.util.List;

/**
 * 업적 모델 클래스
 * config.yml에 정의된 업적 정보를 담습니다.
 */
public class Achievement {

    public enum Category {
        MINING("채굴", "⛏"),
        COMBAT("전투", "⚔"),
        EXPLORATION("탐험", "🗺"),
        BUILDING("건축", "🏗"),
        SOCIAL("소셜", "👥");

        private final String displayName;
        private final String emoji;

        Category(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getEmoji() {
            return emoji;
        }
    }

    public enum ConditionType {
        FIRST_JOIN,
        CONSECUTIVE_DAYS,
        MINE_BLOCK,
        KILL_MOB,
        KILL_PLAYER,
        WALK_DISTANCE,
        ENTER_WORLD,
        PLACE_BLOCK,
        DEATH_COUNT,
        CUSTOM
    }

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private final Material icon;
    private final boolean hidden;
    private final int rewardExp;
    private final String rewardTitle;
    private final List<String> rewardItems;
    private final ConditionType conditionType;
    private final long conditionValue;
    private final String conditionExtra; // block type, mob type, world name etc.

    public Achievement(String id, String name, String description, Category category,
                       Material icon, boolean hidden, int rewardExp, String rewardTitle,
                       List<String> rewardItems, ConditionType conditionType,
                       long conditionValue, String conditionExtra) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.icon = icon;
        this.hidden = hidden;
        this.rewardExp = rewardExp;
        this.rewardTitle = rewardTitle;
        this.rewardItems = rewardItems;
        this.conditionType = conditionType;
        this.conditionValue = conditionValue;
        this.conditionExtra = conditionExtra;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public Material getIcon() { return icon; }
    public boolean isHidden() { return hidden; }
    public int getRewardExp() { return rewardExp; }
    public String getRewardTitle() { return rewardTitle; }
    public List<String> getRewardItems() { return rewardItems; }
    public ConditionType getConditionType() { return conditionType; }
    public long getConditionValue() { return conditionValue; }
    public String getConditionExtra() { return conditionExtra; }
}
