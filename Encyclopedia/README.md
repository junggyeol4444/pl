# Encyclopedia 플러그인

모든 플러그인의 컨텐츠를 통합하는 도감 시스템입니다.

## 명령어
- `/도감` - 도감 메뉴 GUI 열기

## 카테고리
- ITEM: 아이템 도감
- MOB: 몹 도감
- BIOME: 바이옴 도감
- ACHIEVEMENT: 업적 도감
- QUEST: 퀘스트 도감
- SKILL: 스킬 도감
- DIMENSION: 차원 도감
- SUPPLY: 보급 상자 도감

## 공개 API (다른 플러그인에서 사용)
```java
// 항목 등록 (플러그인 시작 시)
EncyclopediaAPI.registerEntry("ITEM", "item_id", "아이템 이름", "설명", Material.DIAMOND);

// 플레이어가 항목 발견
EncyclopediaAPI.discoverEntry(player, "ITEM", "item_id");

// 발견 여부 확인
boolean found = EncyclopediaAPI.isDiscovered(player, "ITEM", "item_id");

// 카테고리 수집률 (0~100)
double rate = EncyclopediaAPI.getDiscoveryRate(player, "ITEM");
```

## 마일스톤 보상
- 25% 달성: 경험치 500
- 50% 달성: 경험치 1000
- 75% 달성: 경험치 2000
- 100% 달성: 경험치 5000
