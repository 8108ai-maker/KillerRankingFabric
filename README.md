# 0.20.99 업데이트

능사 카타나의 뽑기·좌우 베기·내려베기·검집에 넣기를 별도 동작으로 제작했습니다. 뽑기와 넣기는 검집 방향을 따라 직선으로 움직이고, 검이 완전히 빠진 다음 베기 자세로 전환합니다. 손 안에서 칼만 돌아가지 않도록 손목의 잡는 방향을 고정했습니다. 복귀 시 불필요하게 한 바퀴 더 도는 회전도 제거했습니다. 클라이언트와 서버는 모두 0.20.99를 사용하세요.

# 0.20.98 업데이트

능사 카타나의 잔상을 넓게 이어지는 보라색 도트 면으로 다시 만들었습니다. 맨앞은 실제 검날을 따라 일자로 끝나고, 지나간 궤적은 부드럽게 이어지며 뒤쪽부터 사라집니다. 잘게 떨어진 조각과 앞부분의 갈고리 형태를 제거했습니다. 전방으로 날아가는 원거리 참격과 해당 원거리 피해도 제거했습니다. 클라이언트와 서버는 모두 0.20.98을 사용하세요.

# 0.20.97 업데이트

능사 카타나의 베기 이펙트를 다시 만들었습니다. 보낸 형태 순서처럼 작은 곡선에서 넓고 긴 보라색 초승달로 이어지고, 휘두르는 동안 칼끝을 따라 유지되며 끝난 뒤 뒤쪽부터 줄어듭니다. 실제 베기의 준비·통과·후속 궤적을 기록하고, 사용 중 첫 프레임의 수명 때문에 참격이 끊기는 문제를 고쳤습니다. 번개처럼 갈라지는 텍스처를 정리하고 보라색 도트 면과 밝은 바깥 날을 적용했습니다. 원거리 참격의 시각적 시작 위치는 검 앞쪽으로 옮겼습니다. 클라이언트와 서버는 모두 0.20.97을 사용하세요.

# 0.20.96 업데이트

능사 카타나의 검과 한손 베기 모션을 다시 만들었습니다. 검의 손잡이는 준비·베기·복귀 내내 실제 손 위치를 따라갑니다. 허리 옆 검집 자세, 자연스러운 몸·발의 회전과 기존 팔·무기 크기를 유지합니다.

공격마다 보라색 도트 참격이 칼끝 궤적에 남으며, 전방으로 원거리 참격이 한 번씩 나갑니다. 원거리 참격은 사거리 12m·기본 피해 4이고, 벽에서 막히며 적마다 한 번만 맞습니다. 기존 근접 공격력 8·공격속도 1.8은 유지됩니다. Better Combat 2.4의 실제 공격 시점에 발사됩니다. 클라이언트와 서버는 모두 0.20.96을 사용하세요.

# 0.20.95 업데이트

능사 카타나의 공격을 한손 베기로 변경했습니다. 좌우 베기에서 날이 칼끝 이동 방향을 향하며, 반대손은 허리 옆 검집에 남습니다. 팔·무기 크기와 공격 수치는 유지합니다. 클라이언트와 서버는 모두 0.20.95를 사용하세요.

# 0.20.94 업데이트

능사 카타나는 옆 허리춤의 검집에 넣은 상태로 잡습니다. 오른손은 손잡이, 왼손은 검집 입구를 잡으며 검집은 허리 뒤쪽으로 뻗습니다. 공격·패링 때 검을 뽑고 끝나면 다시 옆 허리춤 자세로 돌아옵니다. 팔과 무기의 크기는 유지합니다. 클라이언트와 서버는 모두 0.20.94를 사용하세요.

# 0.20.92 업데이트

능사의 카타나를 추가했습니다. 사진을 기준으로 검정 계열의 길고 얇은 도트 칼날, 검정 손잡이, 금색 코등이와 끝 장식을 만들었습니다. 낮은 양손 준비 자세에서 좌우 베기와 내리치기를 사용하며 몸·발이 함께 움직입니다. 팔과 무기는 원래 크기를 유지하고 양손은 손잡이를 잡습니다. 전용 일반 패링 모션과 칼 궤적 도트 이펙트를 포함합니다. 기본 피해 8·공격 속도 1.8·내구도 1,561, 다이아몬드로 수리합니다. 다이아몬드 2개·실 2개·금괴 1개로 제작하거나 전투 크리에이티브 탭, `/give @s killerranking:neungsa_katana`로 얻습니다. 능사 스킬은 이번 변경에 포함하지 않습니다. 설치 파일은 `build/libs/killerranking-0.20.92.jar`입니다.

# 0.20.91 업데이트

살모사 독기 뱀의 외형을 다시 만들었습니다. 낮고 작은 각진 머리, 긴 목 위쪽의 얇은 좌우 펼침, 굵고 긴 몸통 마디 10개와 표면의 도트 비늘을 적용했습니다. 턱을 벌리며 긴 송곳니를 드러내고 혀를 날름거리며, 빨강·주황 불꽃 눈은 새 머리 위치에 맞춰 움직입니다. 보라색 연기는 뱀 뒤쪽으로 옮기고 투명도를 높여 형태를 읽기 쉽게 했습니다. 구매·스킬·5초 소개 연출 후 발광 전환은 유지합니다. 설치 파일은 `build/libs/killerranking-0.20.91.jar`입니다.

# 0.20.90 업데이트

살모사 독기 뱀의 머리 위치를 0.25m 낮췄습니다. 목 윗부분은 머리에 맞춰 점진적으로 내려 연결하며 눈 불꽃·송곳니·턱·혀·위협 동작도 같은 위치를 따릅니다. 줄인 머리 크기와 몸통 크기는 유지합니다. 설치 파일은 `build/libs/killerranking-0.20.90.jar`입니다.

# 0.20.89 업데이트

살모사 독기 뱀의 머리 크기를 25% 줄였습니다. 턱·송곳니·혀와 눈 불꽃의 위치·크기도 같은 비율로 맞춰 머리와 함께 움직입니다. 넓고 납작한 목과 몸통의 크기는 유지합니다. 설치 파일은 `build/libs/killerranking-0.20.89.jar`입니다.

# 0.20.88 업데이트

살모사 독기 뱀의 목 양옆으로 납작하게 벌어지는 부분을 추가했습니다. 머리 아래에서 좌우로 넓어지고 몸통으로 갈수록 좁아지는 하나의 연속된 형태이며, 얇은 앞뒤 두께와 넓은 옆면에 비늘 무늬를 적용했습니다. 머리의 높이도 보강해 얇은 판처럼 보이던 인상을 줄였습니다. 구매 조건과 스킬 효과는 그대로입니다. 설치 파일은 `build/libs/killerranking-0.20.88.jar`입니다.

# 0.20.87 업데이트

살모사 묶음을 구매하면 독기 뱀이 살모사 전용 외형으로 바뀝니다. 넓고 각진 머리, 벌어진 입, 긴 송곳니, 굵은 몸통과 비늘 무늬를 갖추며 위협 동작과 혀 날름거림, 눈 위치의 불꽃 입자는 유지합니다. 구매 시 새 외형을 5초간 보여준 뒤 기존 보라색 발광으로 전환하고, 이후 독기를 켤 때도 구매한 살모사가 나옵니다. 구매 여부를 주변 플레이어에게도 동기화하며 기존 구매자는 재구매가 필요 없습니다. 설치 파일은 `build/libs/killerranking-0.20.87.jar`입니다.

# 0.20.86 업데이트

폰 6의 일도양단 풀 차징 시간을 5초에서 3.8초로 줄였습니다. 3.8초 전에 놓으면 대시 베기, 3.8초 이상 충전하고 놓으면 제자리 풀 차징 베기가 나갑니다. 충전 게이지·완충 효과음·흰색 투기 연출·상점 설명도 같은 기준을 사용합니다. 설치 파일은 `build/libs/killerranking-0.20.86.jar`입니다.

# 0.20.85 업데이트

살모사의 X 투척을 패링했을 때 칼이 뜨는 최고 높이를 패링 지점 위 약 2m에서 3m로 높였습니다. C 연계는 높아진 칼 위치에 맞춰 더 높이 도약해 잡고 빠르게 내려찍습니다. 설치 파일은 `build/libs/killerranking-0.20.85.jar`입니다.

# 0.20.84 업데이트

살모사의 X 칼 투척을 패링할 수 있습니다. 패링하면 투척 피해와 기존 접근·뽑기가 취소되고 칼이 위로 튕깁니다. 패링 후 0.2~0.9초 사이 C를 누르면 공중으로 도약해 칼을 잡고 빠르게 내려찍습니다. 내려찍기도 패링할 수 있으며 기본 피해 20, 기존 C와 공유하는 쿨타임 20초를 적용합니다. 연계 타이밍에는 C 아이콘이 공중 내려찍기로 바뀌고 사용 가능한 순간을 표시합니다. 팔과 다리 크기는 유지하며 실제 칼손·칼잡기 위치·어깨 타점과 피해 박자를 맞췄습니다. 살모사 묶음 1,000P 구매에 포함됩니다. 설치 파일은 `build/libs/killerranking-0.20.84.jar`입니다.

# 0.20.83 업데이트

살모사 독기 C 스킬 **어깨 찌르기**를 추가했습니다. 중단검을 들고 C를 누르며 12m 안의 상대를 지정하고 놓으면, 피슝 효과음과 함께 한 번에 접근해 칼손을 높이 들었다가 어깨에 내려꽂습니다. 접촉 박자에 기본 피해 20이 한 번 들어가며 쿨타임은 20초입니다. 살모사 묶음 1,000P에 Z·X·C가 함께 포함되며, 기존 구매자는 추가 결제 없이 사용할 수 있습니다. C는 독기 활성화·대상·무기·시야·장애물 조건을 서버에서 확인하고 시전 중 평타와 다른 스킬을 막습니다. 팔은 크기를 늘리거나 굽히지 않고 분리된 마크식 관절 위치로 맞추며, 어깨 높이에 맞춘 역수 단검·도트 찌르기 궤적·출혈 입자를 표시합니다. 설치 파일은 `build/libs/killerranking-0.20.83.jar`입니다.

# 0.20.82 업데이트

독기 보유만으로 와사바리와 칼날 추격이 자동 해금되던 문제를 수정했습니다. 포인트 상점에서 살모사 묶음을 1,000P에 구매해야 두 스킬을 사용할 수 있습니다. 구매 전 HUD는 회색 빈 칸으로 유지하며, 서버에서도 미구매 사용·포인트 부족·중복 결제를 차단합니다. 구매는 저장되고 사망 후에도 유지됩니다. 기존 다른 인물 구매와 두 스킬의 쿨타임·모션·피해는 유지합니다. 설치 파일은 `build/libs/killerranking-0.20.82.jar`입니다.

# 0.20.81 업데이트

