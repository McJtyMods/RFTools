package mcjty.rftools.playerprops;

import mcjty.rftools.RenderGameOverlayEventHandler;



import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class SendBuffsToClientHelper {

    public static void setBuffs(PacketSendBuffsToClient buffs) {
        RenderGameOverlayEventHandler.buffs = new ArrayList<>(buffs.getBuffs());
    }
}
