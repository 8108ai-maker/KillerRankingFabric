package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.MirrorModifier;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class ParryAnimationTest {
	private KeyframeAnimationPlayer guard() throws Exception {
		try (var reader = Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/parry.json"))) {
			var animations = AnimationSerializing.deserializeAnimation(reader);
			assertEquals(1, animations.size());
			var player = new KeyframeAnimationPlayer(animations.getFirst());
			for(int frame=0;frame<8;frame++)player.tick();player.setupAnim(0);
			return player;
		}
	}
	@Test void bothArmsGuideAttackSidewaysRatherThanHoldAFrontBlock() throws Exception {
		var guard = guard();
		var right = guard.get3DTransform("rightArm", TransformType.ROTATION, 0, Vec3f.ZERO);
		var left = guard.get3DTransform("leftArm", TransformType.ROTATION, 0, Vec3f.ZERO);
		assertTrue(right.getX() < -1.3F && left.getX() < -1.3F, "Both arms enter the deflection");
		for(int frame=0;frame<7;frame++)guard.tick();guard.setupAnim(0);
		var sweptRight=guard.get3DTransform("rightArm",TransformType.ROTATION,0,Vec3f.ZERO);
		var sweptLeft=guard.get3DTransform("leftArm",TransformType.ROTATION,0,Vec3f.ZERO);
		assertTrue(sweptRight.getY()<right.getY()-.3F,"The weapon hand must continue sideways after contact");
		assertTrue(sweptLeft.getY()<left.getY()-.6F,"The opposite hand sweeps aside instead of staying in front");
		var layer=new ModifierLayer<IAnimation>();layer.addModifierLast(new dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier(3));layer.setAnimation(guard);
		for (int i = 0; i < 15; i++) layer.tick();
		assertFalse(layer.isActive(), "The48-frame guard must release Better Combat within16 game ticks");
	}
	@Test void mirroringMovesGuardToLeftMainHand() throws Exception {
		ModifierLayer<IAnimation> layer = new ModifierLayer<>();
		layer.addModifierLast(new MirrorModifier(true));
		layer.setAnimation(guard());
		layer.setupAnim(0);
		var left = layer.get3DTransform("leftArm", TransformType.ROTATION, 0, Vec3f.ZERO);
		assertTrue(left.getX() < -1.3F);
		assertTrue(left.getY() > .7F);
		assertEquals(1,layer.get3DTransform("leftArm",TransformType.SCALE,0,new Vec3f(1,1,1)).getY(),.0001f);
	}
}
