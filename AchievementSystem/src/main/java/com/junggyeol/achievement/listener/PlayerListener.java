package com.junggyeol.achievement.listener;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement.ConditionType;
import com.junggyeol.achievement.model.PlayerAchievement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 플레이어 관련 이벤트 리스너 (접속, 이동, 세계 변경, 사망, 채팅 칭호)
 */
public class PlayerListener implements Listener {

    private final AchievementPlugin plugin;

    public PlayerListener(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAchievementManager().loadPlayer(player.getUniqueId());

        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        // 첫 접속 처리
        if (!data.isFirstJoin()) {
            data.setFirstJoin(true);
            plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.FIRST_JOIN);
        }

        // 연속 접속 처리
        updateConsecutiveDays(player, data);

        // 연속 접속 업적 체크
        plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.CONSECUTIVE_DAYS);
    }

    private void updateConsecutiveDays(Player player, PlayerAchievement data) {
        long today = LocalDate.now(ZoneId.systemDefault()).toEpochDay();
        long lastDay = data.getLastLoginDay();

        if (lastDay == -1) {
            // 첫 접속
            data.setLastLoginDay(today);
            data.setConsecutiveDays(1);
            data.setTotalDays(1);
        } else if (today == lastDay) {
            // 오늘 이미 접속함, 무시
        } else if (today == lastDay + 1) {
            // 연속 접속
            data.setLastLoginDay(today);
            data.setConsecutiveDays(data.getConsecutiveDays() + 1);
            data.setTotalDays(data.getTotalDays() + 1);
        } else {
            // 연속 접속 끊김
            data.setLastLoginDay(today);
            data.setConsecutiveDays(1);
            data.setTotalDays(data.getTotalDays() + 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getAchievementManager().unloadPlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // 위치가 실제로 변한 경우에만 (헤드 회전은 제외)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        // 이동 거리 계산 (블록 단위 정수 거리)
        long dx = event.getTo().getBlockX() - event.getFrom().getBlockX();
        long dy = event.getTo().getBlockY() - event.getFrom().getBlockY();
        long dz = event.getTo().getBlockZ() - event.getFrom().getBlockZ();
        long distSq = dx * dx + dy * dy + dz * dz;

        if (distSq >= 1) {
            data.addWalkDistance(1);
            plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.WALK_DISTANCE);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        String worldEnvironment = player.getWorld().getEnvironment().name();
        data.addVisitedWorld(worldEnvironment);

        plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.ENTER_WORLD);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());
        data.incrementDeathCount();
        plugin.getAchievementManager().checkAchievementsByType(player, ConditionType.DEATH_COUNT);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("settings.show-prefix-in-chat", true)) return;

        Player player = event.getPlayer();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        String activeTitle = data.getActiveTitle();
        if (activeTitle == null || activeTitle.isEmpty()) return;

        // 칭호를 채팅 포맷에 추가
        String currentFormat = event.getFormat();
        event.setFormat("§6[" + activeTitle + "]§r " + currentFormat);
    }
}
