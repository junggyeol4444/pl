package com.junggyeol.encyclopedia;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class EncyclopediaAPI {
    public static void registerEntry(String category, String id, String name, String description, Material icon) {
        EncyclopediaPlugin plugin = EncyclopediaPlugin.getInstance();
        if (plugin == null) return;
        plugin.getManager().registerEntry(new EncyclopediaEntry(category, id, name, description, icon, ""));
    }

    public static void discoverEntry(Player player, String category, String id) {
        EncyclopediaPlugin plugin = EncyclopediaPlugin.getInstance();
        if (plugin == null) return;
        plugin.getManager().discoverEntry(player, category, id);
    }

    public static boolean isDiscovered(Player player, String category, String id) {
        EncyclopediaPlugin plugin = EncyclopediaPlugin.getInstance();
        if (plugin == null) return false;
        return plugin.getManager().isDiscovered(player, category, id);
    }

    public static double getDiscoveryRate(Player player, String category) {
        EncyclopediaPlugin plugin = EncyclopediaPlugin.getInstance();
        if (plugin == null) return 0.0;
        return plugin.getManager().getDiscoveryRate(player, category);
    }
}