와사바리의 3인칭 발걸기를 낮은 바깥쪽 궤적으로 바꿨습니다. 지지발을 바닥에 고정하고 골반·상체·반대팔이 함께 균형을 잡으며, 발을 상대 발목 쪽으로 걸어 당긴 뒤 다시 딛습니다. 1인칭 모션과 넘어뜨린 뒤 다리를 잡고 찌르는 구간, 피해·출혈·구속·쿨타임은 그대로입니다. 설치 파일은 `build/libs/killerranking-0.20.81.jar`입니다.

# 0.20.80 업데이트

와사바리에서 큰 상대의 높이·폭과 누운 몸체 길이에 맞춰 시전자 간격을 조정해 겹침을 막습니다. 워든·철 골렘·파괴수 등 계층형 모델의 실제 다리와 상위 관절, 다리 큐브 크기를 읽어 발목을 잡고 다리 옆면을 찌릅니다. 칼손 장전 폭도 줄여 과도한 팔 이동을 다듬었습니다. 상대를 넘어뜨리며 위치를 고정할 때 시야 각도가 중복 적용되던 오류도 수정했습니다. 여섯 번의 찌르기와 피해·출혈·구속·쿨타임은 그대로입니다. 설치 파일은 `build/libs/killerranking-0.20.80.jar`입니다.
# 0.20.79 업데이트

독기 X 칼날 추격: 칼 비행을 0.1초로 줄이고, 피슝 효과음과 함께 상대 바로 앞에 즉시 나타나도록 변경했습니다. 뽑기 전 칼날을 가로로 눕히고 수평으로 빼내며, 몸 전체를 기울이던 설정을 제거해 허리와 발 높이가 고정됩니다. 피해·과다출혈·사거리·쿨타임은 기존과 같습니다. 설치 파일은 `build/libs/killerranking-0.20.79.jar`입니다.

# 0.20.78 업데이트

독기 X 스킬 **칼날 추격**: 살모사의 중단검을 들고 X를 누르며 12m 안의 대상을 지정한 뒤 놓으면 투척해 꽂고, 0.25초 만에 접근해 뽑습니다. 꽂기 피해 6, 뽑기 피해 10, 과다출혈 5초(방어 무시, 0.5초마다 피해 2), 쿨타임 20초입니다. 독기를 끄면 사용할 수 없고, 시전 중 평타와 다른 스킬을 차단합니다. 실제 중단검의 외형으로 날아가고 상대 몸에 꽂힌 후 다시 손으로 돌아옵니다. 재료 소모나 복제 없이 보유 무기를 유지하며, 경로의 벽·사망·차원 이동·무기 교체 시 안전하게 종료합니다. 기존 Z 와사바리는 유지합니다. 설치 파일은 `build/libs/killerranking-0.20.78.jar`입니다.

# 0.20.77 업데이트

살모사의 중단검 칼끝을 더 길게 좁혀 뾰족하게 다듬었습니다. 칼날 폭·길이와 손잡이 중심 정렬은 유지했습니다. 설치 파일은 `build/libs/killerranking-0.20.77.jar`입니다.

# 0.20.76 업데이트

살모사의 중단검 칼날 폭만 20% 줄였습니다. 길이와 손잡이 중심 정렬은 유지했습니다. 설치 파일은 `build/libs/killerranking-0.20.76.jar`입니다.

# 0.20.75 업데이트

살모사의 중단검을 새로 제공한 근접 사진에 맞춰 다시 만들었습니다. 곧은 중앙 경계선의 회색 도트 칼날, 감긴 무늬 손잡이, 칼날 축에 직각인 길고 둥근 끝의 코등이를 적용했습니다. 칼날 폭을 약 15% 넓히고 길이를 약 11% 줄였으며 손잡이와 일직선으로 중심을 맞췄습니다. 공격력 7·공속 2.2와 기존 공격 모션·와사바리 동작은 그대로입니다. 설치 파일은 `build/libs/killerranking-0.20.75.jar`입니다.

# Killer Ranking Fabric 0.20.74 (Minecraft 1.21.1)

살모사의 중단검을 추가했습니다. 제공한 사진처럼 검은 손잡이·코등이와 곧고 넓은 짙은 철색 칼날이며, 칼날은 얇은 도트 텍스처입니다. 기본 공격력 7, 공격속도 2.2, 철 등급 내구도 250입니다. Better Combat 전용 좌우 베기·찌르기와 몸통·다리 체중 이동, 도트 칼날 궤적을 적용했습니다. 와사바리도 이 중단검을 사용하며, 길어진 칼날에 손·칼끝 타격 위치를 다시 맞췄습니다. 크리에이티브 전투 탭, 제작대 또는 `/give @s killerranking:salmosa_mid_dagger`로 얻습니다. 설치 파일은 `build/libs/killerranking-0.20.74.jar`입니다.


