package dev.killerranking.client.mixin;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Use an actual hind leg of native four-legged models, including their own sizes and pivots. */
@Mixin(QuadrupedModel.class)
public interface WasabariQuadrupedAccess {
    @Accessor("rightHindLeg") ModelPart wasabari$rightHindLeg();
    @Accessor("leftHindLeg") ModelPart wasabari$leftHindLeg();
}
