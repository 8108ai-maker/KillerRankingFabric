package dev.killerranking.combat;

import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.core.data.gson.AnimationSerializing;
import dev.kosmx.playerAnim.core.util.Vec3f;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Shape of the reference-style basic attacks (tools/anim/build_swings.py): a big wind-up behind the body,
 * a four-tick whip through the front with the hips turning, a held follow-through and a clean return to rest.
 */
class NightBatAnimationTest {
    private static final int IMPACT = 10;

    static KeyframeAnimationPlayer load(String name) throws Exception {
        try (var reader = Files.newBufferedReader(Path.of("src/client/resources/assets/killerranking/player_animations/" + name + ".json"))) {
            return new KeyframeAnimationPlayer(AnimationSerializing.deserializeAnimation(reader).getFirst());
        }
    }

    /** Item +Y (the blade) in body space from the raw channels, the way the renderer composes them. */
    static org.joml.Vector3f blade(KeyframeAnimationPlayer animation, float delta) {
        var arm = animation.get3DTransform("rightArm", TransformType.ROTATION, delta, Vec3f.ZERO);
        var grip = animation.get3DTransform("rightItem", TransformType.ROTATION, delta, Vec3f.ZERO);
        // ModelPart Z/Y/X, vanilla held-item X=-90/Y=180, then the grip's Z/Y/X.
        return new org.joml.Matrix4f().rotateZ(arm.getZ()).rotateY(arm.getY()).rotateX(arm.getX())
            .rotateX(-(float) Math.PI / 2).rotateY((float) Math.PI)
            .rotateZ(grip.getZ()).rotateY(grip.getY()).rotateX(grip.getX())
            .transformDirection(new org.joml.Vector3f(0, 1, 0)).normalize();
    }

    @ParameterizedTest
    @ValueSource(strings = {"night_bat_forehand", "night_bat_backhand", "night_bat_slam"})
    void wrappedHandleStaysInThePalmWhileTheWristTurns(String name) throws Exception {
        var animation = load(name);
        var model = com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/killerranking/models/item/night_barbed_bat.json"))).getAsJsonObject();
        var display = model.getAsJsonObject("display").getAsJsonObject("thirdperson_righthand");
        float gripY = (1.3f - 8) * display.getAsJsonArray("scale").get(1).getAsFloat() + display.getAsJsonArray("translation").get(1).getAsFloat();
        for (int tick = 0; tick <= 24; tick++) {
            for (float delta : new float[] {0, .5f}) {
                animation.setupAnim(delta);
                var rotation = animation.get3DTransform("rightItem", TransformType.ROTATION, delta, Vec3f.ZERO);
                var position = animation.get3DTransform("rightItem", TransformType.POSITION, delta, Vec3f.ZERO);
                var grip = new org.joml.Matrix4f().translation(position.getX(), position.getY(), position.getZ())
                    .rotateZ(rotation.getZ()).rotateY(rotation.getY()).rotateX(rotation.getX())
                    .transformPosition(new org.joml.Vector3f(0, gripY, 0));
                assertTrue(grip.distance(0, -2, 0) < .5, "Wrapped handle drifts less than half a model pixel between poses");
            }
            animation.tick();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"night_bat_forehand", "night_bat_backhand"})
    void barrelWhipsAcrossTheFrontFromOneSideToTheOther(String name) throws Exception {
        var animation = load(name);
        org.joml.Vector3f before = null, after = null;
        for (int tick = 0; tick <= 24; tick++) {
            animation.setupAnim(0);
            var barrel = blade(animation, 0);
            var grip = animation.get3DTransform("rightItem", TransformType.ROTATION, 0, Vec3f.ZERO);
            if (tick == IMPACT - 1) before = barrel;
            if (tick == IMPACT + 2) after = barrel;
            if (tick == IMPACT) {
                assertTrue(barrel.z < -.9, "At impact the barrel points into the target, not upward");
                assertTrue(Math.abs(barrel.y) < .2, "Side strike stays nearly horizontal");
            }
            if (tick == IMPACT - 3) assertTrue(barrel.z > 0.2 && Math.abs(barrel.x) > .7, "Wind-up carries the barrel behind the body on one side: " + barrel);
            if (tick == IMPACT + 3) assertTrue(barrel.z > 0.2 && Math.abs(barrel.x) > .7, "Follow-through ends behind the body on the other side: " + barrel);
            if (tick == 24) {
                assertEquals(0, grip.getX(), .001); assertEquals(0, grip.getY(), .001); assertEquals(0, grip.getZ(), .001);
            }
            animation.tick();
        }
        assertNotNull(before); assertNotNull(after);
        assertTrue(before.x * after.x < -.25, "Barrel sweeps across the front from one side to the other");
    }

