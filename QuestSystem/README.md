# QuestSystem 플러그인

일일/주간/특별 퀘스트 시스템입니다.

## 명령어
- `/퀘스트` - 퀘스트 목록 GUI 열기

## 퀘스트 종류
- **일일 퀘스트** (매일 자정 갱신)
- **주간 퀘스트** (매주 월요일 갱신)
- **특별 퀘스트** (상시)

## config.yml 설정
```yaml
quests:
  my_quest:
    name: "퀘스트 이름"
    description: "설명"
    period: DAILY  # DAILY/WEEKLY/SPECIAL
    type: KILL_MOB  # KILL_MOB/MINE_BLOCK/CRAFT_ITEM/TRAVEL_DISTANCE/VISIT_BIOME/PLACE_BLOCK/FISH/BREED_ANIMAL/ENCHANT_ITEM/TRADE
    target: ZOMBIE  # 대상 (몹 이름, 블록 이름 등)
    required_count: 30
    reward:
      xp: 200
      items:
        - "DIAMOND:1"
      skill_points: 1
```
