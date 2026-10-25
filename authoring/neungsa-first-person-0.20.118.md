# Iaido first-person grip correction

The fixed shoulder pivots and native limb scales from 0.20.117 remain intact. The wide/slim full-dash clips in `neungsa-charge-0.20.118` correct the cut grip by about 5.7 degrees: the sword follows the actual cuboid's longitudinal +Y axis, rather than a vector from the shoulder pivot to the off-center palm. The palm stays at its actual skin-model offset (-1,10,0).

In 0.20.118, `FirstPersonWeaponPose` rotates and translates both shoulder pivots and arm rotations in one camera frame. The sword and saya retain the authored item rotations and actual palm attachments. The previous first-person wrist substitution is removed. Throughout the outward cut and two-second held finish, the blade continues the rigid forearm direction. The camera frame blends only after the entire blade has cleared the saya, stays constant during the cut and hold, and returns before reinsertion.

Do not rotate the view wrist separately to keep the sword on screen. Position the entire camera rig together, without stretching limbs or moving individual shoulders. Inspect actual first-person frames after changing this rig. Gameplay timings, extraction path, outward blade sweep and shoulder attachment stay unchanged.
