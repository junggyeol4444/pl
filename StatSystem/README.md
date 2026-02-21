# StatSystem 플러그인

플레이어의 상세 활동 통계를 추적하는 시스템입니다.

## 명령어
- `/스탯` - 내 통계 GUI 확인
- `/스탯 <닉네임>` - 다른 플레이어 통계 확인
- `/랭킹` - 카테고리별 서버 랭킹 메뉴
- `/랭킹 <카테고리>` - 특정 카테고리 랭킹

## 추적 통계
| 통계 | 설명 |
|------|------|
| play_time | 총 플레이 시간 (초) |
| walk_distance | 이동 거리 (블록) |
| mobs_killed | 몹 처치 수 |
| deaths | 사망 횟수 |
| blocks_mined | 블록 채굴 수 |
| blocks_placed | 블록 설치 수 |
| items_crafted | 아이템 제작 수 |
| pvp_kills | PVP 킬 |
| pvp_deaths | PVP 데스 |
| fish_caught | 낚시 횟수 |
| login_count | 접속 횟수 |

## 공개 API
```java
long value = StatAPI.getStat(player, "mobs_killed");
StatAPI.addStat(player, "custom_stat", 1);
List<Map.Entry<String, Long>> top = StatAPI.getTopPlayers("mobs_killed", 10);
```

## config.yml 설정
- `save-interval-minutes`: 자동 저장 간격 (분)
- `stat-categories`: 통계 ID와 표시 이름 매핑
