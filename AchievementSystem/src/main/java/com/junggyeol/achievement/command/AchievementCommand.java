package com.junggyeol.achievement.command;

import com.junggyeol.achievement.AchievementPlugin;
import com.junggyeol.achievement.model.Achievement;
import com.junggyeol.achievement.model.PlayerAchievement;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * 업적 명령어 핸들러
 * /achievement, /업적, /ach 명령어를 처리합니다.
 */
public class AchievementCommand implements CommandExecutor, TabCompleter {

    private final AchievementPlugin plugin;

    public AchievementCommand(AchievementPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        if (!player.hasPermission("achievement.use")) {
            player.sendMessage(Component.text("권한이 없습니다.").color(NamedTextColor.RED));
            return true;
        }

        // 인수 없음: GUI 열기
        if (args.length == 0) {
            plugin.getAchievementGUI().openMainGUI(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "목록", "list" -> showList(player);
            case "정보", "info" -> {
                if (args.length < 2) {
                    player.sendMessage(Component.text("사용법: /" + label + " 정보 [업적ID]").color(NamedTextColor.RED));
                } else {
                    showInfo(player, args[1]);
                }
            }
            case "관리자", "admin" -> {
                if (!player.hasPermission("achievement.admin")) {
                    player.sendMessage(Component.text("관리자 권한이 필요합니다.").color(NamedTextColor.RED));
                    return true;
                }
                handleAdmin(player, args);
            }
            case "칭호", "title" -> handleTitle(player, args);
            default -> {
                player.sendMessage(Component.text("알 수 없는 명령어입니다. /업적 또는 /achievement를 사용하세요.").color(NamedTextColor.RED));
            }
        }

        return true;
    }

    private void showList(Player player) {
        Collection<Achievement> achievements = plugin.getAchievementManager().getAllAchievements();
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        player.sendMessage(Component.text("━━━ 업적 목록 (" + data.getCompletedAchievements().size() + "/" + achievements.size() + ") ━━━")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD));

        for (Achievement achievement : achievements) {
            if (achievement.isHidden() && !data.hasCompleted(achievement.getId())) continue;

            boolean completed = data.hasCompleted(achievement.getId());
            NamedTextColor color = completed ? NamedTextColor.GREEN : NamedTextColor.GRAY;
            String prefix = completed ? "✔" : "✘";

            player.sendMessage(Component.text(prefix + " ")
                    .color(color)
                    .append(Component.text("[" + achievement.getCategory().getDisplayName() + "] ")
                            .color(NamedTextColor.YELLOW))
                    .append(Component.text(achievement.getName())
                            .color(color)));
        }
    }

    private void showInfo(Player player, String achievementId) {
        Achievement achievement = plugin.getAchievementManager().getAchievement(achievementId);
        if (achievement == null) {
            player.sendMessage(Component.text("업적을 찾을 수 없습니다: " + achievementId).color(NamedTextColor.RED));
            return;
        }

        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());
        boolean completed = data.hasCompleted(achievementId);

        player.sendMessage(Component.text("━━━ 업적 정보 ━━━").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        player.sendMessage(Component.text("이름: ").color(NamedTextColor.YELLOW)
                .append(Component.text(achievement.getName()).color(NamedTextColor.WHITE)));
        player.sendMessage(Component.text("설명: ").color(NamedTextColor.YELLOW)
                .append(Component.text(achievement.getDescription()).color(NamedTextColor.GRAY)));
        player.sendMessage(Component.text("카테고리: ").color(NamedTextColor.YELLOW)
                .append(Component.text(achievement.getCategory().getDisplayName()).color(NamedTextColor.AQUA)));
        player.sendMessage(Component.text("달성 여부: ").color(NamedTextColor.YELLOW)
                .append(Component.text(completed ? "달성 완료" : "미달성").color(completed ? NamedTextColor.GREEN : NamedTextColor.RED)));

        if (achievement.getRewardExp() > 0) {
            player.sendMessage(Component.text("보상 경험치: ").color(NamedTextColor.YELLOW)
                    .append(Component.text(achievement.getRewardExp() + " XP").color(NamedTextColor.GREEN)));
        }
        if (achievement.getRewardTitle() != null) {
            player.sendMessage(Component.text("보상 칭호: ").color(NamedTextColor.YELLOW)
                    .append(Component.text("[" + achievement.getRewardTitle() + "]").color(NamedTextColor.GOLD)));
        }
    }

