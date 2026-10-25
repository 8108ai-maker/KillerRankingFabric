package dev.killerranking.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.entity.player.Player;

/** Rising dark-red mist behind the character contrasts with the downward liquid streams. */
final class MadnessAuraParticle extends TextureSheetParticle {
    private final Player owner;
    private final SpriteSet sprites;
    private final float side,height,rear,phase,size;
    private final boolean ember;
    MadnessAuraParticle(ClientLevel world,Player owner,SpriteSet sprites,boolean ember) {
        super(world,owner.getX(),owner.getY(),owner.getZ());
        this.owner=owner;this.sprites=sprites;this.ember=ember;hasPhysics=false;
        lifetime=ember?18+random.nextInt(8):28+random.nextInt(12);
        side=(random.nextFloat()-.5f)*4.15f;height=random.nextFloat()*3.45f-.10f;
        rear=1.20f+random.nextFloat()*.25f;phase=random.nextFloat()*6.283f;
        size=ember?.055f+random.nextFloat()*.035f:.40f+random.nextFloat()*.24f;
        if(ember) setColor(.95f,.24f,.12f);
        else if(random.nextFloat()<.28f) setColor(.10f,.004f,.018f);
        else setColor(.43f+random.nextFloat()*.26f,.01f,.045f);
        alpha=0;place();xo=x;yo=y;zo=z;setSpriteFromAge(sprites);
    }
    private void place() {
        float progress=age/(float)lifetime;double yaw=Math.toRadians(owner.yBodyRot);
        double sweep=side*(1+progress*.16)+Math.sin(phase+progress*4)*.12;
        setPos(owner.getX()+Math.cos(yaw)*sweep+Math.sin(yaw)*rear,
            owner.getY()+height+progress*(ember?2:1.75),owner.getZ()+Math.sin(yaw)*sweep-Math.cos(yaw)*rear);
    }
    @Override public void tick() {
        xo=x;yo=y;zo=z;oRoll=roll;
        if(owner.isRemoved() || !owner.isAlive() || owner.isInvisible() || owner.isSpectator() || !MadnessVfx.active(owner.getUUID())) { remove();return; }
        if(++age>=lifetime) { remove();return; }
        float progress=age/(float)lifetime;
        alpha=Math.min(1,age/4f)*Math.min(1,(lifetime-age)/8f)*(ember?.9f:.44f);
        quadSize=size*(1.05f-progress*.45f);roll=(float)Math.sin(phase+progress*3)*.15f;
        place();setSpriteFromAge(sprites);
        if((x-xo)*(x-xo)+(z-zo)*(z-zo)>16) { xo=x;yo=y;zo=z; }
    }
    @Override public void render(VertexConsumer v,Camera camera,float delta) {
        if(MadnessVfx.active(owner.getUUID()) && !KillingIntentVfx.hiddenForCamera(owner)) super.render(v,camera,delta);
    }
    @Override protected int getLightColor(float delta) { return 0xF000F0; }
    @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
}
