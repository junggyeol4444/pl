package com.junggyeol.achievement;

import com.junggyeol.achievement.api.AchievementAPI;
import com.junggyeol.achievement.command.AchievementCommand;
import com.junggyeol.achievement.gui.AchievementGUI;
import com.junggyeol.achievement.listener.BlockListener;
import com.junggyeol.achievement.listener.CombatListener;
import com.junggyeol.achievement.listener.GUIListener;
import com.junggyeol.achievement.listener.PlayerListener;
import com.junggyeol.achievement.manager.AchievementManager;
import com.junggyeol.achievement.storage.DataStorage;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

/**
 * AchievementSystem 플러그인 메인 클래스
 * Paper 1.20.4+ 전용 업적 시스템
 */
public class AchievementPlugin extends JavaPlugin {

    private AchievementManager achievementManager;
    private DataStorage dataStorage;
    private AchievementGUI achievementGUI;
    private AchievementAPI achievementAPI;
    private BukkitTask autoSaveTask;

    @Override
    public void onEnable() {
        // 기본 설정 파일 저장
        saveDefaultConfig();

        // 모듈 초기화
        dataStorage = new DataStorage(this);
        achievementManager = new AchievementManager(this);
        achievementGUI = new AchievementGUI(this);
        achievementAPI = new AchievementAPI(this);

        // 업적 로드
        achievementManager.loadAchievements();

        // 이벤트 리스너 등록
        registerListeners();

        // 명령어 등록
        registerCommands();

        // 자동 저장 태스크 시작
        startAutoSave();

        getLogger().info("AchievementSystem이 활성화되었습니다!");
        getLogger().info("업적 시스템 v" + getDescription().getVersion() + " by junggyeol");
    }

    @Override
    public void onDisable() {
        // 자동 저장 태스크 취소
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        // 모든 플레이어 데이터 저장
        if (achievementManager != null) {
            achievementManager.saveAllPlayers();
        }

        // API 인스턴스 무효화
        AchievementAPI.invalidate();

        getLogger().info("AchievementSystem이 비활성화되었습니다.");
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
    }

    private void registerCommands() {
        AchievementCommand commandHandler = new AchievementCommand(this);

        String[] commandNames = {"achievement", "업적", "ach"};
        for (String cmdName : commandNames) {
            PluginCommand cmd = getCommand(cmdName);
            if (cmd != null) {
                cmd.setExecutor(commandHandler);
                cmd.setTabCompleter(commandHandler);
            } else {
                getLogger().log(Level.WARNING, "명령어 등록 실패: " + cmdName);
            }
        }
    }

    private void startAutoSave() {
        int interval = getConfig().getInt("settings.auto-save-interval", 6000);
        autoSaveTask = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            achievementManager.saveAllPlayers();
        }, interval, interval);
    }

    // ============================================================
    // Getters
    // ============================================================

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public AchievementGUI getAchievementGUI() {
        return achievementGUI;
    }

    public AchievementAPI getAchievementAPI() {
        return achievementAPI;
    }
}