    private void handleAdmin(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("사용법: /업적 관리자 <추가|삭제|리로드|부여>").color(NamedTextColor.RED));
            return;
        }

        switch (args[1].toLowerCase()) {
            case "리로드", "reload" -> {
                plugin.reloadConfig();
                plugin.getAchievementManager().loadAchievements();
                player.sendMessage(Component.text("업적 설정을 다시 로드했습니다.").color(NamedTextColor.GREEN));
            }
            case "부여", "grant" -> {
                if (args.length < 4) {
                    player.sendMessage(Component.text("사용법: /업적 관리자 부여 [플레이어] [업적ID]").color(NamedTextColor.RED));
                    return;
                }
                Player target = plugin.getServer().getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(Component.text("플레이어를 찾을 수 없습니다: " + args[2]).color(NamedTextColor.RED));
                    return;
                }
                boolean success = plugin.getAchievementManager().grantAchievementById(target, args[3]);
                if (success) {
                    player.sendMessage(Component.text(target.getName() + "에게 업적 '" + args[3] + "'을 부여했습니다.").color(NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("업적을 찾을 수 없습니다: " + args[3]).color(NamedTextColor.RED));
                }
            }
            default -> player.sendMessage(Component.text("알 수 없는 관리자 명령어입니다.").color(NamedTextColor.RED));
        }
    }

    private void handleTitle(Player player, String[] args) {
        PlayerAchievement data = plugin.getAchievementManager().getPlayerData(player.getUniqueId());

        if (args.length < 2) {
            // 칭호 목록 표시
            if (data.getOwnedTitles().isEmpty()) {
                player.sendMessage(Component.text("보유한 칭호가 없습니다.").color(NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("보유 칭호 목록:").color(NamedTextColor.GOLD));
                for (String title : data.getOwnedTitles()) {
                    boolean active = title.equals(data.getActiveTitle());
                    player.sendMessage(Component.text("  [" + title + "]")
                            .color(active ? NamedTextColor.GOLD : NamedTextColor.GRAY)
                            .append(active ? Component.text(" ✔ (장착중)").color(NamedTextColor.GREEN) : Component.empty()));
                }
                player.sendMessage(Component.text("/업적 칭호 장착 [칭호명]으로 칭호를 바꿀 수 있습니다.").color(NamedTextColor.AQUA));
            }
            return;
        }

        if (args[1].equalsIgnoreCase("장착") || args[1].equalsIgnoreCase("equip")) {
            if (args.length < 3) {
                player.sendMessage(Component.text("사용법: /업적 칭호 장착 [칭호명]").color(NamedTextColor.RED));
                return;
            }
            String titleToEquip = args[2];
            if (!data.getOwnedTitles().contains(titleToEquip)) {
                player.sendMessage(Component.text("보유하지 않은 칭호입니다.").color(NamedTextColor.RED));
                return;
            }
            data.setActiveTitle(titleToEquip);
            player.sendMessage(Component.text("칭호를 [" + titleToEquip + "]으로 변경했습니다.").color(NamedTextColor.GREEN));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("목록", "정보", "칭호", "관리자");
            for (String sub : subCommands) {
                if (sub.startsWith(args[0])) completions.add(sub);
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("정보") || args[0].equalsIgnoreCase("info")) {
                for (Achievement a : plugin.getAchievementManager().getAllAchievements()) {
                    if (a.getId().startsWith(args[1])) completions.add(a.getId());
                }
            } else if (args[0].equalsIgnoreCase("관리자") || args[0].equalsIgnoreCase("admin")) {
                List<String> adminSubs = Arrays.asList("리로드", "부여");
                for (String sub : adminSubs) {
                    if (sub.startsWith(args[1])) completions.add(sub);
                }
            } else if (args[0].equalsIgnoreCase("칭호") || args[0].equalsIgnoreCase("title")) {
                completions.add("장착");
            }
        } else if (args.length == 3 && (args[0].equalsIgnoreCase("관리자") || args[0].equalsIgnoreCase("admin"))
                && (args[1].equalsIgnoreCase("부여") || args[1].equalsIgnoreCase("grant"))) {
            // 온라인 플레이어 목록
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(p.getName());
                }
            }
        }

        return completions;
    }
}
