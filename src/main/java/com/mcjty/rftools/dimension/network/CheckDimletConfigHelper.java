package com.mcjty.rftools.dimension.network;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class CheckDimletConfigHelper {

    public static void checkDimletsFromServer(PacketCheckDimletConfig message) {
        World world = Minecraft.getMinecraft().theWorld;
        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);

        dimensionManager.checkDimletConfigFromServer(message.getDimlets(), world);
    }

}
