# SkillTree 플러그인

채굴, 전투, 생존 3개 스킬 트리 시스템입니다.

## 명령어
- `/스킬` - 스킬 트리 GUI 열기

## 스킬 트리
- **⛏️ 채굴**: 더블 드랍, 자동 제련, 광맥 감지, 연쇄 채굴
- **⚔️ 전투**: 흡혈, 회피, 크리티컬 마스터, 반격
- **🌿 생존**: 배고픔 감소, 낙하 보호, 야간 시야, 자연 치유

## 스킬 포인트 획득
- 레벨업 시 스킬 포인트 획득 (기본: 1포인트/레벨)
- 퀘스트 완료 시 추가 포인트 (QuestSystem 연동)

## 공개 API
```java
int level = SkillTreeAPI.getSkillLevel(player, "double_drop");
SkillTreeAPI.addSkillPoints(player, 5);
```
