package com.junggyeol.quest;

import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.inventory.CraftItemEvent;

public class QuestListener implements Listener {
    private final QuestPlugin plugin;
    public QuestListener(QuestPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player player)) return;
        String mobType = event.getEntity().getType().name();
        plugin.getQuestManager().incrementProgress(player, QuestType.KILL_MOB, mobType, 1);
    }

    @EventHandler
    public void onMine(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockType = event.getBlock().getType().name();
        plugin.getQuestManager().incrementProgress(player, QuestType.MINE_BLOCK, blockType, 1);
        // Also check deepslate variants
        if (blockType.startsWith("DEEPSLATE_")) {
            String base = blockType.substring("DEEPSLATE_".length());
            plugin.getQuestManager().incrementProgress(player, QuestType.MINE_BLOCK, base, 1);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.hasChangedBlock()) return;
        Player player = event.getPlayer();
        double dist = event.getFrom().distance(event.getTo());
        plugin.getQuestManager().incrementProgress(player, QuestType.TRAVEL_DISTANCE, "", (int) dist);
        // Biome visit
        String biome = player.getLocation().getBlock().getBiome().name();
        plugin.getQuestManager().incrementProgress(player, QuestType.VISIT_BIOME, biome, 1);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String result = event.getRecipe().getResult().getType().name();
        plugin.getQuestManager().incrementProgress(player, QuestType.CRAFT_ITEM, result, 1);
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.getQuestManager().incrementProgress(event.getPlayer(), QuestType.FISH, "", 1);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        String blockType = event.getBlock().getType().name();
        plugin.getQuestManager().incrementProgress(event.getPlayer(), QuestType.PLACE_BLOCK, blockType, 1);
    }
}
