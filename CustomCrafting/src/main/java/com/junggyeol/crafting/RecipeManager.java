package com.junggyeol.crafting;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

public class RecipeManager {
    private final CustomCraftingPlugin plugin;
    private final List<CustomRecipe> recipes = new ArrayList<>();

    public RecipeManager(CustomCraftingPlugin plugin) { this.plugin = plugin; }

    public void loadRecipes() {
        recipes.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("recipes");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection rc = section.getConfigurationSection(id);
            if (rc == null) continue;
            String resultId = rc.getString("result", "");
            int resultAmount = rc.getInt("result-amount", 1);
            List<String> shapeList = rc.getStringList("shape");
            String[] shape = shapeList.toArray(new String[0]);

            ConfigurationSection ingSection = rc.getConfigurationSection("ingredients");
            Map<Character, Material> ingredients = new HashMap<>();
            if (ingSection != null) {
                for (String key : ingSection.getKeys(false)) {
                    if (key.length() != 1) continue;
                    char c = key.charAt(0);
                    try {
                        Material mat = Material.valueOf(ingSection.getString(key, "AIR"));
                        ingredients.put(c, mat);
                    } catch (Exception ignored) {}
                }
            }
            recipes.add(new CustomRecipe(id, resultId, resultAmount, shape, ingredients));
        }
        plugin.getLogger().info(recipes.size() + "개의 커스텀 레시피를 로드했습니다.");
    }

    public CustomRecipe findMatch(Material[] grid) {
        for (CustomRecipe recipe : recipes) {
            if (recipe.matches(grid)) return recipe;
        }
        return null;
    }

    public List<CustomRecipe> getRecipes() { return recipes; }
}
