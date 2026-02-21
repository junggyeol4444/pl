# CustomCrafting 플러그인

바닐라 작업대와 별개로 동작하는 커스텀 제작대 플러그인입니다.

## 사용 방법
- 에메랄드 블록을 우클릭하면 커스텀 제작대가 열립니다.
- `/제작대` 명령어로도 열 수 있습니다.

## 커스텀 아이템
| 아이템 | 효과 |
|--------|------|
| 텔레포트 스크롤 | 랜덤 좌표로 이동 (1회용) |
| 치유의 물약+ | 체력 완전 회복 + 흡수 효과 |
| 광부의 곡괭이 | 채굴 시 50% 확률로 더블 드랍 |
| 탐험가의 나침반 | 가장 가까운 구조물 방향 표시 |
| 보호의 토템+ | 사망 시 아이템 드랍 방지 (1회용) |

## 공개 API
```java
ItemStack item = CustomCraftingAPI.getCustomItem("teleport_scroll");
CustomCraftingAPI.giveCustomItem(player, "teleport_scroll", 1);
```

## config.yml 설정
- `crafting-block`: 제작대로 사용할 블록 타입
- `custom-items.<id>`: 커스텀 아이템 정의
- `recipes.<id>`: 레시피 정의 (3x3 모양)
