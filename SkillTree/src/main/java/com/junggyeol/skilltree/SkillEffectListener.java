package com.junggyeol.skilltree;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class SkillEffectListener implements Listener {
    private final SkillTreePlugin plugin;
    private final Random random = new Random();

    public SkillEffectListener(SkillTreePlugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getManager().applyPassiveEffects(player, plugin.getManager().getPlayerData(player));
    }

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent event) {
        if (event.getNewLevel() > event.getOldLevel()) {
            Player player = event.getPlayer();
            int pointsPerLevel = plugin.getConfig().getInt("skill-point-per-level", 1);
            plugin.getManager().addSkillPoints(player, pointsPerLevel);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Lifesteal
        if (event.getDamager() instanceof Player player) {
            int lifestealLevel = plugin.getManager().getSkillLevel(player, "lifesteal");
            if (lifestealLevel > 0) {
                double healAmount = event.getDamage() * 0.05 * lifestealLevel;
                double newHealth = Math.min(
                        player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue(),
                        player.getHealth() + healAmount);
                player.setHealth(newHealth);
            }
            // Critical master - additional chance
            int critLevel = plugin.getManager().getSkillLevel(player, "critical_master");
            if (critLevel > 0 && random.nextDouble() < 0.05 * critLevel) {
                event.setDamage(event.getDamage() * (1.0 + 0.1 * critLevel));
            }
        }
        // Dodge
        if (event.getEntity() instanceof Player player) {
            int dodgeLevel = plugin.getManager().getSkillLevel(player, "dodge");
            if (dodgeLevel > 0 && random.nextDouble() < 0.05 * dodgeLevel) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.YELLOW + "회피!");
                return;
            }
            // Counter attack
            int counterLevel = plugin.getManager().getSkillLevel(player, "counter_attack");
            if (counterLevel > 0 && event.getDamager() instanceof org.bukkit.entity.LivingEntity attacker) {
                if (random.nextDouble() < 0.2 * counterLevel) {
                    attacker.damage(event.getDamage() * 0.3 * counterLevel, player);
                    player.sendMessage(ChatColor.RED + "반격!");
                }
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        int level = plugin.getManager().getSkillLevel(player, "fall_protection");
        if (level > 0) {
            event.setDamage(event.getDamage() * Math.max(0, 1.0 - 0.15 * level));
        }
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        int level = plugin.getManager().getSkillLevel(player, "hunger_reduce");
        if (level > 0 && event.getFoodLevel() < player.getFoodLevel()) {
            // Chance to not consume food
            if (random.nextDouble() < 0.1 * level) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        int autoSmeltLevel = plugin.getManager().getSkillLevel(player, "auto_smelt");
        int doubleDropLevel = plugin.getManager().getSkillLevel(player, "double_drop");
        boolean doubleDrop = doubleDropLevel > 0 && random.nextDouble() < 0.1 * doubleDropLevel;

        // Auto smelt takes priority; suppress default drops and handle manually
        if (autoSmeltLevel > 0) {
            Material smelted = getSmeltedResult(event.getBlock().getType());
            if (smelted != null) {
                event.setDropItems(false);
                int amount = doubleDrop ? 2 : 1;
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(smelted, amount));
                return;
            }
        }

        // Double drop (no auto smelt applicable)
        if (doubleDrop) {
            for (ItemStack drop : event.getBlock().getDrops(player.getInventory().getItemInMainHand())) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop.clone());
            }
        }
    }

    private Material getSmeltedResult(Material raw) {
        return switch (raw) {
            case IRON_ORE, DEEPSLATE_IRON_ORE -> Material.IRON_INGOT;
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, NETHER_GOLD_ORE -> Material.GOLD_INGOT;
            case COPPER_ORE, DEEPSLATE_COPPER_ORE -> Material.COPPER_INGOT;
            case ANCIENT_DEBRIS -> Material.NETHERITE_SCRAP;
            default -> null;
        };
    }
}
