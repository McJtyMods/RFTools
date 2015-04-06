package mcjty.rftools.blocks.screens.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import mcjty.varia.Coordinate;

@SideOnly(Side.CLIENT)
public class PacketGetScreenDataHelper {
    public static void setScreenData(PacketReturnScreenData message) {
        Coordinate c = new Coordinate(message.x, message.y, message.z);
        ScreenTileEntity.screenData.put(c, message.screenData);
    }
}
