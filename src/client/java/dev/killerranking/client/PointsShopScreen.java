package dev.killerranking.client;
import dev.killerranking.data.KillerPoints;
import dev.killerranking.combat.SpiritBranch;
import dev.killerranking.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class PointsShopScreen extends PanelScreen {
    private AbstractButton buy,knight,rook,cleave,bishop,salmosa,neungsa;private int waiting,selection,poisonSelection;
    public PointsShopScreen(Screen parent){super("포인트 상점",parent);selection=ClientBishop.unlocked()?3:ClientCleave.unlocked()?2:ClientCheckmate.unlocked()?0:1;poisonSelection=ClientNeungsaSkill.unlocked()?1:0;}
    private SpiritBranch selectedBranch(){var d=ClientDashboard.get();return d!=null&&d.trait().equals("fighting_spirit")?ClientDashboard.branch():SpiritBranch.NONE;}
    private boolean king(){return selectedBranch()==SpiritBranch.KING_AND_KILL;}
    private boolean poison(){var d=ClientDashboard.get();return d!=null&&d.trait().equals("poison_aura");}
    @Override protected void init(){
        super.init();
        knight=button(24,78,70,23,"나이트",()->{selection=0;update();});
        rook=button(98,78,70,23,"룩",()->{selection=1;update();});
        cleave=button(172,78,70,23,"폰 6",()->{selection=2;update();});
        bishop=button(246,78,70,23,"비숍",()->{selection=3;update();});
        salmosa=button(24,78,144,23,"살모사",()->{poisonSelection=0;update();});
        neungsa=button(172,78,144,23,"능사",()->{poisonSelection=1;update();});
        buy=button(24,218,292,25,"스킬 해금 · 1,000P 사용",()->{
            if(!canBuy())return;
            if(poison()&&poisonSelection==1&&ClientPlayNetworking.canSend(NeungsaRequestPayload.TYPE)){ClientPlayNetworking.send(new NeungsaRequestPayload(NeungsaRequestPayload.UNLOCK));waiting=40;}
            else if(poison()&&poisonSelection==0&&ClientPlayNetworking.canSend(SalmosaUnlockPayload.TYPE)){ClientPlayNetworking.send(new SalmosaUnlockPayload());waiting=40;}
            else if(king()&&selection==3&&ClientPlayNetworking.canSend(BishopRequestPayload.TYPE)){ClientPlayNetworking.send(new BishopRequestPayload(BishopRequestPayload.UNLOCK));waiting=40;}
            else if(king()&&selection==2&&ClientPlayNetworking.canSend(CleaveRequestPayload.TYPE)){ClientPlayNetworking.send(new CleaveRequestPayload(CleaveRequestPayload.UNLOCK));waiting=40;}
            else if(king()&&selection==1&&ClientPlayNetworking.canSend(RookRequestPayload.TYPE)){ClientPlayNetworking.send(new RookRequestPayload(RookRequestPayload.UNLOCK,-1));waiting=40;}
            else if(king()&&ClientPlayNetworking.canSend(CheckmateUnlockPayload.TYPE)){ClientPlayNetworking.send(new CheckmateUnlockPayload());waiting=40;}
            else if(selectedBranch()==SpiritBranch.TTEOKBAL&&ClientPlayNetworking.canSend(RushUnlockPayload.TYPE)){ClientPlayNetworking.send(new RushUnlockPayload());waiting=40;}
        });update();
    }
    @Override public void tick(){if(waiting>0)waiting--;update();}
    private boolean owned(){return poison()?(poisonSelection==1?ClientNeungsaSkill.unlocked():ClientSalmosa.unlocked()):king()?(selection==3?ClientBishop.unlocked():selection==2?ClientCleave.unlocked():selection==1?ClientRook.unlocked():ClientCheckmate.unlocked()):ClientRush.unlocked();}
    private boolean branch(){return poison()?(poisonSelection==1?ClientNeungsaSkill.branch():ClientSalmosa.branch()):king()?(selection==3?ClientBishop.branch():selection==2?ClientCleave.branch():selection==1?ClientRook.branch():ClientCheckmate.branch()):selectedBranch()==SpiritBranch.TTEOKBAL&&ClientRush.branch();}
    private boolean otherOwned(){return !poison()&&(king()?(selection==3?ClientBishop.otherOwned():ClientBishop.unlocked()||(selection==2?ClientCleave.otherOwned():selection==1?ClientRook.otherOwned():ClientCheckmate.rushOwned())):ClientCheckmate.unlocked()||ClientRook.unlocked()||ClientCleave.unlocked()||ClientBishop.unlocked());}
    private boolean canBuy(){return waiting==0&&branch()&&!owned()&&!otherOwned()&&ClientProfile.get().pointUnits()>=KillerPoints.whole(1000);}
    private void update(){
        if(buy==null)return;
        knight.visible=rook.visible=cleave.visible=bishop.visible=king();
        salmosa.visible=neungsa.visible=poison();
        salmosa.setMessage(Component.literal((poisonSelection==0?"● ":"")+"살모사"));
        neungsa.setMessage(Component.literal((poisonSelection==1?"● ":"")+"능사"));
        knight.setMessage(Component.literal((selection==0?"● ":"")+"나이트"));rook.setMessage(Component.literal((selection==1?"● ":"")+"룩"));cleave.setMessage(Component.literal((selection==2?"● ":"")+"폰 6"));
        bishop.setMessage(Component.literal((selection==3?"● ":"")+"비숍"));
        buy.visible=poison()||selectedBranch()!=SpiritBranch.NONE;buy.active=canBuy();
        buy.setMessage(Component.literal(!branch()?"동기화 중…":owned()?"해금 완료":otherOwned()?"다른 투기 스킬을 이미 해금했습니다":waiting>0?"해금 요청 중…":ClientProfile.get().pointUnits()<KillerPoints.whole(1000)?"포인트 부족 · 1,000P 필요":"스킬 해금 · 1,000P 사용"));
    }
    @Override protected void content(GuiGraphics g){
        text(g,"보유 포인트  "+KillerPoints.format(ClientProfile.get().pointUnits())+" P",22,48,WHITE);g.fill(16,72,324,207,0xFF1B2636);
        if(poison()){
            if(poisonSelection==1){
                text(g,"능사 · 독기 스킬 3종",24,112,0xFFD7AAFF);
                text(g,KillerRankingClient.rushKeyLabel().getString()+" 무형참격 · 누르면 즉시 시전",24,132,WHITE);
                text(g,KillerRankingClient.secondaryKeyLabel().getString()+" 발도술 · 짧게 12 / 충전 25",24,150,WHITE);
                text(g,KillerRankingClient.rookMountKeyLabel().getString()+" 연속참격 · 큰 베기 + 잔상 20회 + 마무리",24,168,WHITE);
                text(g,"C: 12m · 매 타격 4 · 쿨타임 20초",24,186,MUTED);
                text(g,"능사 카타나 필요 · 1,000P",24,200,MUTED);
            }else{
                text(g,"살모사 · 독기 스킬 3종",24,112,0xFFD7AAFF);
                text(g,KillerRankingClient.rushKeyLabel().getString()+" 와사바리 · 2m · 다리 6회 찌르기",24,132,WHITE);
                text(g,KillerRankingClient.secondaryKeyLabel().getString()+" 칼날 추격 · 12m · 투척 후 순간 접근",24,150,WHITE);
                text(g,KillerRankingClient.rookMountKeyLabel().getString()+" 어깨 찌르기 · 12m · 피해 20",24,168,WHITE);
                text(g,"각각 쿨타임 20초 · X / C는 중단검 필요",24,186,MUTED);
                text(g,"1,000P · 세 스킬 해금 + 살모사 독기 외형",24,200,MUTED);
            }
            text(g,"독기를 켠 상태에서 사용 · 무기에 맞춰 스킬 표시",24,250,MUTED);return;
        }
        if(selectedBranch()==SpiritBranch.NONE){text(g,"현재 특성의 판매 스킬이 없습니다.",24,105,WHITE);text(g,ClientDashboard.needsChoice()?"먼저 투기의 분기를 선택해주세요.":"해당 전투 특성의 스킬을 여기서 해금합니다.",24,130,MUTED);return;}
        if(king()){
            if(selection==3){
                text(g,"비숍 · 신의 부름 / 형제들의 도움",24,112,0xFFEA86B0);
                text(g,KillerRankingClient.rushKeyLabel().getString()+" 신의 부름 · 2 / 4 / 6명 · 30초 유지",24,132,WHITE);
                text(g,KillerRankingClient.bishopBarrageKeyLabel().getString()+" 형제들의 도움 · 6m · 2피해/0.1초 · 5초",24,150,WHITE);
                text(g,KillerRankingClient.secondaryKeyLabel().getString()+" 상대지정 · 거리 제한 없음 · 내 형제만 추격",24,168,WHITE);
                text(g,KillerRankingClient.rushKeyLabel().getString()+" / "+KillerRankingClient.bishopBarrageKeyLabel().getString()+" 각각 15 / 10 / 7초 · 본인 피해 ×0.75",24,187,MUTED);
            }else if(selection==2){
                text(g,"일도양단 / 자동 패링",24,112,WHITE);text(g,KillerRankingClient.rushKeyLabel().getString()+" 꾹 눌러 3.8초 충전 → 놓아서 베기",24,132,WHITE);
                text(g,"대시 4m · 피해 21 / 완충 6m · 피해 45",24,150,WHITE);
                text(g,KillerRankingClient.secondaryKeyLabel().getString()+" 유지: 자동 패링 · 연속 공격은 잔상 방어",24,168,WHITE);
                text(g,"카타나 필요 · 일도양단 쿨타임 30초",24,187,MUTED);
            }else if(selection==1){
                text(g,"잡기",24,112,0xFFFFE8A9);text(g,"2m 안의 상대 지정 → 놓아서 붙잡기",24,132,WHITE);
                text(g,"5초 안에 "+KillerRankingClient.rushKeyLabel().getString()+" 던지기 / "+KillerRankingClient.rookPunchKeyLabel().getString()+" 주먹 / "+KillerRankingClient.rookMountKeyLabel().getString()+" 올라타기",24,150,WHITE);
                text(g,"던지기 8 + 벽 6 · 주먹 4×5 · 올라타기 8×6",24,168,MUTED);
                text(g,"기본 키 2.17m · 쿨타임 20초 · 하나만 해금",24,187,MUTED);
            }else{
                text(g,"체크 메이트",24,112,WHITE);text(g,"L자 발판으로 도약해 적을 내려치는 일격",24,132,MUTED);
                text(g,"2×2 발판 · 적중 시 반 칸 · 나이트 방망이 필요",24,150,WHITE);
                text(g,KillerRankingClient.rushKeyLabel().getString()+" 누르고 선택 → 놓으면 도약",24,168,WHITE);
                text(g,"쿨타임 20초 · 투기 스킬 하나만 해금",24,187,MUTED);
            }
        }else{
            text(g,"떡발의 투기 · 떡발의 연타",24,88,0xFF8AF3A3);text(g,"내 스킨의 팔·주먹 잔상이 쏟아지는 연속 공격",24,117,MUTED);
            text(g,"5초 동안 30타 · 타격당 기본 피해 5",24,143,WHITE);text(g,"발동 키: "+KillerRankingClient.rushKeyLabel().getString()+" · 쿨타임 20초",24,164,WHITE);
            text(g,KillerRankingClient.secondaryKeyLabel().getString()+" 강타 · 피해 30 · 쿨타임 25초",24,190,WHITE);
            text(g,"연타 해금 시 함께 해금 · 붕대 주먹 필요",24,211,MUTED);
        }
        text(g,selectedBranch().label()+"를 켠 상태에서 사용",24,250,MUTED);
    }
}
