# AchievementSystem 플러그인

마인크래프트 Paper 서버용 업적 시스템 플러그인입니다.

## 명령어
- `/업적` - 업적 목록 GUI 열기
- `/업적 랭킹` - 업적 달성 수 랭킹 확인
- `/칭호` - 보유 칭호 목록 확인
- `/칭호 <칭호명>` - 칭호 장착

## 공개 API
```java
AchievementAPI.trigger(player, "achievementId");
AchievementAPI.isCompleted(player, "achievementId");
```

## config.yml 설정
- `prefix`: 채팅 접두사
- `achievements.<id>.name`: 업적 이름
- `achievements.<id>.description`: 설명
- `achievements.<id>.category`: MINING/COMBAT/EXPLORATION/BUILDING/SOCIAL/OTHER
- `achievements.<id>.reward.xp`: 경험치 보상
- `achievements.<id>.reward.items`: 아이템 보상 목록 (예: "DIAMOND:5")
- `achievements.<id>.reward.title`: 칭호 보상
- `achievements.<id>.trigger`: FIRST_JOIN/MINE_BLOCK/KILL_MOB/VISIT_BIOME/HOLD_ITEMS/MANUAL
- `achievements.<id>.required_count`: 달성 필요 수
