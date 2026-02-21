# pl — 마인크래프트 시청자 참여 야생 서버 커스텀 플러그인

Paper API 1.20.4+ 기반의 8개 독립 플러그인 모음입니다.

## 플러그인 목록

| 폴더 | 이름 | 설명 | 주요 명령어 |
|------|------|------|------------|
| `AchievementSystem/` | 🏆 업적 시스템 | 커스텀 업적 등록/추적/달성/보상 | `/업적`, `/칭호` |
| `CustomCrafting/` | 🔨 커스텀 제작대 | 에메랄드 블록 우클릭으로 특수 아이템 제작 | `/제작대` |
| `Encyclopedia/` | 📖 도감 | 모든 컨텐츠 통합 도감 시스템 | `/도감` |
| `QuestSystem/` | 📜 퀘스트 | 일일/주간/특별 퀘스트 | `/퀘스트` |
| `SkillTree/` | ⚡ 스킬 트리 | 채굴/전투/생존 스킬 트리 | `/스킬` |
| `SupplyBox/` | 📦 보급 상자 | 랜덤 좌표에 보급 상자 낙하 | `/보급` |
| `SecretDimension/` | 🌀 비밀 차원문 | 조건 충족 시 입장하는 비밀 차원 | `/차원` |
| `StatSystem/` | 📊 스탯 | 플레이어 활동 통계 추적 및 랭킹 | `/스탯`, `/랭킹` |

## 기술 스택
- **언어**: Java 17+
- **서버 API**: Paper API 1.20.4+
- **빌드**: Maven (각 플러그인 독립 `pom.xml`)
- **데이터**: YAML 파일 기반 (플레이어별 UUID.yml)

## 빌드 방법

각 플러그인 폴더에서:
```bash
mvn clean package
# target/ 폴더에 jar 파일 생성
```

## 플러그인 간 연동

각 플러그인은 **단독으로도 동작**하며, 다른 플러그인이 설치되어 있을 때 자동으로 연동됩니다 (soft-depend 방식).

```
업적(AchievementSystem) ← 모든 플러그인에서 업적 트리거
도감(Encyclopedia) ← 모든 플러그인에서 도감 등록
스탯(StatSystem) ← 모든 플러그인에서 통계 전송

퀘스트 완료 → 업적 트리거 + 도감 등록 + 스킬 포인트
스킬 해금 → 업적 트리거 + 도감 등록
보급 상자 획득 → 업적 트리거 + 도감 등록 + 스탯 기록
차원 클리어 → 업적 트리거 + 도감 등록 + 스탯 기록
커스텀 아이템 제작 → 도감 등록 + 업적 트리거 + 스탯 기록
```

## 공개 API 요약

```java
// 업적
AchievementAPI.trigger(player, "achievementId");
AchievementAPI.isCompleted(player, "achievementId");

// 도감
EncyclopediaAPI.registerEntry("ITEM", "id", "이름", "설명", Material.DIAMOND);
EncyclopediaAPI.discoverEntry(player, "ITEM", "id");

// 스킬
SkillTreeAPI.getSkillLevel(player, "double_drop");
SkillTreeAPI.addSkillPoints(player, 5);

// 스탯
StatAPI.getStat(player, "mobs_killed");
StatAPI.addStat(player, "custom_stat", 1);
StatAPI.getTopPlayers("mobs_killed", 10);

// 커스텀 아이템
CustomCraftingAPI.getCustomItem("teleport_scroll");
CustomCraftingAPI.giveCustomItem(player, "teleport_scroll", 1);
```