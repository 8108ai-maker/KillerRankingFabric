# Combat camera follow-up

Reference inspected directly in the browser: https://www.youtube.com/watch?v=9NPbOl0LGds . Frame stepping at 134.449994, 134.549995 and 134.749999 seconds showed the first-person contact flash, changing view framing and raised blocking hand. Camera feedback is an interpretation of that short impact, not a claim of measured source camera parameters. Remote media was not downloaded; its audio was not analysed.

The original metal parry sound is restored exactly from the 0.20.118 source archive: ANVIL_PLACE, volume 0.5, pitch 0.5. The white contact spark added in 0.20.119 is retained.

The view now has a render-only impact curve: rapid narrowing of FOV, slight wider rebound, brief damped fine movement, exact return to the resting projection within 0.35 seconds for attacks and 0.4 seconds for parries. Ordinary attack feedback is scheduled at the actual Better Combat upswing boundary. A local successful parry starts a stronger pulse from the success packet. Nearby players cannot shake the local view. Stacked impulses are capped.

GameRenderer applies the same FOV ratio and shake to the world and the held-hand rendering. No entity rotation, aim, velocity, option FOV or player animation is rewritten. Pawn6 normal/reverse/auto/echo parry assets and all 0.20.119 weapon poses remain unchanged.