[살모사 참고 영상의 6~16초](https://www.youtube.com/shorts/Tcfep_QfKEg)에 맞춰 와사바리를 다시 만들었습니다. 발을 뒤로 장전했다가 앞으로 걸어 상대를 넘어뜨리고, 선 자세에서 상대 다리를 거의 수직으로 들어 발목을 붙잡습니다. 칼손을 머리 높이로 크게 들어 장전한 뒤 다리 옆면으로 아래쪽 대각선으로 여섯 번 내려꽂습니다. 손과 칼끝 및 도트 궤적은 실제 렌더링된 다리 위치를 따라가며 팔다리 크기를 늘리지 않습니다. 피해량, 상태 효과, 지속시간, 쿨타임과 저장 데이터는 0.20.72와 동일합니다. 설치 파일은 `build/libs/killerranking-0.20.73.jar`입니다.

Version 0.20.60:

One native-size upper-body afterimage is colocated with the player: no sideways/forward offset or extra facing rotation. Echo torso and fists use their own twelve-tick rhythm alongside the player eight-tick punches; no pose or clock synchronization and no delayed history copy. The torso rotates around the hip joint. Native player rendering corrects the interpolated torso pivot against the actual legs, carrying head, arms, sleeves and jacket together so the waist never detaches. Echo rendering pins its own independently posed torso to the same hip/root. Rigid block arms, native skin and cloth, one echo, first-person hand visibility and normal walking pitch remain intact. Native Minecraft and Better Combat: all three views verify the torso/leg seam, zero echo-origin offset, independent upper-body poses, thirty contacts and cleanup. Combat, network, damage, cooldown and save-data classes are byte-identical to 0.20.59. Install `build/libs/killerranking-0.20.60.jar` on clients and server.

Version 0.20.59:


Rigid native block arms: no elbow deformation, shortening or growth. Detached shoulder pivots retain side loading, forward/inward punches, contact hold and recovery. Cloth and hand trails follow the same rigid transforms. Standing feet stay planted and normal walking is preserved; only the upper body turns and changes body-shot height. One upper-body echo is .38m beside the player and aimed into the same attack area; it shares the actual player animation clock, torso and head height while punches interleave two ticks apart. Native Minecraft and Better Combat: third-person views show one complete upper-body echo; first-person shows its arms while hiding its own head and torso to keep the view clear. All three views confirm zero forearm folding, no leg twisting or body-root sway, matching torso timing, thirty contacts and cleanup. Combat, damage, cooldown, network and save-data classes are byte-identical to 0.20.58. Install `build/libs/killerranking-0.20.59.jar` on clients and server.

Version 0.20.53:


Bishop X's four helpers now vanish after a 0.2-second introduction, framed by small four-tick flash particles, vertical glints, forward smears and layered teleport/swish sounds. The forward space then fills with rapid moving cuts: four pixel ribbons every 0.1 seconds, crossing horizontal, diagonal and vertical directions over the near, middle and far sections. Bright blade cores with dark thin edges cross the four-block width in 0.125 seconds, leaving short fading trails and repeated swishes. The five-second zone, 50 base-damage-2 contacts, independent 15/10/7-second X cooldowns and all Z summons remain unchanged. Reference: [official Malevolent Shrine clip](https://www.youtube.com/watch?v=uHyhPKNrf5c). Install `build/libs/killerranking-0.20.53.jar` on clients and server.

Version 0.20.52:

Bishop gains X, Brother Barrage, unlocked with the existing Bishop purchase. Two native-size hooded attackers appear on each side for one second, perform accelerated directional dagger cuts and vanish. A fixed forward zone, six blocks long and four blocks wide, continues cutting for five seconds: 50 server contacts, one every two ticks, with base damage 2 per contact. Crossing eight-frame pixel ribbons remain active for the entire zone lifetime; turning or moving the caster does not move the zone.

X has its own 15/10/7-second cooldown at trait levels 1/2/3. Z's cooldown, army count and each existing wave remain separate. Spectral attackers have no collision, loot or autonomous damage and do not count toward the summoned army. Native armor, parry, morale, team/pet and PvP rules apply to zone damage; solid walls block it. Normal attacks are gated while the skill runs, while movement and camera remain free. Disabling the trait, dying, logging out, changing dimension or changing the owned branch clears the skill and helpers. Both cooldowns persist; older player attachments gain a ready X skill through an optional field. Install `build/libs/killerranking-0.20.52.jar` on clients and server.

Version 0.20.51:

Bishop's retreat-rush attack is removed. Followers retain six continuous cuts and add six straight dagger thrusts at ticks 5/10/15/20/25/30, retracting the shoulder and arm between contacts. Each contact uses half the native dagger damage, preserving the cuts' total damage. Directional pixel ribbons can overlap; native limb sizes, the small dagger, separate head/body and connected robe remain intact.

The autonomous AI selects thrusts for farther or fleeing targets, leads their horizontal movement by a bounded amount, advances between contacts, spreads crowded approaches, replans around blocked paths and immediately seeks another reachable hostile after a kill. Ordinary low long leaps remain movement for closing distance. Native line-of-sight, armor, owner/ally/pet and PvP checks still govern damage.

Z summons another 2/4/6 followers at levels 1/2/3 with a 15/10/7-second cooldown. Existing waves remain alive, each expiring 30 seconds after its own arrival; the normal level-three cycle can maintain up to 30 followers. A new cast preserves older waves, failed spawning refunds its cooldown, and trait disable/death/logout/dimension changes dismiss all waves. HUD shows the combined follower count and current summoning cooldown. Install `build/libs/killerranking-0.20.51.jar` on clients and server.

Version 0.20.50:

Bishop's combo now crosses left/right and diagonal directions in six continuous cuts, contacting at ticks 4/8/12/16/20/24. Each contact deals half the former damage, preserving the previous triple-cut total. Every cut leaves its own animated pixel ribbon from the dagger's actual rendered transform; successive ribbons overlap briefly without connecting reversals into a zigzag. Only the ribbon is enlarged; the short dagger, native limbs and connected garment remain unchanged.

The same eight-frame, full-bright, two-sided weapon trail now follows Bishop thrust/cut/rush patterns and Checkmate, partial/full Cleave, Madness axe leap, Rush fists and Rook throw/punch/mounted punches. Bare-hand trails follow actual rendered arm matrices, including the separate mounted scene. Marker-only additions preserve the existing skill poses, hit timings, damage, charging/aiming and recovery. Preparing or resting leaves no attack trail. Install `build/libs/killerranking-0.20.50.jar` on clients and server.

Version 0.20.49:

Bishop's triple cut and retreat-rush are now the two primary attacks, selected from the opening encounter and alternated thereafter. Target changes reset the encounter. Retreat uses a cloaked backward-hop clip and physical movement based on the target's current position, checking alternative directions against solid blocks. Allied follower bodies no longer veto takeoff. Combos step after fleeing or knocked-back targets; rushing flight tracks moving targets and transitions into a finishing cut on close arrival. A blocked retreat falls back to a triple cut. Damage values, summon rules and the independent head/body and connected garment remain unchanged.

Version 0.20.48:

Bishop followers now run about 31% faster in combat. Ordinary low leaps use 2.0 blocks/tick after a three-tick windup. Their autonomous cycle adds a 24-tick triple cut with native damage contacts at ticks 6/12/18, and a retreat to at least six blocks followed by a two-tick windup and a low 3.2-block/tick rushing leap. Arrival immediately transitions into a 12-tick finishing cut, hitting at tick 4. A blocked retreat times out into normal pursuit. All damage uses the native mob pipeline; only confirmed follow-up contacts of the same triple cut clear vanilla hit frames. Native limb size, independent head/body bones and the single connected cloth garment remain intact.

Version 0.20.47:

Bishop's native head and torso are restored underneath the garment. The mask follows the head's independent look direction instead of copying the chest rotation. Only the garment is one connected surface: its hood follows the head, collar blends head and shoulders, and lower cloth follows the hips with trailing motion. All previous attack clips, movement speeds, skill rules and textures are retained.

Version 0.20.46:

Bishop followers now wear one connected, zero-thickness cloth surface from hood to hem. Its shared seams stay attached while the lower cloth trails behind a sprint. Four authored animation clips use the same keyframe engine as the mod's weapons: full-body dagger thrust, cross-body cut, cloaked sprint and crouch/takeoff/landing. Dagger rotation pivots around the grip; all limbs retain native size. Damage lands at the authored impact frame. Combat running is about 1.9 times the previous speed setting; low leaps cover up to 16 blocks at 1.65 blocks per tick after a four-tick windup. Arms and the dagger remain hidden outside strikes.

Version 0.20.45 introduced Bishop:

Bishop is a fourth KING & KILL choice, costing 1,000P and excluding the other fighting-spirit skills. Z summons 2/4/6 independent masked followers at trait levels 1/2/3 for 30 seconds, on a 40-second cooldown. Followers stay cloaked and passive until their owner deals or receives a hit, then autonomously defend their owner and hunt nearby hostile mobs with cloaked sprinting, low long-distance leaps, flanking, timed dagger stabs and retreat patterns dealing 4/5/6 base damage. Hands and the smaller dagger appear only during the 14-tick attack animation; a separate 64x64 smiling mask sits clear of the hood. The owner deals 75% normal attack damage while the enabled Bishop passive is owned. Followers disappear on trait disable, death, logout, dimension change or expiry; cooldown and ownership persist. No manual command keys are required.

Version 0.20.44 adds a reference diagonal blade-braced guard and its mirror to Pawn 6 automatic-parry echoes. One raised hand grips the hilt while the opposite lower hand supports the blade across the chest. Echoes now cycle through ten poses; the previous eight, native limb size, upper-body-only rendering, current-player alignment and all parry mechanics are preserved.

계승 나이프와 한손 도끼를 함께 들면 평준화한 공격속도에 ×0.8을 적용합니다. 기본 평준화 기준은 1.8 → 1.44이며, 양손 순서와 관계없이 적용합니다. 특성·물약·Better Combat 보정을 유지한 뒤 20% 감소시키므로 반복 갱신이나 손 교체 때 중첩되지 않습니다.

# 0.20.40 — 톱칼 밑동의 깊이 조정

유혈목이의 톱칼은 칼날 밑동이 손잡이 안으로 더 깊게 들어가도록 2모델 픽셀 내려 연결했습니다. 도트 궤적도 새 칼끝 위치에 맞췄습니다.

# 0.20.39 — 유혈목이의 톱칼

사진처럼 손잡이 가까운 부분에만 짧은 톱니가 있고, 칼날 끝부분에는 둥글게 파인 작은 홈이 있는 길고 좁은 은색 칼날과 검은 손잡이를 추가했습니다. 코등이는 없으며 칼날의 밑동을 손잡이 안에 연결했습니다. 칼날은 도트 텍스처, 손잡이는 입체 형태입니다. 기본 공격력 9·공격속도 1.6·내구도 250이며, 한 손 좌우 베기와 내려베기 3동작 및 도트 궤적을 사용합니다. 전투 아이템 탭, 제작대 또는 `/give @s killerranking:yuhyeolmoki_saw_knife`로 얻습니다. 자세한 내용은 [유혈목이톱칼.md](유혈목이톱칼.md)를 참고하세요.

# 0.20.36 — 나이프 날 면을 세로로 세우기

칼끝을 아래로 뒤집던 변경을 되돌립니다. 기존 잡는 방향에서 나이프의 긴 축을 중심으로 날 면만 90도 돌려 세로로 세웁니다. 손잡이와 칼끝의 위치, 기존 공격 모션과 능력치는 유지합니다. 1인칭도 이전의 정상적인 표시 위치로 복구합니다.

# 0.20.35 — 그냥고씨 나이프의 아래 방향 잡기

나이프는 평상시 날이 아래로 향하도록 잡습니다. 손잡이의 손 안 위치를 유지하며, 양손과 1인칭에 적용합니다. 기존 베기·찌르기 모션과 무기 능력치는 유지합니다.

# 0.20.34 — 룩 장갑의 측면 들어 내려꽂기

룩 너클 장갑은 평소 가드 없이 팔을 내리고 서 있습니다. 공격할 때 한쪽 팔을 옆으로 크게 들어 올린 채 몸 뒤로 조금 당겼다가 정면 중앙으로 내려꽂고, 타격에 맞춰 몸통과 다리도 체중을 싣습니다. 공격 후에는 팔을 내립니다. 떡발의 복싱 가드와 공격, 피해량 및 공격 속도는 유지합니다.

# 0.20.33 — 주먹 공격의 뒤로 당기기

룩 장갑과 떡발 주먹은 공격할 팔을 몸 뒤로 당겼다가 정면 중앙으로 내지릅니다. 룩은 느리고 묵직하게, 떡발은 빠르게 당겨 칩니다. 짧은 팔 길이와 주먹 크기, 가드 및 기존 타격 시점은 유지합니다.

# 0.20.32 — 짧은 팔과 가드 비율 수정

팔 길이를 직전 버전보다 약 14% 줄였습니다. 어깨에서 분리한 팔의 아래쪽은 조금 벌리고 위쪽 주먹은 맞댑니다. 붕대와 장갑 크기는 기존 95%에서 108%로 늘려 맨손을 덮도록 맞췄습니다. 공격 시 크기는 변하지 않습니다.

# 0.20.31 — 주먹 무기 자세 수정

룩 장갑과 떡발 주먹의 팔과 주먹의 맞춘 비율을 유지하며 전체 크기를 5% 줄이고, 팔 길이는 추가로 약 10% 줄여 일정하게 유지합니다. 주먹 무기 크기는 기존의 95%이며, 팔 전체를 어깨에서 분리해 몸 앞에 세우고 두 주먹을 맞대는 가드와 정면 펀치를 적용합니다. 스킬 연타는 별도 모션입니다.

# 0.20.30 — 무기 공격 전신 움직임

방망이, 나이프, 도끼, 룩 장갑, 떡발 주먹, 폰 6 카타나의 일반 공격 17개에 체중 이동과 몸통·골반 회전, 발의 피벗을 강화했습니다. 기존 손·무기 궤적과 타격 시점, 이동 중 다리 움직임을 유지합니다.

# Killer Ranking Fabric 0.20.29 (Minecraft 1.21.1)

Version 0.20.29 adds a chest-height horizontal katana guard and its opposite-side motion to automatic-parry echoes. Both use a level blade across the body, impact recoil and recovery, native arm/item size and a two-hand hilt grip. Echoes now cycle through eight motions in left/right pairs: horizontal, low diagonal, near-upright turn and overhead guard. The six existing motions and parry mechanics remain unchanged.

## 0.20.28

Version 0.20.28 flattens the automatic-parry echo's downward diagonal guard to about 21-25 degrees below horizontal. Each of the three echo guard styles now has a separately authored opposite-side motion. Echoes cycle in left/right pairs: low diagonal, near-upright turn, raised overhead guard. Both palms stay on the hilt at native arm/item size; upper-body rendering and player alignment remain unchanged.

## 0.20.27

Version 0.20.27 replaces the full-player automatic-parry echoes with native-size torso, arms, skin overlays and katana only. The baby-body render scale is bypassed. Echoes follow the source player's interpolated current position without lateral/forward offsets. Three new independent echo motions cycle through a downward diagonal blade guard, a near-upright guard with a small turn, and a raised overhead guard; the caster's parry and gameplay remain unchanged. The local first-person view hides the echo torso and uses faint arms/swords with skin back-face culling to keep the camera clear.

## 0.20.26

Version 0.20.26 adds Pawn 6's second skill, automatic parry. Unlocking Pawn 6 includes Cleave on Z and automatic parry on held X, including players who already own Pawn 6. Hold X with the katana and the King & Kill trait enabled to parry eligible close melee hits automatically. Each success clears the parry cooldown, retains flash/anvil/slowness and six-tick success protection, and applies no knockback. Hits overlapping the previous body or echo motion spawn independent translucent player-skin/sword echoes; they never restart the body animation. All motions retain native arm/item size. Normal attacks and Cleave cannot overlap automatic defense. Release, menu, weapon/trait changes, death, dimension changes and lost-input timeout cancel the hold. Rook still uses X for its grabbed-target punch. Install **0.20.26 on both clients and server**.

## 0.20.25

Version 0.20.25 removes arm growth/shrinkage from all three Pawn 6 katana normal attacks. Both arms and the sword remain at native scale; shoulder/wrist positions retain the two-hand hilt grip. Blade paths, combo timing, torso/leg motion and damage remain unchanged. Katana pixel trails follow their authored strike gate independently of arm size. All katana idle, parry and Cleave poses also retain native arm/item size.

## 0.20.24

Version 0.20.24 removes arm growth/shrinkage from both Pawn 6 katana parries and their shared resting guard. Arms and held items stay at native scale; shoulder and wrist placement preserve the two-hand hilt grip and existing blade paths. Alternating directions, timing, first-person support and parry behavior remain unchanged.

## 0.20.23

Version 0.20.23 alternates Pawn 6 katana parries between the original deflection and an opposite-side blade tilt/deflection. Both palms retain the two-hand hilt grip, and each motion returns to the same resting guard. The server chooses and sends the direction to every nearby client. Only accepted main-hand katana parry starts advance the sequence; cooldown-rejected inputs and other weapons do not. Handedness and first-person visibility remain supported. Install **0.20.23 on client and server**.

## 0.20.22

Version 0.20.22 blocks normal attacks throughout skill preparation and execution: Rush, Checkmate's square selection/jump, every Rook grab phase, Cleave's charge/slash, and the parry window. Client input runs before Better Combat's callbacks and cancels pending upswings through its optional public API. Server attack and melee-damage gates enforce the same rule, while scoped skill strikes retain their damage and timing. Normal attacks resume after completion/cancellation, including during cooldown; passive traits do not lock them. Install **0.20.22 on client and server**.

## 0.20.21

Version 0.20.21 distinguishes the point-shop character choices from their skill titles: **나이트 → 체크 메이트**, **룩 → 잡기**, **폰 6 → 일도양단**. Character buttons retain their names; details, gauges and skill notices use the corresponding skill name. The mounted-hit description also reflects the existing six hits. Install **0.20.21 on client and server**.

## 0.20.20

Version 0.20.20 renames the skill previously displayed as 일도양단 to **폰 6 (Pawn 6)** in the point shop, combat HUD, unlock notices and administrator feedback. Install **0.20.20 on client and server**.

## 0.20.19

Version 0.20.19 fixes Rook's mounted pose floating above its victim. The ordinary caster model is hidden during mounting; a separate rendering of the same animated skin is positioned with its pelvis on the captured torso, independent of the caster's standing hitbox. Two detached, upright leg blocks keep their native length and are placed close to the victim's sides with feet at ground height. Six strikes retain base damage 8 at ticks 8/23/38/53/68/83, aligned with their animation and impacts. The ordinary model returns on completion or cancellation. Install **0.20.19 on client and server**.

## 0.20.18

Version 0.20.18 gives Rook's mounted legs 25% more length and reduces their forward tilt so they no longer look shortened from the front. The legs keep straight cuboid faces, their existing width and skin UVs. Install **0.20.18 on client and server**.

## 0.20.17

Version 0.20.17 removes the curved leg deformation from Rook's C mounted finisher. Each leg retains its original straight Minecraft cuboid, dimensions and skin faces; the complete leg rotates at the hip to straddle the captive. The six mounted strikes, their timing and damage remain unchanged. Install **0.20.17 on client and server**.

## 0.20.16

Version 0.20.16 increases the **C mounted finisher of Rook's Grab from three to six strikes**. Each strike retains base damage 8 and the 15-tick (0.75-second) interval. Hands alternate through hits at ticks 8/23/38/53/68/83; the seated animation, victim impact pulses and capture anchor persist through all six hits and recovery. The action lasts 93 ticks (4.65 seconds), retaining the existing 20-second skill cooldown. The X one-hand pummel and other attacks remain unchanged. Install **0.20.16 on client and server**.

Verification: 167 automated checks pass. Native Minecraft/Better Combat checks verify six real mounted hits with the original per-hit damage and interval, all six alternating arm poses/impact pulses, the late mounted anchor and cleanup, and the existing X one-hand finishers.

## 0.20.15

Version 0.20.15 corrects the **X pummel finisher of Rook's Grab**. The offhand keeps holding the captive throughout all five strikes, and only the main hand winds back and punches forward. Left-handed players mirror these roles. The 4-damage hits at ticks 3/9/15/21/27, cooldown and capture rules remain unchanged. Throwing, mounted alternating punches and ordinary glove attacks retain their animations. Install **0.20.15 on client and server**.

Verification: 167 automated checks pass. Native Minecraft/Better Combat checks confirm a fixed grab-arm pose and a moving strike arm in right-handed, first-person and left-handed play, the original five-hit timing/damage, mounted attacks and visual cleanup.

## 0.20.14

Version 0.20.14 removes arm enlargement from **Ildoyangdan**. Both arms retain their native width and length throughout charging and both cuts. Compact hand reach and small shoulder translations maintain the two-hand hilt grip; the sword also keeps its original dimensions. Blade-tip trails use the skill's strike phase without requiring arm stretch. The straight overhead cut and all gameplay settings remain unchanged. Install **0.20.14 on client and server**.

Verification: 167 automated checks pass, including unchanged arm dimensions throughout the loaded animation. Native Minecraft/Better Combat checks confirm normal model arm scales, vertical cutting, both-hand grip, first-person/left-handed cuts, trails and the existing gameplay checks.

## 0.20.13

Version 0.20.13 corrects **Ildoyangdan** to a straight overhead cut. Charging keeps the blade upright in front of the player; both the partial-charge dash and full-charge stationary strike swing down through the forward vertical plane. Both hands follow the hilt, with forward body/foot follow-through and a narrow vertical white slash instead of a horizontal streak. The 5-second charge, 21/45 base damage, 4m dash/6m full reach, 30-second cooldown and 1,000P unlock remain unchanged. Existing ordinary katana attacks and parrying retain their animations. Install **0.20.13 on client and server**.

Verification: 167 automated checks and the native Minecraft/Better Combat skill checks pass. Native item matrices verify the vertical blade plane and both-hand grip in right-handed, left-handed and first-person views.

## 0.20.12

Version 0.20.12 adds **일도양단 (Ildoyangdan)** as a third King & Kill point-shop choice alongside Knight's Checkmate and Rook's Grab. Unlock one fighting-spirit skill for **1,000P**, equip **Pawn 6's Katana**, enable the trait and hold **Z**. Releasing before five seconds performs a **4m forward dash cut, base damage 21**. At **five seconds**, the actual player and equipment become a black silhouette with bright white eyes, the white brush-written fighting-spirit glyph and radiating aura. Releasing a full charge performs a **stationary forward cut reaching 6m, base damage 45**. Cooldown is **30 seconds**, spent when the cut begins. The HUD shows charge and cooldown at the existing fixed GUI scale 3.

Charge time, ownership, point debit, collision, targets and damage are server-authoritative. Dash movement collides with blocks and cuts require line of sight; each target is hit once per cast. Armour, parries and the existing morale system apply to base damage. Swapping weapons, disabling the trait, changing dimension, dying or disconnecting clears the charge/animation; cancelling a charge spends no cooldown, but a committed cut retains it. Continuous charge times out after 15 seconds. The skill uses both hands, animated pixel blade trails, body/foot follow-through, free camera aim and left-handed/first-person support. Existing weapon textures, attacks, parry and other skill animations remain unchanged.

Operator commands: `/kr cleave set <players> <true|false>` grants/revokes ownership (granting replaces other fighting-spirit skills); `/kr cleave reset <players>` cancels and resets cooldown. New saved data uses an optional default-locked attachment and leaves existing player-data formats intact. Install **0.20.12 on both client and server**.

Verification: **167 automated checks pass**. Native Minecraft with Better Combat verifies a real shop purchase charged once, real Z hold/release, the 4m dash and base 21 damage on an unarmoured target, a stationary full cut dealing 45 at 6m while leaving a rear target untouched, cooldown denial, weapon/trait cancellation, exclusive administrator grants, left-handed first-person full cuts, wall collision/occlusion, mesh silhouette/eyes/aura and trail cleanup. Maximum measured palm gap is 0.070m including slim-skin offsets. This is an isolated integrated-server test, not a full modpack multiplayer session.

## 0.20.11

Version 0.20.11 adds a dedicated **Pawn 6's Katana parry** on the existing **R** binding. Both hands keep holding the hilt as the straight blade rises diagonally across the chest, deflects outward and returns to guard. It works in third person, first person and left-handed play, with free camera aim. The secondary hand's item is hidden during first-person katana parrying so the second palm can grip the hilt. Other weapons keep their existing parry. Success still produces FLASH and the anvil sound, grants 0.3 seconds of protection and refunds 0.5 seconds of cooldown. Install **0.20.11 on client and server**.

Verification: 158 automated checks pass. Native Minecraft with Better Combat checks eight real R-key/network activations, blocked damage, the 10-tick cooldown refund, exact six-tick protection, FLASH/anvil packets, right/left/first-person palm alignment, free camera movement, ordinary-weapon fallback, parrying during a sword swing and animation/trail cleanup. The maximum measured grip gap is 0.067m (about one model pixel, including slim-skin offsets). This is an isolated integrated-server check, not a full modpack multiplayer session.

## 0.20.10

Version 0.20.10 adds **Pawn 6's Katana**: a long slender straight silver-white pixel blade, white wrapped handle and thin pale guard. The blade consists only of flat front/back texture faces. Base damage is **8**, attack speed **1.8**, durability **1,561**. Original two-handed attacks alternate two diagonal cuts and an overhead cut, with both palms following the hilt, the cutting edge leading the swing, foot/hip follow-through and the existing animated pixel trail sampled at the actual blade tip. First-person animation shows both arms, retains the two-hand grip at rest and through attacks, and leaves camera aim free. Craft with two diamonds, two string and one stick; repair with diamond. Find it in the Combat creative tab or use `/give @s killerranking:pawn6_katana`. Install **0.20.10 on client and server**.

Verification: 158 existing automated checks pass. An isolated native Minecraft/Better Combat run checks registration, recipe, diamond repair, damage/speed, model and animation loading, the three-cut combo, seven real hits with durability loss, two-hand palm alignment in right/left/first-person views, trail cleanup and unchanged camera aim. The long blade remains within Minecraft's model bounds and keeps only front/back faces. Existing weapon and skill assets are preserved. This is not a full modpack multiplayer session.

## 0.20.9

Version 0.20.9 makes **Rook's Grab owners taller**: a default 1.8m player becomes **2.17m**, with proportional body width, native model scale, eye height and collision dimensions. Purchasing or granting the skill applies the size change; saved ownership restores it after loading. Ownership keeps the size when the combat trait is toggled off. Revoking the skill, switching to Knight or changing trait/branch removes only Rook's size modifier, preserving other mods' scale adjustments. The point shop displays the height benefit. Install **0.20.9 on client and server**.

## 0.20.8

Version 0.20.8 closes **Tteokbal's boxing guard**: both shoulders move inward and both fists gather in front of the face, nearly touching at eye height. Jab and cross recover to this same closed guard, and the opposite hand stays in front of the face during attacks. Base damage 6, attack speed 2.8 and all skill settings are unchanged. Install **0.20.8 on client and server**.

## 0.20.7

Version 0.20.7 gives **Tteokbal's Fist its own fast boxing motions**: a compact high guard, short lead jab, rear straight and immediate return to guard, with light foot/hip pivots and the opposite fist protecting the cheek. Base attack speed increases from 1.6 to **2.8** (about 0.36 seconds per attack, or 0.30 with Better Combat's default dual-wield bonus). The actual hit cadence changes together with the motion. Rook's heavier animations and speed 1.0, Tteokbal's damage 6, bandage assets and Rush skill balance remain unchanged. Install **0.20.7 on client and server**.

