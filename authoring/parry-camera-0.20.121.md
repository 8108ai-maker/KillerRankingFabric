# Parry-only camera correction

The user explicitly restricted all camera shake and rapid focus/rebound to parrying. The normal attack camera mixin, its registration and the attack pulse entry point have been removed entirely. Existing ordinary attack poses from0.20.119 remain unchanged. Only the local successful-parry packet creates a camera pulse; remote success does not.

Keep the original ANVIL_PLACE metal sound at volume0.5 and pitch0.5. Keep all Pawn6 parry clips. World and hand rendering share the same render-only focus and shake. Player aim and FOV settings remain unchanged.

The separate native capture exercises52 ordinary attack views and asserts zero camera pulses, FOV changes or shakes throughout that entire attack phase. Then11 actual successful parries must apply the camera effect and expire to the resting projection.
