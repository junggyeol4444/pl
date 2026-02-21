package com.junggyeol.encyclopedia;

import org.bukkit.Material;

public class EncyclopediaEntry {
    private final String category;
    private final String id;
    private final String name;
    private final String description;
    private final Material icon;
    private final String hint;

    public EncyclopediaEntry(String category, String id, String name, String description, Material icon, String hint) {
        this.category = category;
        this.id = id;
        this.name = name;
        this.description = description;
        this.icon = icon;
        this.hint = hint;
    }

    public String getCategory() { return category; }
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Material getIcon() { return icon; }
    public String getHint() { return hint; }
}