## 0.20.6

Version 0.20.6 adds **Tteokbal's Fist**, a compact white bandage wrap on the hand and wrist. Base damage is 6, attack speed 1.6, durability 250; equip one or a separate wrap in each hand. Craft it with two white wool and three string, and repair with either material. The Combat item tab contains it, or use `/give @s killerranking:tteokbal_fist`. Straight punches use the corrected central hand trajectory. Rush also renders cloth on the native animated arms and skin-based fist echoes, preserving the existing skill's unlock, key, damage, duration and cooldown. Install **0.20.6 on client and server**.

## 0.20.5

Version 0.20.5 corrects Rook's glove strikes to converge on the **same forward centerline**. Each attacking hand retracts behind its shoulder, then follows a straight inward path into the central target rather than sweeping out to the side. The authored hand position accounts for the full-body rotation and actual glove attachment, while keeping the knuckle row level at contact. Base attack speed 1.0, the longer wind-up, full-body weight transfer and hit timing remain as in 0.20.4. Install **0.20.5 on client and server**.

## 0.20.4

Version 0.20.4 makes Rook's knuckle-glove attacks slower and weightier. Base attack speed drops from **2.0 to 1.0**. Better Combat's weapon wind-up ratio increases from 0.45 to 1.0: with its default server multiplier, damage lands halfway through the attack interval, allowing a deliberate shoulder pull before contact. The shoulder retracts farther behind the torso and the fist moves behind the body before a full-body forward drive, sustained follow-through and smooth recovery. Dual-wielding and trait bonuses still scale the actual attack interval. Base damage remains 7. Install **0.20.4 on client and server**.

