package com.junggyeol.crafting;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CustomCraftingAPI {
    public static ItemStack getCustomItem(String itemId) {
        CustomCraftingPlugin plugin = CustomCraftingPlugin.getInstance();
        if (plugin == null) return null;
        CustomItem item = plugin.getItemManager().getItem(itemId);
        if (item == null) return null;
        return item.buildItemStack(1);
    }

    public static void giveCustomItem(Player player, String itemId, int amount) {
        CustomCraftingPlugin plugin = CustomCraftingPlugin.getInstance();
        if (plugin == null) return;
        CustomItem item = plugin.getItemManager().getItem(itemId);
        if (item == null) return;
        player.getInventory().addItem(item.buildItemStack(amount));
    }
}
