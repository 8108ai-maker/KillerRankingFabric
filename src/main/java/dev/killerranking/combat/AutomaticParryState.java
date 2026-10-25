package dev.killerranking.combat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Short input leases prevent a lost release packet from leaving automatic defense enabled. */
public final class AutomaticParryState {
    public static final int LEASE_TICKS=15, MOTION_TICKS=12;
    public record Strike(boolean echo,int sequence) { }
    private final Map<UUID,Long> holds=new HashMap<>(),busyUntil=new HashMap<>();
    private final Map<UUID,Integer> sequences=new HashMap<>();
    public boolean active(UUID id,long now){return now<holds.getOrDefault(id,Long.MIN_VALUE);}
    public boolean contains(UUID id){return holds.containsKey(id);}
    public boolean hold(UUID id,long now){
        boolean started=!active(id,now);
        if(started){busyUntil.remove(id);sequences.remove(id);}
        holds.put(id,now+LEASE_TICKS);return started;
    }
    public Strike strike(UUID id,long now){
        if(!active(id,now))throw new IllegalStateException("Automatic parry is not held");
        boolean echo=now<busyUntil.getOrDefault(id,Long.MIN_VALUE);
        int sequence=(sequences.getOrDefault(id,-1)+1)&4095;
        sequences.put(id,sequence);busyUntil.put(id,now+MOTION_TICKS);
        return new Strike(echo,sequence);
    }
    public void release(UUID id){holds.remove(id);busyUntil.remove(id);sequences.remove(id);}
    public void clear(){holds.clear();busyUntil.clear();sequences.clear();}
}
