package com.junggyeol.crafting;

import org.bukkit.Material;

import java.util.Map;

public class CustomRecipe {
    private final String id;
    private final String resultItemId;
    private final int resultAmount;
    private final String[] shape; // 3 rows
    private final Map<Character, Material> ingredients;

    public CustomRecipe(String id, String resultItemId, int resultAmount, String[] shape, Map<Character, Material> ingredients) {
        this.id = id;
        this.resultItemId = resultItemId;
        this.resultAmount = resultAmount;
        this.shape = shape;
        this.ingredients = ingredients;
    }

    public String getId() { return id; }
    public String getResultItemId() { return resultItemId; }
    public int getResultAmount() { return resultAmount; }
    public String[] getShape() { return shape; }
    public Map<Character, Material> getIngredients() { return ingredients; }

    public boolean matches(Material[] grid) {
        // grid is 9 elements (3x3)
        for (int row = 0; row < 3; row++) {
            String rowStr = row < shape.length ? shape[row] : "   ";
            while (rowStr.length() < 3) rowStr += " ";
            for (int col = 0; col < 3; col++) {
                char c = rowStr.charAt(col);
                Material expected = c == ' ' ? Material.AIR : ingredients.get(c);
                Material actual = grid[row * 3 + col];
                if (actual == null) actual = Material.AIR;
                if (expected == null) expected = Material.AIR;
                if (expected != actual) return false;
            }
        }
        return true;
    }
}
