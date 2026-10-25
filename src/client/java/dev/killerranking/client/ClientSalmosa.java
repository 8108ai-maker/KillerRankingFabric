package dev.killerranking.client;
import dev.killerranking.network.SalmosaStatusPayload;
public final class ClientSalmosa {
    private static SalmosaStatusPayload value;
    public static void set(SalmosaStatusPayload status){value=status;}
    public static void clear(){value=null;}
    public static boolean branch(){return value!=null&&value.branch();}
    public static boolean unlocked(){return value!=null&&value.unlocked();}
    private ClientSalmosa(){ }
}
