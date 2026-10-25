# Shoulder-attached Neungsa Iaido

The latest request replaces the earlier detached-arm style for this skill. Both arm pivots stay on the native shoulders: X=-5/+5, Y=2 for wide skins and 2.5 for slim skins, Z=0. Arm and item scales remain one. Do not solve a moving fist target by moving the shoulder.

The six editable JSON clips are the actual production carry, charge and full-dash exports. The authoring script is `work/author_neungsa_charge_v20117.py` in the enclosing workspace. Frames use 32 animation samples per Minecraft tick; global body offsets use blocks, while limb pivots and palm offsets use model pixels.

The right palm owns the blade. The opposite palm owns the saya, with its relative orientation stored in `leftItem` and grip distance in `neungsaSaya.y`. Both shoulders stay fixed even while the opposite hand withdraws the saya. The real 21.51-pixel blade clears the saya mouth before turning. The wrist remains fixed throughout the outward cut and its held finish.

Dash phases, in Minecraft ticks: extraction 0–1.5; preparation 1.5–2.5; outward cut 2.5–6.5; exact finished pose until 46.5; alignment until 50.5; insertion until 55.5; attached carry until 58. The forty-tick hold does not extend gameplay parry or attack lock. A new attack may interrupt the visible recovery.

First-person uses one shared translation for the two shoulder pivots and retains the exact blade direction and native scales. A separate fixed view grip during the cut lets the forearm rise from its virtual shoulder below the camera, keeping the finished sword visible without a floating whole arm. Never restore the old independently positioned fist path or arm enlargement. Capture the actual client after any pose changes; unit geometry alone cannot establish the appearance.
