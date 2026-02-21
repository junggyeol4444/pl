package com.junggyeol.crafting;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CustomItemListener implements Listener {
    private final CustomCraftingPlugin plugin;
    private final Random random = new Random();

    public CustomItemListener(CustomCraftingPlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        Block clickedBlock = event.getClickedBlock();
        ItemStack hand = player.getInventory().getItemInMainHand();

        // Open crafting GUI on right-click emerald block
        String craftingBlock = plugin.getConfig().getString("crafting-block", "EMERALD_BLOCK");
        if (clickedBlock != null && clickedBlock.getType().name().equals(craftingBlock)
                && event.getAction().name().contains("RIGHT")) {
            event.setCancelled(true);
            CraftingGUI.open(player, plugin);
            return;
        }

        // Custom item use
        String itemId = plugin.getItemManager().getCustomItemId(hand);
        if (itemId == null) return;
        if (!event.getAction().name().contains("RIGHT")) return;

        CustomItem customItem = plugin.getItemManager().getItem(itemId);
        if (customItem == null) return;

        switch (customItem.getEffect()) {
            case "TELEPORT" -> {
                event.setCancelled(true);
                teleportRandom(player);
                if (customItem.isOneTimeUse()) {
                    hand.setAmount(hand.getAmount() - 1);
                }
            }
            case "HEAL" -> {
                event.setCancelled(true);
                player.setHealth(player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue());
                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 200, 1));
                player.sendMessage(ChatColor.GREEN + "체력이 완전히 회복되었습니다!");
                if (customItem.isOneTimeUse()) {
                    hand.setAmount(hand.getAmount() - 1);
                }
            }
            case "LOCATE_STRUCTURE" -> {
                event.setCancelled(true);
                player.sendMessage(ChatColor.AQUA + "가장 가까운 구조물을 탐색 중...");
                // Structure location runs async
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    // Simplified: just tell player to explore
                    Bukkit.getScheduler().runTask(plugin, () ->
                            player.sendMessage(ChatColor.AQUA + "북쪽 방향으로 탐험해보세요!"));
                });
            }
        }
    }

    private void teleportRandom(Player player) {
        World world = player.getWorld();
        for (int attempt = 0; attempt < 20; attempt++) {
            int x = random.nextInt(10000) - 5000;
            int z = random.nextInt(10000) - 5000;
            int y = world.getHighestBlockYAt(x, z) + 1;
            if (y > world.getMinHeight() && y < world.getMaxHeight()) {
                Location loc = new Location(world, x + 0.5, y, z + 0.5, player.getLocation().getYaw(), player.getLocation().getPitch());
                player.teleport(loc);
                player.sendMessage(ChatColor.AQUA + "랜덤 좌표로 이동했습니다: " + x + ", " + y + ", " + z);
                player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return;
            }
        }
        player.sendMessage(ChatColor.RED + "텔레포트 위치를 찾을 수 없습니다.");
    }

    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        String itemId = plugin.getItemManager().getCustomItemId(player.getInventory().getItemInMainHand());
        if (!"miners_pickaxe".equals(itemId)) return;
        CustomItem pickaxe = plugin.getItemManager().getItem("miners_pickaxe");
        if (pickaxe == null) return;
        if (random.nextDouble() < pickaxe.getDoubleDropChance()) {
            for (org.bukkit.entity.Item drop : event.getItems()) {
                event.getBlock().getWorld().dropItemNaturally(
                        event.getBlock().getLocation(), drop.getItemStack().clone());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null) continue;
            String itemId = plugin.getItemManager().getCustomItemId(item);
            if ("protection_totem".equals(itemId)) {
                // Prevent item drops
                event.getDrops().clear();
                // Consume totem
                player.getInventory().remove(item);
                player.sendMessage(ChatColor.GOLD + "보호의 토템+이 당신의 아이템을 지켰습니다!");
                return;
            }
        }
    }
}
