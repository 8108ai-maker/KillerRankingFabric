package dev.killerranking.client;
import dev.killerranking.combat.RookRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class RookHud {
    public static void render(GuiGraphics g){
        var c=Minecraft.getInstance();if(c.player==null||c.options.hideGui||c.player.isSpectator())return;
        if(ClientKnifePursuit.phase()==dev.killerranking.combat.KnifePursuitRules.AIM){
            FixedUi.begin(g);var target=ClientKnifePursuit.selected();g.drawCenteredString(c.font,target==null?"12m 안의 상대를 바라보세요":target.getName().getString()+" · X를 놓으면 칼날 추격",FixedUi.width()/2,FixedUi.height()/2+27,target==null?0xFFC7CBD3:0xFFE0B0FF);FixedUi.end(g);return;
        }
        if(!ClientRook.choice()&&ClientRook.phase()!=RookRules.AIM&&!ClientRook.captured())return;
        FixedUi.begin(g);int x=FixedUi.width()/2,y=FixedUi.height()/2+27;
        if(ClientRook.captured())g.drawCenteredString(c.font,"붙잡힘 · 행동 불능",x,y,0xFFFFA49A);
        else if(ClientRook.phase()==RookRules.AIM){
            var target=ClientRook.selected();g.drawCenteredString(c.font,target==null?"2m 안의 상대를 바라보세요":target.getName().getString()+" · 키를 놓으면 잡기",x,y,target==null?0xFFC7CBD3:0xFFFFE8A9);
        }else{
            y=Math.max(20,Math.min(FixedUi.height()-133,SkillHud.top()-65));
            g.fill(x-151,y-5,x+151,y+49,0xE5101723);
            var target=ClientRook.target();g.drawCenteredString(c.font,"잡기 · "+(target==null?"상대":target.getName().getString())+" · "+CombatHud.seconds(ClientRook.remaining()),x,y,0xFFFFE8A9);
            g.drawCenteredString(c.font,KillerRankingClient.rushKeyLabel().getString()+" 던지기   "+KillerRankingClient.rookPunchKeyLabel().getString()+" 주먹 연속타   "+KillerRankingClient.rookMountKeyLabel().getString()+" 올라타기",x,y+16,0xFFF1F4FA);
            g.drawCenteredString(c.font,"시간 안에 선택하지 않으면 놓아줍니다",x,y+29,0xFF9FACBF);
            CombatHud.bar(g,x-140,y+42,280,4,(float)ClientRook.remaining()/RookRules.CHOICE_DURATION,0xFFFFE8A9);
        }
        FixedUi.end(g);
    }
    private RookHud(){}
}