## 0.20.3

Version 0.20.3 rebuilds Rook's knuckle-glove attacks around a planted-foot pivot and weight transfer. Hips lead the torso, the shoulder loads outward, and the fist drives straight ahead before the body unwinds into guard. Shape-preserving curves connect the poses continuously; longer entry and exit blends soften idle/combo transitions. Better Combat retains its walking/riding leg overrides and free camera aim. Dual gloves now alternate hands consistently without applying the off-hand mirror twice. Weapon damage, attack speed and hit timing are unchanged. Install **0.20.3 on client and server**.

## 0.20.2

Version 0.20.2 rebuilds Poison Aura's purple serpent from 50 small body/neck sections into **20 longer, thicker blocks** with small joint gaps and flush scale panels. The head has a broad flat skull, short blunt muzzle and hinged jaw. Long fangs unfold when the mouth opens, and a forked tongue flicks three times during each threat display. An original short hiss plays in sync with the mouth opening. Red/orange particle eyes follow the revised head transform; no pupils are added. The five-second introduction, first-person hiding, outline and combat effects retain their existing behavior. Install **0.20.2 on client and server**.

## 0.20.1

Version 0.20.1 gives Rook's grab punches, mounted punches and both glove attacks a large shoulder-height side wind-up followed by a fast straight thrust, a short contact hold and recoil. The torso turns slightly with the attacking shoulder. The five-hit grab now strikes at ticks 3/9/15/21/27 after its opening wind-up, retaining its 28-tick duration and base damage 4×5. Mounted punches retain their right/left/right sequence, hit timings and base damage 8×3. Grab anchors, cooldowns and other combat abilities are unchanged. Install **0.20.1 on client and server**.

## 0.20.0

Version 0.20.0 updates combat balance and adds morale and **Rook's Knuckle Gloves**. Below 30% health, Mixed Madness grants **18/36/54% damage** and **5/10/15% attack speed** at levels 1/2/3. The half-heart survival skill and inherited knife/hatchet pair bonuses are unchanged. The previous shipped 25% config default migrates once to 18%; other custom settings remain intact.

Poison Aura keeps Weakness I, 20/40/60% knockback resistance and a single hunter-only tracked target for 3/6/9 seconds. It now grants **15/30/45% attack and movement speed**, removes the mark's vulnerability multiplier, and applies **one point of bleeding damage each second**, including the final pulse at expiry. Retargeting transfers the bleeding; rapid re-hits renew expiry without postponing ticks. Multiple hunters cannot cause more than one bleed pulse per target per second. Bleeding uses native damage gates and absorption, has a visible red particle pulse, and cannot reapply marks or grant hit XP/morale.

All players start with **morale ×1.00**, clamped to ×0.75–×1.25, multiplying outgoing damage. A successful combat hit against the player subtracts 0.01; dealt damage adds 0.01 per actual health/absorption point lost, after armor and without overkill credit. Blocked, canceled and environmental hits do not change morale. The current hit uses the previous multiplier, then updates morale for the next hit. Saves/reconnects preserve it; death resets it to ×1.00. The U stats panel displays morale and reflects its damage effect at fixed GUI scale 3.

An enabled King & Kill **Knight's Checkmate owner** holding the barbed bat receives **Speed I and Jump Boost I**; melee damage made with the bat is **×2**, including Better Combat's attacking hand. Buffs end promptly on unequip/disable, and stronger/longer existing potion effects retain vanilla priority. Checkmate's landing damage still leaves half a heart and its targeting is unchanged. Rook ownership does not grant Knight's weapon bonus.

Rook's gloves are an iron-tier melee weapon (**base damage 7, attack speed 2.0**, before bonuses), with a compact black wrist/hand model, four metal knuckle caps and alternating compressed rear windup/straight-punch animations in Better Combat. They remain visible in grab animations. Craft with three iron ingots and three leather (rows `III / L L /  L `), find them in the Combat creative tab, or use `/give @s killerranking:rook_knuckle_gloves`. Equip another copy in the off hand for gloves on both hands. Existing Rush, Rook finishers, Checkmate, parry, weapon motion and missions remain available. Install **0.20.0 on client and server**.

Verification: 158 automated checks pass. An isolated native Better Combat game checks all three trait levels, ordinary poison damage with no vulnerability, three/nine bleed pulses including expiry, mark retargeting/disable, no bleed XP/morale recursion, morale after armor/absorption/lethal/blocked hits and save/reload, the existing half-heart survival floor, Knight ownership/bat damage and Speed/Jump cleanup with stronger potions, real glove attacks/durability, glove-equipped Rook's five-hit morale sequence/release, Checkmate's half-heart landing rule and stats at GUI scales 1 and 4. This does not represent a full modpack multiplayer session.

## 0.19.1

Version 0.19.1 changes mounted Rook attacks into alternating straight punches: the shoulder and shortened arm retract behind the torso, then snap forward into the prone target. The arm keeps the same aim angle and shoulder height through each hit; no overhead wind-up or downward swing is authored. A brief forward smear, held impact pose and quick recoil preserve the weighted rhythm. Mounted stance, skin/pants knee deformation, fixed target position, one-handed throw, damage, hit timings and cooldown are unchanged. Use 0.19.1 on both client and server.

## 0.19.0

Version 0.19.0 adds **Rook's Grab** to King & Kill. The phone's point shop offers Rook's Grab or **Knight's Checkmate** for 1,000P each, with one spirit skill owned at a time. Existing Knight owners keep their skill. Tteokbal's Rush remains on its own branch.

Hold the spirit skill key (default Z) to aim at a visible enemy within two blocks; release it to capture. A successful grab starts the 20-second cooldown. The held enemy cannot move, attack or use items. Within five seconds, choose **Z: throw (8 damage + 6 once on a wall impact), X: five punches (4 each), or C: lay upright victims on their backs (flip quadrupeds belly-up), straddle them and deliver three heavy blows (8 each)**. No choice releases the target without damage. Invalid aim cancellation costs no cooldown. X/C route to the grab while the choice is open, including when they overlap vanilla creative toolbar keys; rebinding is supported. The camera remains free.

Capture, one-handed throw and punch poses use the player's skin, sleeves and handedness. The target stays at the captured position; the attacker moves to it. Client and server suppress entity shoves and travel while held, so mount height does not drift from local gravity. Native leg/pants meshes bend continuously at the knee and spread around the victim. The throwing shoulder retracts behind the body before a single-handed forward release. Mounted punches alternate right/left/right, each with a raised wind-up, a short smear, a held impact pose and weighted recoil. Mounting uses a grounded straddling pose and requires clear space to reach the victim. Mob AI resumes with its previous setting on release. Trait disable, death, disconnect, dimension changes, spectator mode and external teleport cancel a hold while preserving its cooldown. Ownership and cooldown persist through saves. Operators can use `/kr rook set <players> true|false` and `/kr rook reset <players>`; granting Rook replaces Knight/Rush ownership, and granting Knight replaces Rook. HUD and shop retain fixed GUI scale 3. Use **0.19.0 on both client and server**.

Verification: 150 automated checks pass. An isolated Better Combat game verifies real shop and Z/X/C inputs, original target-position anchoring, client/server shove suppression and stable mount height, exactly five X hits and three C hits, one wall bonus, player action blocking, five-second expiry, saved ownership/cooldown, operator switching and existing Knight/Rush behavior. Native renders capture prone players/zombies, inverted cows, continuous skin/pants knees, alternating heavy fists and the one-handed throw. Full-modpack multiplayer remains untested.

## 0.18.0

Version 0.18.0 remakes Tteokbal's Rush as an anime barrage. Actual straight arms squash into compact poses, then snap forward through brief elongated smear poses, with four bursts per arm each second. There is no elbow folding channel. Hand/wrist geometry flares and tapers through the native skin mesh; the head, torso and legs have no authored motion. Arm length varies between 46% and 180% during the short animation cycle. First-person actual arm geometry is compacted separately.

Flying fists use cropped native forearm/hand meshes with the player's skin, native wide/slim dimensions and optional sleeve UVs. Twelve staggered lanes fan forward with growing hands, two trailing afterimages, white/green speed strokes and small impact flashes. First-person hands are smaller and offset around the aiming corridor. Minimal particles reduce the fan to six lanes. The Z key, 1,000P unlock, 5 seconds/30 hits, 5 damage per hit, 20-second cooldown and Tteokbal-only restriction are retained. Use 0.18.0 on client and server.

## 0.17.8

Version 0.17.8 changes Madness Survival to a 15-second health floor: incoming hits still land, with normal hit reactions, knockback, armor durability and absorption, while final health damage stops at half a heart (1 HP). Healing above the floor can be damaged normally during the skill. On expiry, apply Slowness I and Darkness for five seconds, once; ending the skill early also applies this penalty. The activation cooldown remains 300 seconds and is preserved across saves. Use 0.17.8 on client and server.

## 0.17.7

