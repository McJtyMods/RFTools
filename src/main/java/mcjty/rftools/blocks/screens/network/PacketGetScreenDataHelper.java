package mcjty.rftools.blocks.screens.network;

import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PacketGetScreenDataHelper {
    public static void setScreenData(PacketReturnScreenData message) {
        ScreenTileEntity.screenData.put(message.getPos(), message.screenData);
    }
}
