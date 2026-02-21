package com.junggyeol.skilltree;

import java.util.LinkedHashMap;
import java.util.Map;

public class SkillTree {
    private final String id;
    private final String name;
    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public SkillTree(String id, String name) { this.id = id; this.name = name; }

    public String getId() { return id; }
    public String getName() { return name; }
    public Map<String, Skill> getSkills() { return skills; }
    public void addSkill(Skill skill) { skills.put(skill.getId(), skill); }
}