Version 0.17.7 gives the inherited knife/hatchet pair a shared base attack speed of 1.8 in either hand order. Existing potion, trait and Better Combat dual-wield speed modifiers still apply. With Mixed Madness enabled, this pair gains x1.3 melee damage and x1.2 attack speed at any health; the existing below-30%-health damage bonus stacks multiplicatively. The bonus ends when the trait is disabled or the pair is removed. Stats show the current bonuses. Single weapons and other combinations retain their original speeds. Use 0.17.7 on client and server.

## 0.17.6

Version 0.17.6 matches the inherited weapons to the supplied photographs: a gently curved reddish wooden hatchet handle with a broad flared silver head, and a plain black knife grip with a straight single-edged silver blade. Blade/head surfaces remain pixel textures. Palm anchors, downward idle hatchet edge, attack animations and the 0.17.5 first-person view improvements are retained. See `참고무기텍스처.md` for asset paths and built-in image generation prompts. Use 0.17.6 on client and server.

## 0.17.5

Version 0.17.5 adds compact first-person poses for the bat, knife and hatchet. The camera player holds the attacking shoulder lower and farther forward, with reduced lateral sweep, body movement and arm stretch. The original wrist grip, impact timing, damage and third-person motions are retained, and view adjustments fade in and out with the animation. Both hands are supported. Use 0.17.5 on client and server.

## 0.17.4

Version 0.17.4 removes directional shading from the weapon trail. Both faces and both swing directions retain the same texture brightness, with ordinary alpha fading and depth testing. The smooth curve, eight-frame artwork and weapon motions are retained. Use 0.17.4 on client and server.

## 0.17.3

Version 0.17.3 smooths the animated weapon trails into a single restrained arc. The slash retains its start and current blade-tip positions while suppressing intermediate wrist wobble and width changes. Its inner edge is constrained so tight swings cannot fold the strip back on itself. The existing eight-frame pixel animation, timing and weapon motions are retained. Use 0.17.3 on client and server. Earlier release notes follow.

## 0.17.2

Version 0.17.2 replaces the weapons' scattered square trails with an **eight-frame pixel-texture animation**. Night's bat, Gossi's knife and Eunbal's hatchet paint the texture onto a curved strip sampled from the actual weapon path. Each stroke opens into a bright ivory slash, breaks into pixel streaks, and expires in 0.35 seconds. Separate strokes and hands never connect; idle movement and teleports emit no effect. First-person rendering and the minimal particle setting are supported. Weapon stats, player motions and the downward idle hatchet edge are retained. Texture assets and the built-in image generation prompt are documented in `무기궤적텍스처.md`. Use 0.17.2 on client and server. Earlier release notes follow.

## 0.17.1

Version 0.17.1 turns Eunbal's hatchet cutting edge toward the ground while idle. The original grip and arm pose are retained; the blade turns to lead each attack and returns to its downward-facing idle orientation after recovery. Both handedness settings are supported. Other weapons, gameplay, UI and saved data are unchanged. Use 0.17.1 on client and server. Earlier release notes follow.

## 0.17.0

Version 0.17.0 adds **Gossi's Inherited Knife** (6 damage, 2.4 attack speed) and **Eunbal's Hatchet** (9 damage, 1.2 attack speed). Each has a 250-durability iron-repairable 3D grip and a thin, transparent pixel-textured blade. Their three custom one-handed Better Combat attacks support dual wielding. Knife attacks alternate quick slashes and a forward stab. Hatchet attacks turn the cutting edge to lead the forehand, backhand and overhead strike, including mirrored left-hand use. Brief limb compression/stretch and restrained body, free-arm and stance motion keep the handle in the palm while leaving the head and walking pitch free. Blade paths emit short pixel trails.

The phone shop now displays **only the selected spirit branch's skill**: Tteokbal owns Rush; King & Kill owns Checkmate. Cross-branch tabs are removed, an open shop updates on branch changes, and other traits show no spirit purchase. Z retains its branch-specific controls: press for Rush, hold to select and release to leap for Checkmate. The existing server ownership guards, 1,000P unlock price, single-skill purchase rule and separate cooldowns are retained.

Obtain the weapons in the Combat creative tab or with `/give @s killerranking:gossi_inherited_knife` and `/give @s killerranking:eunbal_hatchet`. Blade texture assets and generation prompts are documented in `무기텍스처.md`.

Verification: **124 automated checks pass**, including palm-centered interpolation, bounded limb motion, free head/walking channels and cutting-edge direction for both handedness settings. An isolated Better Combat game verifies both pixel-textured models, six animations, weapon stats, repair and save/load, actual damage/durability, dual-wield alternation, left-hand/first-person trails and idle expiry. Real shop purchase packets unlock only the chosen branch's skill and spend exactly 1,000P; wrong-branch purchase/cast requests are rejected. Full-modpack multiplayer remains untested. Earlier release notes follow.

## 0.16.0

Version 0.16.0 changes Checkmate to **hold Z to aim, release Z to leap**. Holding the skill key shows available two-by-two-block chess squares without starting the cast animation or spending cooldown. Aim directly at a square to highlight it and its L route; release to commit it. The destination is selected by the look ray, with no enemy preference. Releasing outside a square or with a blocked route cancels for free. Moving away from the preview origin, disabling the trait, changing the weapon or waiting ten seconds cancels the preview. Mouse yaw and pitch remain free during selection and flight; server position corrections use relative rotations so they do not overwrite local camera input.

Each knight cell is now two blocks. The route remains two cells along one axis, then one perpendicular cell, with a four-block apex. One enemy occupying the selected 2×2 landing square is hit. A connected Checkmate blow leaves **1 health point (half a heart)** after armor, resistance and absorption; shields, parry, creative immunity and invulnerability still gate the blow. An already critically wounded enemy is not healed or killed by this strike. Ordinary damage keeps its normal behavior. Casting spends the existing 20-second cooldown, with the same bat, branch, toggle, 1,000P unlock and ownership rules.

Rush now keeps the shoulder attached to the torso and bends the elbow into a **front guard** before extending. Shoulder translation never moves behind the body. The elbow alternates a short recovery and fast straight punch, with smaller 94–114% arm compression/stretch. The head and torso remain unkeyed. Existing flying fists, native skin geometry, sleeves, skill damage and timing remain unchanged.

Use 0.16.0 on both client and server. The preview packet has a new channel for the separate aiming stage. Earlier notes below describe their respective releases.

Verification for 0.16.0: 115 automated checks pass. Isolated Better Combat games verify real Z hold/release in both hands and first person, eight platforms, 57 successive mouse yaw/pitch changes during flight, exact L endpoints/four-block height, half-heart player and mob damage with armor/resistance/absorption, ordinary damage preservation, shield/immunity/spawn protection, invalid selection without cooldown, ownership and obstruction cancellation. Rush games verify native WIDE and SLIM front guards, actual bent skin/sleeve rendering, real shop/Z activation, 30 hits/150 base damage, cooldown and existing defenses. Full-modpack multiplayer remains untested.

Version 0.15.0 remakes **Tteokbal's Rush player animation**. The shoulder draws behind the torso with a folded elbow, pauses briefly under load, then snaps the hand forward. Alternating arms compress to 86% and briefly stretch to 120% during the strike, with small inward shoulder movement and staggered recoil. Shoulder height stays nearly constant; the head, torso and legs have no authored motion.

The actual player's arm and sleeve faces are subdivided and curved around a continuous elbow joint during Rush. Original cube positions, UVs, native arm widths, sleeve visibility, lighting and overlay colors are retained. Wide and slim arms use their own native geometry. Meshes are cached weakly, rendering is restricted to the active player's four arm/sleeve parts, and the normal rendering path resumes on cancellation. The animation begins at the server's reported skill age, including when a viewer starts tracking a running cast. No extra animation dependency is required.

The existing flying skin fists and speed lines, 30 hits over 5 seconds, 5 damage per hit, 20-second cooldown, 1,000P unlock and Z key are unchanged. Checkmate's chess platforms/high L leap and all other combat, UI and player data are preserved. Client and server should both use 0.15.0.

Verification for 0.15.0: 111 automated checks pass, including elbow continuity, attached shoulder/joint, bounded forearm length, alternate punches and coupled bend/compression/stretch. Isolated Better Combat games verify native WIDE and SLIM arm rendering, real Z activation, shop purchase, exactly 30 hits/150 base damage, cooldown, targeting, cancellation and existing defenses. Both arms, left-handed mode and first-person rendering are captured. The preview uses 150 real game frames over 7.50 seconds. Release comparison confirms the existing flying-fist renderer and all Checkmate resources/classes are byte-identical to 0.14.1. Full-modpack multiplayer remains untested.

Version 0.14.1 corrects **Night's Checkmate** to use the chess knight's movement. Activation projects up to eight legal black-and-white destination platforms on the floor, plus a bright L-shaped route to the selected square. Each chess cell is 1.5 blocks: move two cells along one world axis, then one perpendicular cell. Selection prefers a visible enemy's square in front of the player, then the square closest to horizontal aim. The destination is fixed at activation; an enemy moving off that square escapes the hit.

The player leaps in a four-block-high arc over the two-leg route and strikes a single enemy occupying the landing square at tick 26 (1.3 seconds), with recovery through tick 42 (2.1 seconds). The bat stays raised during flight and swings downward on landing. Floor platforms fade after impact. Server-controlled movement checks the entire player box along the route, rejects blocked/unsafe landing terrain before consuming cooldown, and cancels if a new obstruction appears. It does not place or break blocks. Ordinary armor, shields and invulnerability remain effective. A completed flight resets its fall distance.

Unlock price (1,000P), default Z key, King & Kill branch, Night's bat requirement, V toggle, damage 20, cooldown 20 seconds, ownership persistence and UI scale 3 are unchanged. Client and server should both use 0.14.1. Earlier release notes below describe those releases' behavior.

Verification for 0.14.1: 108 automated checks pass, including exact L endpoints, a four-block apex, landing-square targeting and bounded board-packet round trips. An isolated Better Combat game verifies the actual route, height, single hit, no fall damage, off-square exclusion, moving victims, floor visuals, ceiling and newly placed obstacle cancellation, cooldown, armor, shield, immunity, ownership migration, both hands and first-person trails. The preview contains 171 real game frames over 10.75 seconds. Full-modpack multiplayer remains untested.

Version 0.14.0 implements **Night's Checkmate** for the King & Kill Fighting Spirit branch. Unlock it once for 1,000 points in the phone's skill shop, enable the trait with V, hold Night's bat in the main hand, stand on solid ground, and press the shared spirit-skill key (default Z). The player loads the shoulder, hops, strikes downward once at tick 16 and recovers over 32 ticks. The attacking arm briefly compresses/stretches while shoulders, support arm, hips and legs follow the motion. The head has no authored channel. Both handedness settings and first-person item rendering are supported; the actual bat trajectory emits the existing short pixel trail. White impact flash, dust and impact sounds accompany the strike.

