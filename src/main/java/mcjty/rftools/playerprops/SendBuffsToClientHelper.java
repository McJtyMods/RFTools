package mcjty.rftools.playerprops;

import mcjty.rftools.RenderGameOverlayEventHandler;

import java.util.ArrayList;

public class SendBuffsToClientHelper {

    public static void setBuffs(PacketSendBuffsToClient buffs) {
        RenderGameOverlayEventHandler.buffs = new ArrayList<>(buffs.getBuffs());
    }
}
