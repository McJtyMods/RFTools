package mcjty.rftools.blocks.storage;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ReturnCountInfoHelper {
    public static int cnt = -1;

    public static void setDestinationInfo(PacketReturnCountInfo message) {
        cnt = message.getCnt();
    }

}