Initial tuning: base damage 20, cooldown 400 ticks (20 seconds) from activation, 3.5-block horizontal reach, one visible enemy in the forward cone. Armor, shields, invulnerability, parry and normal hit cooldown still apply. An empty swing consumes cooldown. Holding another item, disabling the trait, changing branch/dimension, death or disconnect cancels the cast without resetting cooldown. Ordinary attacks and parry cannot overlap the skill. A successful hit consumes two durability. No teleport, block breaking or guaranteed kill is used.

Checkmate ownership and cooldown persist through saves and death. Previous releases' King & Kill checkmate ownership flags migrate without another purchase; the ownership movement bonus remains connected. `/kr checkmate set <players> true|false` grants/revokes the actual skill and retains cooldown. Regular purchases allow only one of Rush and Checkmate; operators may grant exceptions. All UI continues using fixed scale 3. Existing Rush fist visuals, normal bat attacks and unrelated gameplay are preserved.

Reference: [original Night leap and downward strike, EP.2 ~6:42–6:45](https://www.youtube.com/watch?v=LLKcvCFgRbg&t=402s). The gameplay numbers are initial mod tuning, not values specified by the video.

Verification for 0.14.0: 103 automated checks pass. An isolated game with Better Combat 2.4.0 verifies one 20-damage strike, cooldown, ordinary-attack/parry overlap guards, trait/item cancellation, wall/rear/range exclusion, armor/shield/immunity, purchase/fraction preservation, legacy ownership migration and operator grant/revoke. Actual rendering verifies the animation, both hands, first-person trails and expiry (181 captured frames). Full-modpack multiplayer remains untested.

Version 0.13.0 adds short pixel trails to Night's bat. The trail samples the rendered barrel during the fast part of each swing, leaving staggered cream/white squares along its actual path. Dots fade in 0.225 seconds; idle movement does not emit a trail. Histories are bounded and clear on world changes. Left-handed swings are supported. The custom trail requires the supplied Better Combat animations.

The hand now grips the black wrapped handle above the pommel. Wrist translation compensates for rotation around that grip point. A coordinated weight shift starts at the hips, passes through the chest and shoulders, and settles with the free arm and a small foot counter-pivot. The supporting motions stay small: hip rotation below four degrees, chest rotation below seven degrees, weight shift at most 1.2 cm per axis and leg compression at most 1.6%. The overhead strike preserves its attacking-arm keyframes while adding coordinated support. No head track is authored. Leg positions and walking pitch remain unkeyed to preserve locomotion. Weapon damage, attack speed, durability, sounds and all other gameplay are unchanged. Existing Rush animation and flying fists are unchanged.

Verification for 0.13.0: 99 automated checks pass, including grip stability, swing direction, bounded coordinated support, preserved walking pitch, recovery and trail lifetime/bounds. An isolated Better Combat game verifies three real attacks with unchanged damage/durability, actual rendering, both hands, first-person trail emission and idle expiry. Full-modpack multiplayer remains untested.

Version 0.12.2 lowers the bat's grip slightly into the palm during the two horizontal attacks (up to 1.5 model pixels in the held item's local -Y direction). It blends back to the existing idle grip. Arm poses, grip rotation, overhead strike, weapon model and combat values are unchanged from 0.12.1.

Verification for 0.12.2: the existing 94 checks pass. An isolated Better Combat game confirmed rendering and unchanged damage/durability. Release comparison verifies that only the two animations' grip-height values and version metadata changed.

Version 0.12.1 fixes the bat's two horizontal attacks. A keyed grip rotation lays the barrel forward during the strike instead of leaving it upright above a punching fist. The wrist lags during the load and leads during follow-through; the shoulder follows a lateral arc with less forward shove. Head/torso tracks remain absent. The overhead strike, model, stats and all other gameplay are byte-identical to 0.12.0. The animation regression checks now explicitly verify the barrel's forward, nearly horizontal orientation at impact and its sweep across the front.

Verification for 0.12.1: 94 automated checks pass. An isolated Better Combat game verified the three ordinary attacks, unchanged damage/durability and actual front/oblique rendering. Release comparison confirms only the two horizontal animation resources and version metadata changed. Full-modpack multiplayer remains untested.

Version 0.12.0 adds **Night's Barbed-Wire Bat**: an original 3D wooden bat with a wrapped grip, tapered barrel, two continuous turns of metal wire and barbs. Unmodified player stats are **8 attack damage**, **1.2 attacks/second** and **250 durability**. It uses iron-tier repair and normal weapon/durability enchantments. Obtain it from the Combat creative tab or `/give @s killerranking:night_barbed_bat`. No special ability, crafting recipe or point price has been assigned yet.

Better Combat reads its three original animations: forehand, backhand and overhead strike. Every hit has a 1.0 damage multiplier. The attacking shoulder draws back, the arm compresses, briefly stretches during the fast swing, then recoils and settles. The arm stretches by at most 38%; the shoulder travels less than 0.3 block. Only the attacking arm is animated, allowing the other hand, walking and looking to retain their normal behavior. Head and torso have no authored animation tracks. Better Combat mirrors the attacks for a left-handed player. Without Better Combat the item remains a usable ordinary melee weapon with vanilla attacks.

Animation direction for future work: emphasize anticipation, fast smear frames, compression/stretch, recoil and follow-through in arms and legs. Modest separation and changes of limb length are intentional; avoid huge multi-block disconnections. Normally leave the head and torso alone unless a specific special performance calls for movement. Actual joint bending requires a supported renderer; scale/translation/rotation work with the installed PlayerAnimator. Existing Rush pose and flying fist VFX are unchanged in 0.12.0.

Verification for 0.12.0: 92 automated tests pass. An isolated Fabric game with Better Combat 2.4.0 verified item registration, 8 damage / 1.2 attack speed, persistence, baked geometry, all three animation resources, three real attacks dealing 24 total damage and consuming three durability, plus front/oblique and left-handed rendering. Full-modpack multiplayer has not been tested.

Version 0.11.2 changes only the player's Rush pose. Shoulder pivots retract behind the torso at a fixed height while both arms stay horizontal and face forward. A short loaded pose precedes a rapid extension, slight arm compression/stretch and slower recoil. There are no body, torso, head or leg tracks. The outgoing skin-arm afterimage renderer is identical to 0.11.1.

Verification for 0.11.2: 89 automated tests pass, including the loaded animation's constant arm height/orientation, rearward shoulder travel, extension/recoil and untouched body/head/leg transforms. An isolated game run verified the arm animation and both front/oblique rendering, plus the existing shop, 30-hit damage, cooldown and cancellation checks. Release contents differ from 0.11.1 only in rush.json and version metadata. Full-modpack multiplayer testing has not been performed.

Version 0.11.1 scales mission rewards by **0.00001**: old weights of 40–300 now pay **0.0004–0.003 P** (4,000–30,000 won at 1 P = 10,000,000 won). A weight of 100 pays **0.001 P**. Catalog reward fields remain base weights; `Definition.rewardUnits()` performs the conversion used by both payouts and previews. Current incomplete missions use the new reward on completion. Already completed missions are not paid again and retain their actual historical reward in the UI.

Balances use exact integer units (one won, 0.0000001 P), with a separately saved fractional remainder beside the legacy whole-point field. Existing balances keep their value. Earning, saving, profile changes, network messages and shop deductions preserve fractions. `/kr points add @s 0.001` grants a fractional amount; the per-command range is 0.0000001–1,000,000 P. Values are rounded to one-won precision. Phone, mission, shop and operator output show ordinary decimals without scientific notation. The profile and dashboard payload IDs are versioned; server and clients must both use 0.11.1. The previously specified Rush unlock price remains 1,000 P.

Verification for 0.11.1: 88 automated tests pass. An isolated actual game world verified two completed missions paying 0.001 P and 0.0004 P exactly once, legacy whole-balance preservation, fractional player NBT save/load, network/UI values, a purchase rejected one won below the price, successful purchase preserving the fractional remainder, and the decimal operator command. Full-modpack multiplayer testing has not been performed.

Version 0.11.0 adds **Tteokbal Rush**. Use the phone's **Shop** button to unlock it once for **1,000 points**, then press **Z** while Tteokbal is enabled. One cast lasts **100 ticks (5 seconds)** with **30 scheduled strikes**, each dealing **5 base damage** before armor and other defensive effects. Each strike selects one visible living target within a forward cone and 3.2 blocks; missed strikes are not banked. The **400-tick (20-second) cooldown starts at activation**. Ownership and cooldown persist across saving, death and reconnects. V off, branch loss, item use, death and changing dimension cancel a cast without refunding its cooldown. Parrying cannot start during a rush.

The rush renders actual player-skin arms and hands, including slim/wide geometry and optional sleeve layers. Staggered afterimages extend from the shoulders along the aim direction, with faint speed streaks. Both player arms wind up, punch and recoil; they are visible in first person. Version 0.11.2 removes the earlier torso rotation and replaces the arm pose with a faster strike and slower recovery. Held items are hidden only visually during the animation. No item is removed. Nearby tracking clients receive server-owned visual state, with a four-player / 32-block rendering cap. The HUD and shop keep UI scale 3.

Vanilla damage processing still handles armor, shields, parries, invincibility and PvP rules. Only rush strikes skip vanilla's repeated-hit delay; existing protection from unrelated hits is preserved. Intermediate hits suppress knockback so the target stays in range; the final strike keeps normal knockback. Skills for other branches and the newly supplied killer-app lore (ranking, badge tagging and location tracking) are not implemented in this release.

Verification for 0.11.0: 79 automated tests pass. An isolated actual game world verified the shop button and real Z-key request, one-time charging and saved ownership, 30 hits / 150 base damage, forward target selection, cooldown enforcement, cancellation, preserved damage protection, and the player-skin/arm animation. Preview frames are captured in game. Full-modpack multiplayer testing has not been performed.

Version 0.10.0 changes Mixed Madness damage, XP, live stats and the gauge to **strictly below 30% of maximum health** (exactly 30% is inactive). Its new **Madness Survival** passive triggers at **one health point / half a heart**, grants **600 server ticks (30 seconds)** of immunity and starts a **6,000-tick (300-second)** cooldown immediately. A hit that would cross the threshold, including a lethal hit, is limited to half a heart after armor, resistance and absorption. Ordinary combat and environmental damage are blocked during protection; administrative `/kill` remains available. Duration and cooldown do not scale with trait level.

The extra HUD bar and phone combat page show protection/cooldown time. V off, trait loss, death and logout cancel remaining protection without clearing cooldown. The persisted cooldown survives save/load and death; it follows overworld game time, not daylight or real time, so sleeping and `/time` do not skip it and a stopped server does not count down. Creative/spectator players cannot trigger it. If the cooldown ends while health is still at or below half a heart, it triggers again. Existing VFX timing and UI scale 3 are preserved.

