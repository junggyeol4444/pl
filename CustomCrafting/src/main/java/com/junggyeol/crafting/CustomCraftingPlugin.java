package com.junggyeol.crafting;

import org.bukkit.plugin.java.JavaPlugin;

public class CustomCraftingPlugin extends JavaPlugin {
    private static CustomCraftingPlugin instance;
    private CustomItemManager itemManager;
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        itemManager = new CustomItemManager(this);
        itemManager.loadItems();
        recipeManager = new RecipeManager(this);
        recipeManager.loadRecipes();
        getCommand("제작대").setExecutor(new CraftingCommand(this));
        getServer().getPluginManager().registerEvents(new CustomItemListener(this), this);
        getServer().getPluginManager().registerEvents(new CraftingGUI(this), this);
        getLogger().info("CustomCrafting 플러그인이 활성화되었습니다.");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomCrafting 플러그인이 비활성화되었습니다.");
    }

    public static CustomCraftingPlugin getInstance() { return instance; }
    public CustomItemManager getItemManager() { return itemManager; }
    public RecipeManager getRecipeManager() { return recipeManager; }
}
