package com.junggyeol.supplybox;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SupplyBoxManager {
    private final SupplyBoxPlugin plugin;
    private final Map<Location, SupplyBox> activeBoxes = new HashMap<>();
    private long nextDropTime = 0L;
    private final Random random = new Random();

    public SupplyBoxManager(SupplyBoxPlugin plugin) { this.plugin = plugin; }

    public void scheduleNextDrop() {
        long intervalMillis = plugin.getConfig().getLong("drop-interval-minutes", 60) * 60 * 1000L;
        nextDropTime = System.currentTimeMillis() + intervalMillis;
    }

    public long getNextDropTime() { return nextDropTime; }

    public void spawnSupplyBox() {
        World world = Bukkit.getWorlds().get(0);
        int range = plugin.getConfig().getInt("drop-range", 2000);
        int x = random.nextInt(range * 2) - range;
        int z = random.nextInt(range * 2) - range;
        int y = world.getHighestBlockYAt(x, z) + 1;

        Location loc = new Location(world, x, y, z);
        SupplyBoxGrade grade = rollGrade();

        // Place chest
        Block block = loc.getBlock();
        block.setType(Material.CHEST);
        if (block.getState() instanceof Chest chest) {
            fillChest(chest.getInventory(), grade);
        }

        SupplyBox supplyBox = new SupplyBox(loc, grade, System.currentTimeMillis());
        activeBoxes.put(loc, supplyBox);

        // Announce
        String gradeDisplay = grade.getDisplayName();
        String msg = grade.getColor() + "[보급상자] " + gradeDisplay + ChatColor.WHITE + " 보급 상자가 " +
                ChatColor.YELLOW + "(" + x + ", " + z + ")" + ChatColor.WHITE + " 근처에 떨어졌습니다!";
        Bukkit.broadcastMessage(msg);
        Bukkit.broadcastMessage(ChatColor.GRAY + "정확한 좌표는 직접 탐색하세요!");

        // Particle beam effect
        spawnParticleBeam(loc, world);

        scheduleNextDrop();
        scheduleDespawn(loc);
    }

    private SupplyBoxGrade rollGrade() {
        int common = plugin.getConfig().getInt("grade-chances.COMMON", 70);
        int rare = plugin.getConfig().getInt("grade-chances.RARE", 25);
        int roll = random.nextInt(100);
        if (roll < common) return SupplyBoxGrade.COMMON;
        if (roll < common + rare) return SupplyBoxGrade.RARE;
        return SupplyBoxGrade.LEGENDARY;
    }

    private void fillChest(Inventory inventory, SupplyBoxGrade grade) {
        List<String> lootTable = plugin.getConfig().getStringList("loot-tables." + grade.name());
        List<String> shuffled = new ArrayList<>(lootTable);
        Collections.shuffle(shuffled, random);
        int count = Math.min(grade == SupplyBoxGrade.LEGENDARY ? 5 : (grade == SupplyBoxGrade.RARE ? 4 : 3), shuffled.size());
        for (int i = 0; i < count; i++) {
            String[] parts = shuffled.get(i).split(":");
            try {
                Material mat = Material.valueOf(parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                inventory.addItem(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }
    }

    private void spawnParticleBeam(Location loc, World world) {
        Bukkit.getScheduler().runTaskTimer(plugin, task -> {
            if (!activeBoxes.containsKey(loc)) { task.cancel(); return; }
            for (double y2 = loc.getY(); y2 < loc.getY() + 30; y2 += 0.5) {
                Location particleLoc = new Location(world, loc.getX(), y2, loc.getZ());
                world.spawnParticle(Particle.END_ROD, particleLoc, 2, 0.1, 0, 0.1, 0);
            }
        }, 0L, 20L);
    }

    private void scheduleDespawn(Location loc) {
        long despawnTicks = plugin.getConfig().getLong("auto-despawn-minutes", 10) * 60 * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            SupplyBox box = activeBoxes.remove(loc);
            if (box != null && !box.isOpened()) {
                Block block = loc.getBlock();
                if (block.getType() == Material.CHEST) {
                    block.setType(Material.AIR);
                }
                Bukkit.broadcastMessage(ChatColor.GRAY + "[보급상자] 미개봉 보급 상자가 소멸했습니다.");
            }
        }, despawnTicks);
    }

    public void handleOpen(Player player, Location loc) {
        SupplyBox box = activeBoxes.get(loc);
        if (box == null) return;
        if (box.isOpened()) return;
        box.setOpened(true);
        Bukkit.broadcastMessage(box.getGrade().getColor() + player.getName() + ChatColor.WHITE + " 님이 " +
                box.getGrade().getDisplayName() + ChatColor.WHITE + " 보급 상자를 획득했습니다!");
        notifyIntegrations(player, box.getGrade());
    }

    private void notifyIntegrations(Player player, SupplyBoxGrade grade) {
        try {
            Class<?> ac = Class.forName("com.junggyeol.achievement.AchievementAPI");
            ac.getMethod("trigger", Player.class, String.class).invoke(null, player, "supply_box_" + grade.name().toLowerCase());
        } catch (Exception ignored) {}
        try {
            Class<?> ec = Class.forName("com.junggyeol.encyclopedia.EncyclopediaAPI");
            ec.getMethod("discoverEntry", Player.class, String.class, String.class)
                    .invoke(null, player, "SUPPLY", grade.name().toLowerCase());
        } catch (Exception ignored) {}
        try {
            Class<?> stc = Class.forName("com.junggyeol.stats.StatAPI");
            stc.getMethod("addStat", Player.class, String.class, int.class)
                    .invoke(null, player, "supply_boxes_opened", 1);
        } catch (Exception ignored) {}
    }

    public Map<Location, SupplyBox> getActiveBoxes() { return activeBoxes; }
}
