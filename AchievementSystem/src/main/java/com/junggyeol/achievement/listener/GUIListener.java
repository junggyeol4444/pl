package com.junggyeol.achievement.listener;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.gui.AchievementGUI;
import com.junggyeol.achievement.model.Achievement.Category;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * GUI 클릭 이벤트 리스너
 */
public class GUIListener implements Listener {

    private final AchievementPlugin plugin;
    private static final String MAIN_TITLE_PLAIN = stripColor(AchievementGUI.MAIN_GUI_TITLE);
    private static final String CATEGORY_TITLE_PLAIN = stripColor(AchievementGUI.CATEGORY_GUI_PREFIX);

    public GUIListener(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getCurrentItem() == null) return;

        String rawTitle = PlainTextComponentSerializer.plainText().serialize(event.getView().title());

        // 메인 GUI
        if (rawTitle.contains(MAIN_TITLE_PLAIN)) {
            event.setCancelled(true);
            handleMainGUIClick(player, event.getSlot());
            return;
        }

        // 카테고리 GUI
        if (rawTitle.startsWith(CATEGORY_TITLE_PLAIN)) {
            event.setCancelled(true);
            handleCategoryGUIClick(player, event.getSlot());
        }
    }

    private void handleMainGUIClick(Player player, int slot) {
        // 닫기 버튼
        if (slot == 49) {
            player.closeInventory();
            return;
        }

        // 카테고리 버튼 슬롯 매핑
        Category[] categories = Category.values();
        int[] categorySlots = {20, 22, 24, 30, 32};

        for (int i = 0; i < categorySlots.length && i < categories.length; i++) {
            if (slot == categorySlots[i]) {
                plugin.getAchievementGUI().openCategoryGUI(player, categories[i]);
                return;
            }
        }
    }

    private void handleCategoryGUIClick(Player player, int slot) {
        // 닫기 버튼
        if (slot == 49) {
            player.closeInventory();
            return;
        }
        // 뒤로가기 버튼
        if (slot == 45) {
            plugin.getAchievementGUI().openMainGUI(player);
        }
    }

    private static String stripColor(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }
}
