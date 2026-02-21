package com.junggyeol.supplybox;

import org.bukkit.ChatColor;

public enum SupplyBoxGrade {
    COMMON(ChatColor.WHITE + "일반", ChatColor.WHITE),
    RARE(ChatColor.BLUE + "희귀", ChatColor.BLUE),
    LEGENDARY(ChatColor.GOLD + "전설", ChatColor.GOLD);

    private final String displayName;
    private final ChatColor color;

    SupplyBoxGrade(String displayName, ChatColor color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() { return displayName; }
    public ChatColor getColor() { return color; }
}
