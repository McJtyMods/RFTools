package mcjty.rftools.dimension.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ReturnDestinationInfoHelper {
    public static Integer id = null;
    public static String name = "?";

    public static void setDestinationInfo(PacketReturnDestinationInfo message) {
        id = message.getId();
        name = message.getName();
    }

}
