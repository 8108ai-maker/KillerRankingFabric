package dev.killerranking.client.mixin;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ModelPart.Cube.class)
public interface RushCubeAccess {
    @Accessor("polygons") ModelPart.Polygon[] killerranking$rushFaces();
}
