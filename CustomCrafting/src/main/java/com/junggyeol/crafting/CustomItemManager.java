package com.junggyeol.crafting;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CustomItemManager {
    private final CustomCraftingPlugin plugin;
    private final Map<String, CustomItem> items = new LinkedHashMap<>();

    public CustomItemManager(CustomCraftingPlugin plugin) { this.plugin = plugin; }

    public void loadItems() {
        items.clear();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("custom-items");
        if (section == null) return;
        for (String id : section.getKeys(false)) {
            ConfigurationSection ic = section.getConfigurationSection(id);
            if (ic == null) continue;
            String name = ic.getString("name", id);
            List<String> lore = ic.getStringList("lore");
            Material material;
            try { material = Material.valueOf(ic.getString("material", "PAPER")); }
            catch (Exception e) { material = Material.PAPER; }
            int customModelData = ic.getInt("custom-model-data", 0);
            boolean oneTimeUse = ic.getBoolean("one-time-use", false);
            String effect = ic.getString("effect", "");
            double dropChance = ic.getDouble("double-drop-chance", 0.0);
            items.put(id, new CustomItem(id, name, lore, material, customModelData, oneTimeUse, effect, dropChance));
        }
        plugin.getLogger().info(items.size() + "개의 커스텀 아이템을 로드했습니다.");
    }

    public Map<String, CustomItem> getItems() { return items; }

    public CustomItem getItem(String id) { return items.get(id); }

    public String getCustomItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, CustomItem.PDC_KEY);
        if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        }
        return null;
    }
}
