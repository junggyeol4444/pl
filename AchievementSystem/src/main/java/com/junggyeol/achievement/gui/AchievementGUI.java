package com.junggyeol.achievement.gui;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement;
import com.junggyeol.achievement.model.Achievement.Category;
import com.junggyeol.achievement.model.PlayerAchievement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * 업적 인벤토리 GUI 클래스
 */
public class AchievementGUI {

    private final AchievementPlugin plugin;

    // GUI 제목 식별자
    public static final String MAIN_GUI_TITLE = "§l🏆 업적 시스템";
    public static final String CATEGORY_GUI_PREFIX = "§l업적 - ";

    public AchievementGUI(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 메인 업적 GUI를 엽니다 (카테고리 선택 화면)
     */
    public void openMainGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(MAIN_GUI_TITLE));

        fillBorder(inv, Material.BLACK_STAINED_GLASS_PANE);

        // 카테고리 버튼
        int[] categorySlots = {20, 22, 24, 30, 32};
        Category[] categories = Category.values();

        for (int i = 0; i < categories.length && i < categorySlots.length; i++) {
            Category cat = categories[i];
            List<Achievement> catAchievements = plugin.getAchievementManager().getAchievementsByCategory(cat);
            PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

            long completed = catAchievements.stream()
                    .filter(a -> data.hasCompleted(a.getId()))
                    .count();

            ItemStack item = createCategoryItem(cat, (int) completed, catAchievements.size());
            inv.setItem(categorySlots[i], item);
        }

        // 통계 정보 아이템
        ItemStack statsItem = createStatsItem(player);
        inv.setItem(4, statsItem);

        // 닫기 버튼
        inv.setItem(49, createCloseItem());

