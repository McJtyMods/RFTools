package mcjty.rftools.playerprops;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.PlayerBuff;
import mcjty.rftools.RenderGameOverlayEventHandler;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class SendBuffsToClientHelper {

    public static void setBuffs(PacketSendBuffsToClient buffs) {
        RenderGameOverlayEventHandler.buffs = new ArrayList<PlayerBuff>(buffs.getBuffs());
    }
}
