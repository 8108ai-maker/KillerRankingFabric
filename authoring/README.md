# Blockbench 제작 파일

- `neungsa_katana.bbmodel`: 현재 게임의 Java 1.21.1 아이템을 불러온 편집 파일. 실제 Java 내보내기는 `exports/neungsa_katana.item.json`.
- `neungsa_katana_geckolib.bbmodel`: 같은 아이템 요소를 Texture Stitcher로 묶은 GeckoLib 제작 예제.
- `exports/neungsa_katana.geo.json`: GeckoLib 4.9.3이 지원하는 1.12.0 형식, 뼈 1개·큐브 19개.
- `exports/neungsa_katana.animation.json`: 제작 경로 검증용 2초 반복 애니메이션.
- `exports/neungsa_katana_atlas.png`: 실제 Texture Stitcher가 만든 아틀라스.

이 폴더는 제작 원본과 검증 예제입니다. 게임 자산 폴더에 자동 복사하지 않으며, 기존 게임 무기와 플레이어 모션도 자동 전환하지 않습니다. 새 모델 적용 시 모드의 타깃·그립·텍스처 방향을 확인하고 실제 게임 촬영으로 검증합니다.
