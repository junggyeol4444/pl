package com.junggyeol.crafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.stream.Collectors;

public class CustomItem {
    public static final String PDC_KEY = "custom_item_id";
    private final String id;
    private final String name;
    private final List<String> lore;
    private final Material material;
    private final int customModelData;
    private final boolean oneTimeUse;
    private final String effect;
    private final double doubleDropChance;

    public CustomItem(String id, String name, List<String> lore, Material material,
                      int customModelData, boolean oneTimeUse, String effect, double doubleDropChance) {
        this.id = id;
        this.name = name;
        this.lore = lore;
        this.material = material;
        this.customModelData = customModelData;
        this.oneTimeUse = oneTimeUse;
        this.effect = effect;
        this.doubleDropChance = doubleDropChance;
    }

    public ItemStack buildItemStack(int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        if (lore != null) {
            meta.setLore(lore.stream()
                    .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                    .collect(Collectors.toList()));
        }
        if (customModelData > 0) meta.setCustomModelData(customModelData);
        NamespacedKey key = new NamespacedKey(CustomCraftingPlugin.getInstance(), PDC_KEY);
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        item.setItemMeta(meta);
        return item;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Material getMaterial() { return material; }
    public boolean isOneTimeUse() { return oneTimeUse; }
    public String getEffect() { return effect != null ? effect : ""; }
    public double getDoubleDropChance() { return doubleDropChance; }
}