Verification for 0.10.0: 74 automated tests pass. An isolated actual game world verified damage/XP on both sides of the strict 30% boundary, absorption/resistance before triggering, lethal-hit survival, a full naturally elapsed 30-second window under repeated environmental damage, cooldown/save-data persistence, day-time independence, toggle/trait/gamemode handling, death cleanup and the new HUD packet. Full-modpack multiplayer testing has not been performed.

Version 0.9.0 completes the Mixed Madness introduction: a large brush-written **狂** behind the player, dark crimson strokes, a flickering peach/red rim and rising maroon smoke. Nine stroke-edge sources produce animated downward liquid ribbons with moving highlights and detached tapered drops. The blood effect is cosmetic: no damage, stains, entities or saved world objects. The existing V toggle, five-second introduction followed by dark-red outline, stat/XP rules and fixed-scale UI are preserved. First-person self, invisibility and spectator hiding apply to the glyph, blood and smoke together. Rendering is limited to four nearby players within 32 blocks; particle settings reduce smoke density.

To view it: `/kr trait set @s mixed_madness`, F5, then V off/on. It plays on enabling regardless of health; the actual damage bonus still uses the existing low-health trigger.

Version 0.9.0 was verified in an isolated integrated game world: animated blood and glyph rendering, five-second transition, real V-key toggling, position/yaw following, first-person/invisibility/spectator hiding, and unchanged King and Kill / Killing Intent visuals. The preview uses actual game frames. Full-modpack multiplayer testing remains outside this verification.

Version 0.8.0 adds King and Kill's white brush-style **鬪**, the **V** trait toggle, and mod UI fixed to scale **3**.

V toggles the owned trait's gameplay effects and visuals together. Disabled traits grant no attack/defense/speed/knockback bonuses, on-hit effects or trait XP. Turning Poison off clears its hunting mark; already applied Weakness finishes its remaining duration. Base parrying, levels, branch ownership, gear and other mods' modifiers are preserved. The server validates toggle requests and stores the enabled flag in a separate persistent attachment that defaults to enabled for existing players and survives death.

Turning a trait on shows its large VFX for **100 server ticks (5 seconds)**, then switches to a colored player outline while enabled: Killing Intent red, Mixed Madness dark red, Tteokbal green, King and Kill white, Poison purple. Tteokbal and King and Kill share the brush glyph and dense rising smoke; Poison retains the purple snake and red/orange smoke eyes. Attacks and level-ups do not restart the visual introduction. Login, respawn, branch/trait changes and returning from spectator mode show a fresh introduction. Invisible and first-person self visuals are hidden. State is synchronized to tracking clients with remaining introduction time; scoreboard teams and vanilla glowing effects are not changed.

Phone, stats, missions, branch selection, combat gauges and hunting mark HUD use three pixels per UI unit regardless of Minecraft's GUI scale. Screen buttons use the same coordinates as their rendering. This does not change Minecraft's GUI scale setting. At very small window resolutions the fixed-size panels can extend beyond the window; ESC always closes them.

Use `/kr branch set @s king_and_kill` or `/kr branch set @s tteokbal`, then F5. **V** switches the trait; the binding is editable in Minecraft Controls. Server and clients must use the same version. The 512px glyph is bundled as a transparent texture; no external font is needed.

Historical notes below describe older releases; the new five-second introduction and toggle replace their always-on / on-hit VFX timing.

0.6.1 reduces snake-eye particle emission by two thirds at full particle settings and makes each particle about 1.8 times larger. New red/orange smoke particles emerge at the eyes, rise 0.65 blocks over 1–1.3 seconds, shrink and fade, while the emitter continues to replenish the eye silhouette. Reduced/minimal particle settings also lower emission. Poison gameplay and the purple aura remain unchanged.

Poison Aura now applies Weakness I and a single-target hunting mark for 3/6/9 seconds. The trait grants +20/40/60 percentage points of knockback resistance; marked targets take +5/10/15% damage after armor/resistance calculations. Attacking another target replaces the mark, while Weakness already applied to a previous target finishes normally. Multiple hunters use the strongest mark, never an accumulated multiplier. A private HUD shows the target's direction, distance, coordinates and remaining time only to its hunter. Marks clear on expiry, target invalidation, dimension changes, death, logout and trait changes.

Poison Aura also displays a coiled purple snake apparition with square scales, a lowered tapered head, animated jaw and rising violet smoke. Smaller instances of the same blocky smoke particles as Killing Intent cluster at the snake's eyes: red outer flames and orange inner flames form sharp slanted silhouettes, with no modeled eyeballs or pupils. The body and surrounding smoke remain purple. A successful attack refreshes it for 3/6/9 seconds. The model follows the body, has no collision or damage, and is hidden for the owner in first person. Cosmetic state is synchronized to nearby tracking clients. Up to four nearby snake apparitions render within 32 blocks.

Traits now gain persistent experience and automatically reach levels 2 and 3 at 300 and a further 600 XP. Killing Intent and Mixed Madness earn one XP per accumulated 20 active server ticks; Fighting Spirit and Poison Aura earn one per successful dealt/received combat hit, including lethal hits. Blocked hits, environmental damage, self hits, creative and spectator players do not earn XP. Progress is shown in the phone combat tab. XP and fractional active time survive save/reload and death; manual level setting resets progress. Server config `traitLevel2Xp` and `traitLevel3Xp` control thresholds.

Killing Intent now has a rising crimson and dark smoke aura behind the player, using vanilla particle sprites. It follows the existing 60-tick damage bonus after a successful hit, refreshes on subsequent hits and fades when the effect ends. The server sends visual state to the owner and tracking players, including newly tracking viewers. Your own aura is hidden in first person; use F5 to view it. Invisible, dead and spectator players do not show the aura. Particle settings reduce density, with a 32-block range and a maximum of six nearby auras. The new visual sequence above supersedes this older timing.

Operator commands: `/kr parry reset <players>` clears only remaining cooldown; `/kr trait reroll <players>` uses configured weights while preserving level, points and missions and clearing branch/Checkmate/current Killing Intent timer. `/kr help` lists commands; `/kr` aliases `/killerranking` with the same permission requirement.

Successful parries immediately subtract 10 ticks from the remaining cooldown, clamped at zero, and synchronize the cooldown HUD. The six-tick immunity remains unchanged.

Successful parries grant six server ticks of damage immunity. The deadline uses overworld game time (not daylight time); incoming hits do not refresh it. Protection is cleared on death, reconnect and server shutdown without changing vanilla invulnerability flags.

Starter phones are delivered once per player on join, including existing players after upgrading. Already owned inventory/ender-chest phones count as received. Full inventories defer delivery; the persistent receipt survives reconnects and death.

See [사용안내.md](사용안내.md) for installation, exact bonuses, commands, mission timing and verification scope.

- Persistent combat trait levels 1–3 with linear percentage bonuses.
- One-time Fighting Spirit branch selection: Tteokbal or King and Kill. Beneficial bonuses scale with level; the King and Kill movement penalty remains -2% until Checkmate ownership replaces it with +2/4/6%.
- Branch benefits use server-side attribute modifiers; defense reduces damage with an entity source. Unselected players retain the legacy low-health effect until they choose a branch.
- U opens live stats including gear, potions and active trait bonuses. The phone also opens stats and missions.
- 100 mission definitions, 10 per player per world calendar cycle: 3 kills, 3 mining, 3 crafting and 1 activity. Cycles are 72,000 overworld day-time ticks, so sleeping advances the schedule.
- Persistent stat baselines and completion flags, immediate point rewards, no reconnect rerolls. Legacy single zombie and wall-clock hunt missions are replaced by this board.
- Operator commands for trait, branch, level, Checkmate ownership and points. Branch selection packets cannot change an already selected branch or another player's data.
- Existing phone texture, combat gauges and parry animation/flash/anvil sound retained.

Tteokbal Rush is the first purchasable active skill. Other skill mechanics and stat-based unlock requirements are outside this release. Mixed Madness uses the 狂/blood introduction added in 0.9.0 and a dark-red outline.

## Build

Use JDK 21 and run `gradlew.bat build` on Windows or `./gradlew build` elsewhere. Build includes 79 regression tests. Install `build/libs/killerranking-0.11.0.jar` on both server and clients, replacing the earlier Killer Ranking jar. Requires Fabric Loader 0.19.5+, Fabric API and Player Animator 2.0.4+ for Minecraft 1.21.1.

Tests cover attribute calculation and cleanup, profile migration, trait levels, command permissions, 100 valid mission stat IDs, balanced random selection, calendar boundaries, backward time changes, reward flags and serialization. Actual client screens and payload round trips are smoke-tested separately. An isolated integrated world also verifies joining, starter phone delivery, ten missions, aura activation from a real attack, expiry, retriggering, first-person hiding and removal on trait change. Smoke-only classes and test worlds are excluded from release. Full CurseForge modpack and multiplayer gameplay have not been tested.

Mission definitions are in `src/main/resources/data/killerranking/missions.json`. They are bundled resource definitions, not a runtime datapack reload API. Keep definition IDs stable across updates to preserve active boards. Trait/mission attachments are registered during mod initialization, before loading player NBT.

On Windows, Gradle's classpath-jar fallback can affect development launches under long non-ASCII paths. Production jars do not use that launch mechanism.

The XP integration smoke test verifies timed Killing Intent and Mixed Madness progression, passive dealt/received and lethal-hit rewards, environmental damage exclusion, and the phone XP display using a real integrated world. Test-only threshold overrides and harness classes are excluded from the release.

The Poison integrated-world smoke verifies all three levels of Weakness I, removal of legacy poison, knockback resistance, post-armor damage multipliers, target replacement and cleanup, private HUD payload round trips and expiry. VFX checks cover actual hit activation, retriggering, expiration, first-person hiding, trait changes, level-three duration, position/orientation following and reset synchronization. Preview frames are captured in game.

The Tteokbal integrated-world smoke checks activation without attacking, continuous ownership refresh, payload round trips, unchanged stats and XP, branch/trait changes, spectator transitions, invisibility and first-person hiding. Its previews include front, back, turned and nighttime views. Full-modpack multiplayer gameplay has not been tested.

The 0.8.0 King and Kill integrated-world check verifies white activation without combat, continuous refresh, branch payload round trips and unknown-branch cleanup, green/white switching, unchanged level-two stats and Checkmate movement, unselected-branch cleanup, trait/spectator changes and first-person/invisible hiding. Front, back, turning and night captures were inspected; the preview is actual in-game footage. Full-modpack multiplayer gameplay remains untested.

The combined toggle/UI integration checks validate real V-key packets, disabled/restored gameplay across all four traits, XP exclusion, hunting-mark cleanup, saved player-data restoration, five-second introduction/outline transitions and all five colors. GUI scales 1–4 are checked for physical scale 3, phone navigation and mission-page hitboxes. Final captures include the completed outline postprocessing pass.


