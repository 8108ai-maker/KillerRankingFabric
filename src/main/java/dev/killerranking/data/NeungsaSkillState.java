package dev.killerranking.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.killerranking.KillerRankingMod;
import dev.killerranking.combat.NeungsaSlashRules;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.server.level.ServerPlayer;

/** Purchase and cooldown survive reconnects and death without modifying Salmosa ownership. */
public record NeungsaSkillState(boolean unlocked,long readyAt,long iaidoReadyAt,long echoReadyAt) {
    public NeungsaSkillState {readyAt=Math.max(0,readyAt);iaidoReadyAt=Math.max(0,iaidoReadyAt);echoReadyAt=Math.max(0,echoReadyAt);}
    public NeungsaSkillState(boolean unlocked,long readyAt,long iaidoReadyAt){this(unlocked,readyAt,iaidoReadyAt,0);}
    public NeungsaSkillState(boolean unlocked,long readyAt){this(unlocked,readyAt,0);}
    public static final NeungsaSkillState LOCKED=new NeungsaSkillState(false,0);
    public static final Codec<NeungsaSkillState> CODEC=RecordCodecBuilder.create(i->i.group(
        Codec.BOOL.optionalFieldOf("unlocked",false).forGetter(NeungsaSkillState::unlocked),
        Codec.LONG.optionalFieldOf("readyAt",0L).forGetter(NeungsaSkillState::readyAt),
        Codec.LONG.optionalFieldOf("iaidoReadyAt",0L).forGetter(NeungsaSkillState::iaidoReadyAt),
        Codec.LONG.optionalFieldOf("echoReadyAt",0L).forGetter(NeungsaSkillState::echoReadyAt)
    ).apply(i,NeungsaSkillState::new));
    private static final class Storage {
        static final AttachmentType<NeungsaSkillState> TYPE=AttachmentRegistry.create(KillerRankingMod.id("neungsa_skill"),
            b->b.initializer(()->LOCKED).persistent(CODEC).copyOnDeath());
    }
    public static void initialize(){Storage.TYPE.identifier();}
    public static NeungsaSkillState get(ServerPlayer p){return p.getAttachedOrCreate(Storage.TYPE);}
    public static void set(ServerPlayer p,NeungsaSkillState state){p.setAttached(Storage.TYPE,state);}
    public int cooldown(long now){return NeungsaSlashRules.cooldown(readyAt,now);}
    public int iaidoCooldown(long now){return (int)Math.clamp(iaidoReadyAt-now,0,dev.killerranking.combat.NeungsaIaidoRules.COOLDOWN);}
    public int echoCooldown(long now){return (int)Math.clamp(echoReadyAt-now,0,dev.killerranking.combat.NeungsaEchoRules.COOLDOWN);}
    public NeungsaSkillState echo(long now){return unlocked&&echoCooldown(now)==0?new NeungsaSkillState(true,readyAt,iaidoReadyAt,now+dev.killerranking.combat.NeungsaEchoRules.COOLDOWN):this;}
    public NeungsaSkillState owned(){return new NeungsaSkillState(true,readyAt,iaidoReadyAt,echoReadyAt);}
    public NeungsaSkillState cast(long now){return unlocked&&cooldown(now)==0?new NeungsaSkillState(true,now+NeungsaSlashRules.COOLDOWN,iaidoReadyAt,echoReadyAt):this;}
    public NeungsaSkillState iaido(long now){return unlocked&&iaidoCooldown(now)==0?new NeungsaSkillState(true,readyAt,now+dev.killerranking.combat.NeungsaIaidoRules.COOLDOWN,echoReadyAt):this;}
}
