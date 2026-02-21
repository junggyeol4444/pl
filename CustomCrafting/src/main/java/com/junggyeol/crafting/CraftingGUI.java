package com.junggyeol.crafting;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class CraftingGUI implements Listener {
    private static final String GUI_TITLE = "커스텀 제작대";
    // Grid slots in 54-slot inventory: slots 10,11,12, 19,20,21, 28,29,30
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int RESULT_SLOT = 24;
    private static final int CRAFT_BUTTON_SLOT = 23;

    private final CustomCraftingPlugin plugin;
    // Track current result per player
    private final Map<UUID, CustomRecipe> currentRecipe = new HashMap<>();

    public CraftingGUI(CustomCraftingPlugin plugin) { this.plugin = plugin; }

    public static void open(Player player, CustomCraftingPlugin plugin) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);
        // Fill border with gray glass
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.setDisplayName(" ");
        glass.setItemMeta(gm);
        for (int i = 0; i < 54; i++) {
            if (!isGridSlot(i) && i != RESULT_SLOT && i != CRAFT_BUTTON_SLOT) {
                inv.setItem(i, glass);
            }
        }
        // Craft arrow button
        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta am = arrow.getItemMeta();
        am.setDisplayName(ChatColor.YELLOW + "► 제작");
        arrow.setItemMeta(am);
        inv.setItem(CRAFT_BUTTON_SLOT, arrow);

        player.openInventory(inv);
    }

    private static boolean isGridSlot(int slot) {
        for (int gs : GRID_SLOTS) if (gs == slot) return true;
        return false;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!GUI_TITLE.equals(event.getView().getTitle())) return;

        int slot = event.getRawSlot();
        // Block clicking on glass/result/craft button
        if (slot == RESULT_SLOT) {
            // Take result
            event.setCancelled(true);
            takeResult(player, event.getInventory());
            return;
        }
        if (slot == CRAFT_BUTTON_SLOT) {
            event.setCancelled(true);
            updateResult(player, event.getInventory());
            return;
        }
        ItemStack clicked = event.getCurrentItem();
        if (clicked != null && clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }
        // After click, update result on next tick
        Bukkit.getScheduler().runTask(plugin, () -> updateResult(player, event.getInventory()));
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!GUI_TITLE.equals(event.getView().getTitle())) return;
        // Return grid items to player
        Inventory inv = event.getInventory();
        for (int slot : GRID_SLOTS) {
            ItemStack item = inv.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
                for (ItemStack lo : leftover.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), lo);
                }
                inv.setItem(slot, null);
            }
        }
        // Return result
        ItemStack result = inv.getItem(RESULT_SLOT);
        if (result != null && result.getType() != Material.AIR) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(result);
            for (ItemStack lo : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), lo);
            }
        }
        currentRecipe.remove(player.getUniqueId());
    }

    private void updateResult(Player player, Inventory inv) {
        Material[] grid = new Material[9];
        for (int i = 0; i < 9; i++) {
            ItemStack item = inv.getItem(GRID_SLOTS[i]);
            grid[i] = (item == null || item.getType() == Material.AIR) ? Material.AIR : item.getType();
        }
        CustomRecipe recipe = plugin.getRecipeManager().findMatch(grid);
        if (recipe != null) {
            currentRecipe.put(player.getUniqueId(), recipe);
            CustomItem resultItem = plugin.getItemManager().getItem(recipe.getResultItemId());
            if (resultItem != null) {
                inv.setItem(RESULT_SLOT, resultItem.buildItemStack(recipe.getResultAmount()));
            } else {
                inv.setItem(RESULT_SLOT, null);
            }
        } else {
            currentRecipe.remove(player.getUniqueId());
            inv.setItem(RESULT_SLOT, null);
        }
    }

    private void takeResult(Player player, Inventory inv) {
        ItemStack result = inv.getItem(RESULT_SLOT);
        if (result == null || result.getType() == Material.AIR) return;
        CustomRecipe recipe = currentRecipe.get(player.getUniqueId());
        if (recipe == null) return;

        // Consume ingredients
        for (int i = 0; i < 9; i++) {
            ItemStack ingredient = inv.getItem(GRID_SLOTS[i]);
            if (ingredient != null && ingredient.getType() != Material.AIR) {
                ingredient.setAmount(ingredient.getAmount() - 1);
                if (ingredient.getAmount() <= 0) inv.setItem(GRID_SLOTS[i], null);
            }
        }

        // Give result
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(result.clone());
        for (ItemStack lo : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), lo);
        }
        inv.setItem(RESULT_SLOT, null);
        currentRecipe.remove(player.getUniqueId());

        // Integrations
        notifyIntegrations(player, recipe.getResultItemId());
        updateResult(player, inv);
    }

    private void notifyIntegrations(Player player, String itemId) {
        CustomItem item = plugin.getItemManager().getItem(itemId);
        if (item == null) return;
        try {
            Class<?> apiClass = Class.forName("com.junggyeol.encyclopedia.EncyclopediaAPI");
            apiClass.getMethod("discoverEntry", Player.class, String.class, String.class)
                    .invoke(null, player, "ITEM", itemId);
        } catch (Exception ignored) {}
        try {
            Class<?> apiClass = Class.forName("com.junggyeol.achievement.AchievementAPI");
            apiClass.getMethod("trigger", Player.class, String.class)
                    .invoke(null, player, "crafter");
        } catch (Exception ignored) {}
        try {
            Class<?> apiClass = Class.forName("com.junggyeol.stats.StatAPI");
            apiClass.getMethod("addStat", Player.class, String.class, int.class)
                    .invoke(null, player, "items_crafted", 1);
        } catch (Exception ignored) {}
    }
}
