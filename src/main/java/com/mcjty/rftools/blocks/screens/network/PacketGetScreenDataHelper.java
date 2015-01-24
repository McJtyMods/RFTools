package com.mcjty.rftools.blocks.screens.network;

import com.mcjty.rftools.blocks.screens.SimpleScreenTileEntity;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PacketGetScreenDataHelper {
    public static void setScreenData(PacketReturnScreenData message) {
        Coordinate c = new Coordinate(message.x, message.y, message.z);
        SimpleScreenTileEntity.screenData.put(c, message.screenData);
    }
}
