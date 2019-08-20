package mcjty.rftools.blocks.screens.network;

import mcjty.rftools.blocks.screens.ScreenTileEntity;



@SideOnly(Side.CLIENT)
public class PacketGetScreenDataHelper {
    public static void setScreenData(PacketReturnScreenData message) {
        ScreenTileEntity.screenData.put(message.getPos(), message.getScreenData());
    }
}