        player.openInventory(inv);
    }

    /**
     * 특정 카테고리의 업적 GUI를 엽니다
     */
    public void openCategoryGUI(Player player, Category category) {
        String title = CATEGORY_GUI_PREFIX + category.getEmoji() + " " + category.getDisplayName();
        Inventory inv = Bukkit.createInventory(null, 54, Component.text(title));

        fillBorder(inv, Material.BLACK_STAINED_GLASS_PANE);

        List<Achievement> achievements = plugin.getAchievementManager().getAchievementsByCategory(category);
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        int slot = 10;
        int itemsPerRow = 7;
        int currentInRow = 0;

        for (Achievement achievement : achievements) {
            if (slot >= 44) break; // GUI 범위 초과 방지

            if (achievement.isHidden() && !data.hasCompleted(achievement.getId())) {
                // 숨겨진 업적은 미달성 시 ???로 표시
                inv.setItem(slot, createHiddenAchievementItem());
            } else {
                boolean completed = data.hasCompleted(achievement.getId());
                inv.setItem(slot, createAchievementItem(achievement, completed, data));
            }

            slot++;
            currentInRow++;

            if (currentInRow >= itemsPerRow) {
                slot += 2; // 다음 줄의 시작 위치로
                currentInRow = 0;
            }
        }

        // 뒤로가기 버튼
        inv.setItem(45, createBackItem());
        inv.setItem(49, createCloseItem());

        player.openInventory(inv);
    }

    private ItemStack createCategoryItem(Category category, int completed, int total) {
        Material material = switch (category) {
            case MINING -> Material.DIAMOND_PICKAXE;
            case COMBAT -> Material.DIAMOND_SWORD;
            case EXPLORATION -> Material.COMPASS;
            case BUILDING -> Material.BRICKS;
            case SOCIAL -> Material.BOOK;
        };

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(category.getEmoji() + " " + category.getDisplayName())
                .color(NamedTextColor.YELLOW)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("진행도: ")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(completed + "/" + total).color(NamedTextColor.GREEN)));
        lore.add(Component.empty());
        lore.add(Component.text("클릭하여 확인").color(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));

        meta.lore(lore);

        if (completed == total && total > 0) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createAchievementItem(Achievement achievement, boolean completed, PlayerAchievement data) {
        ItemStack item = new ItemStack(achievement.getIcon());
        ItemMeta meta = item.getItemMeta();

        NamedTextColor nameColor = completed ? NamedTextColor.GREEN : NamedTextColor.RED;
        String prefix = completed ? "✔ " : "✘ ";

        meta.displayName(Component.text(prefix + achievement.getName())
                .color(nameColor)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text(achievement.getDescription())
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.empty());

        // 카테고리
        lore.add(Component.text("카테고리: ")
                .color(NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text(achievement.getCategory().getDisplayName())
                        .color(NamedTextColor.YELLOW)));

        // 달성 상태
        if (completed) {
            lore.add(Component.text("✔ 달성 완료!")
                    .color(NamedTextColor.GREEN)
                    .decorate(TextDecoration.BOLD)
                    .decoration(TextDecoration.ITALIC, false));
        } else {
            // 진행도 표시 (조건 유형에 따라)
            long current = getProgressValue(achievement, data);
            long required = achievement.getConditionValue();
            if (required > 1) {
                String bar = buildProgressBar(current, required, 20);
                lore.add(Component.text("진행도: " + current + " / " + required)
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false));
                lore.add(Component.text(bar)
                        .color(NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false));
            }
        }

        lore.add(Component.empty());

        // 보상 정보
        if (achievement.getRewardExp() > 0) {
            lore.add(Component.text("보상 경험치: " + achievement.getRewardExp())
                    .color(NamedTextColor.AQUA)
                    .decoration(TextDecoration.ITALIC, false));
        }
        if (achievement.getRewardTitle() != null) {
            lore.add(Component.text("보상 칭호: [" + achievement.getRewardTitle() + "]")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);

        if (completed) {
            meta.addEnchant(Enchantment.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        item.setItemMeta(meta);
        return item;
    }

    private long getProgressValue(Achievement achievement, PlayerAchievement data) {
        return switch (achievement.getConditionType()) {
            case MINE_BLOCK -> achievement.getConditionExtra() != null
                    ? data.getBlocksMined(achievement.getConditionExtra())
                    : 0;
            case KILL_MOB -> achievement.getConditionExtra() != null
                    ? data.getMobKills(achievement.getConditionExtra())
                    : 0;
            case KILL_PLAYER -> data.getPlayerKills();
            case WALK_DISTANCE -> data.getTotalWalkDistance();
            case PLACE_BLOCK -> data.getTotalBlocksPlaced();
            case DEATH_COUNT -> data.getDeathCount();
            case CONSECUTIVE_DAYS -> data.getConsecutiveDays();
            default -> 0;
        };
    }

    private String buildProgressBar(long current, long max, int length) {
        int filled = (int) Math.min((double) current / max * length, length);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]");
        return bar.toString();
    }

    private ItemStack createHiddenAchievementItem() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("??? 숨겨진 업적")
                .color(NamedTextColor.DARK_GRAY)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("이 업적은 달성해야 공개됩니다.")
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createStatsItem(Player player) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());
        long total = plugin.getAchievementManager().getAllAchievements().size();
        long completed = data.getCompletedAchievements().size();

        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("📊 내 업적 현황")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Component.text("달성한 업적: " + completed + " / " + total)
                .color(NamedTextColor.GREEN)
                .decoration(TextDecoration.ITALIC, false));
        lore.add(Component.text("연속 접속: " + data.getConsecutiveDays() + "일")
                .color(NamedTextColor.AQUA)
                .decoration(TextDecoration.ITALIC, false));

        String activeTitle = data.getActiveTitle();
        if (activeTitle != null) {
            lore.add(Component.text("현재 칭호: [" + activeTitle + "]")
                    .color(NamedTextColor.GOLD)
                    .decoration(TextDecoration.ITALIC, false));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createBackItem() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("◀ 뒤로가기")
                .color(NamedTextColor.YELLOW)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCloseItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("✖ 닫기")
                .color(NamedTextColor.RED)
                .decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private void fillBorder(Inventory inv, Material material) {
        ItemStack border = new ItemStack(material);
        ItemMeta meta = border.getItemMeta();
        meta.displayName(Component.text(" ").decoration(TextDecoration.ITALIC, false));
        border.setItemMeta(meta);

        for (int i = 0; i < 9; i++) inv.setItem(i, border.clone());
        for (int i = 45; i < 54; i++) inv.setItem(i, border.clone());
        for (int i = 1; i < 5; i++) {
            inv.setItem(i * 9, border.clone());
            inv.setItem(i * 9 + 8, border.clone());
        }
    }
}