    @ParameterizedTest
    @ValueSource(strings = {"night_bat_slam"})
    void slamComesOverTheHeadAndDownThroughTheTarget(String name) throws Exception {
        var animation = load(name);
        for (int tick = 0; tick <= 24; tick++) {
            animation.setupAnim(0);
            var barrel = blade(animation, 0);
            if (tick == IMPACT - 2) assertTrue(barrel.y < -.8, "Raised overhead before the drop: " + barrel);
            if (tick == IMPACT) assertTrue(barrel.z < -.8 && barrel.y > .1 && barrel.y < .6, "Impact lands forward and slightly down: " + barrel);
            if (tick == IMPACT + 2) assertTrue(barrel.y > .4, "Follow-through continues below the target: " + barrel);
            animation.tick();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"night_bat_forehand", "night_bat_backhand", "night_bat_slam"})
    void hipsTurnWithTheSwingWhileHeadAndWalkingStayFreeThenEverythingSettles(String name) throws Exception {
        var animation = load(name);
        boolean sweep = !name.endsWith("slam");
        var baseline = new Vec3f(.12f, .23f, .34f);
        float peakYaw = 0, yawAtWindup = 0, yawAtFollow = 0, footPivot = 0, worstJump = 0;
        Vec3f previousArm = null, previousGrip = null;
        java.util.List<Integer> gate = new java.util.ArrayList<>();
        for (int tick = 0; tick <= 24; tick++) {
            for (float delta : new float[] {0, .5f}) {
                animation.setupAnim(delta);
                for (var type : new TransformType[] {TransformType.POSITION, TransformType.ROTATION, TransformType.SCALE}) {
                    var head = animation.get3DTransform("head", type, delta, baseline);
                    assertEquals(baseline.getX(), head.getX(), .0001, "head");
                    assertEquals(baseline.getY(), head.getY(), .0001, "head");
                    assertEquals(baseline.getZ(), head.getZ(), .0001, "head");
                }
                var hips = animation.get3DTransform("body", TransformType.ROTATION, delta, Vec3f.ZERO);
                assertTrue(Math.abs(hips.getX()) < .3 && Math.abs(hips.getY()) < .8 && Math.abs(hips.getZ()) < .15, "Hip turn stays on its feet");
                peakYaw = Math.max(peakYaw, Math.abs(hips.getY()));
                var weight = animation.get3DTransform("body", TransformType.POSITION, delta, Vec3f.ZERO);
                assertTrue(Math.abs(weight.getX()) < .01 && Math.abs(weight.getY()) < .01 && Math.abs(weight.getZ()) < .01, "The body pivots in place");
                for (String leg : new String[] {"leftLeg", "rightLeg"}) {
                    var pos = animation.get3DTransform(leg, TransformType.POSITION, delta, baseline);
                    assertEquals(baseline.getX(), pos.getX(), .0001); assertEquals(baseline.getY(), pos.getY(), .0001); assertEquals(baseline.getZ(), pos.getZ(), .0001);
                    var turn = animation.get3DTransform(leg, TransformType.ROTATION, delta, baseline);
                    assertEquals(baseline.getX(), turn.getX(), .0001, "Walking pitch is not replaced");
                    assertTrue(Math.abs(turn.getY()) <= .5 && Math.abs(turn.getZ()) <= .15, "Planted feet counter the hip turn");
                    footPivot = Math.max(footPivot, Math.abs(turn.getY()));
                    var size = animation.get3DTransform(leg, TransformType.SCALE, delta, new Vec3f(1, 1, 1));
                    assertTrue(size.getY() >= .98 && size.getY() <= 1, "No leg squash");
                }
                var scale = animation.get3DTransform("rightArm", TransformType.SCALE, delta, new Vec3f(1, 1, 1));
                assertEquals(1, scale.getX(), .0001); assertEquals(1, scale.getY(), .0001); assertEquals(1, scale.getZ(), .0001);
                var shoulder = animation.get3DTransform("rightArm", TransformType.POSITION, delta, new Vec3f(-5, 2, 0));
                assertTrue(Math.abs(shoulder.getX() + 5) < 2 && Math.abs(shoulder.getY() - 2) < 1.5 && Math.abs(shoulder.getZ()) <= 3.5, "Shoulder stays on the torso");
                var free = animation.get3DTransform("leftArm", TransformType.ROTATION, delta, Vec3f.ZERO);
                assertTrue(Math.abs(free.getX()) < .9 && Math.abs(free.getY()) < .5 && Math.abs(free.getZ()) < .4, "Free arm counterbalances");
            }
            animation.setupAnim(0);
            var arm = animation.get3DTransform("rightArm", TransformType.ROTATION, 0, Vec3f.ZERO);
            var grip = animation.get3DTransform("rightItem", TransformType.ROTATION, 0, Vec3f.ZERO);
            if (previousArm != null) {
                // The renderer lerps the raw channels between keyframes, so a wrapped angle would read as a spin.
                // (The player clamps read-back rotations to (-pi, pi], so compare the wrapped difference.)
                for (var pair : new Vec3f[][] {{arm, previousArm}, {grip, previousGrip}})
                    for (float d : new float[] {pair[0].getX() - pair[1].getX(), pair[0].getY() - pair[1].getY(), pair[0].getZ() - pair[1].getZ()})
                        worstJump = Math.max(worstJump, Math.abs(Math.atan2(Math.sin(d), Math.cos(d))));
            }
            previousArm = arm; previousGrip = grip;
            if (animation.get3DTransform("nightBatTrail", TransformType.POSITION, 0, Vec3f.ZERO).getY() > .25) gate.add(tick);
            var hips = animation.get3DTransform("body", TransformType.ROTATION, 0, Vec3f.ZERO);
            if (tick == IMPACT - 2) yawAtWindup = hips.getY();
            if (tick == IMPACT + 2) yawAtFollow = hips.getY();
            if (tick == 24) {
                for (String part : new String[] {"body", "torso", "leftLeg", "rightLeg"}) {
                    var rest = animation.get3DTransform(part, TransformType.ROTATION, 0, Vec3f.ZERO);
                    assertEquals(0, rest.getX(), .001); assertEquals(0, rest.getY(), .001); assertEquals(0, rest.getZ(), .001);
                }
                assertEquals(-.35f, arm.getX(), .001); assertEquals(0, arm.getY(), .001); assertEquals(-.15f, arm.getZ(), .001);
            }
            animation.tick();
        }
        assertTrue(worstJump < 2.0, "No wrapped-angle jump between keyframes: " + worstJump);
        assertEquals(java.util.List.of(IMPACT - 3, IMPACT - 2, IMPACT - 1, IMPACT, IMPACT + 1, IMPACT + 2, IMPACT + 3), gate, "Trail gate opens around the impact only");
        if (sweep) {
            assertTrue(peakYaw > .3, "The hips turn well into the swing");
            assertTrue(yawAtWindup * yawAtFollow < 0, "Hips wind up one way and follow through the other");
        } else {
            assertTrue(peakYaw > .1 && footPivot > .05, "Even the overhead slam shifts the stance");
        }
        for (int tick = 0; tick < 20; tick++) animation.tick();
        assertFalse(animation.isActive(), "Attack releases control after settling");
    }
}
