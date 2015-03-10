package com.mcjty.rftools.dimension.network;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class SyncDimensionInfoHelper {

    public static void syncDimensionManagerFromServer(PacketSyncDimensionInfo message) {

        World world = Minecraft.getMinecraft().theWorld;
        System.out.println("SYNC DIMENSION STUFF: world.isRemote = " + world.isRemote);
        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);

        dimensionManager.syncFromServer(message.getDimensions(), message.getDimensionInformation());
        dimensionManager.save(world);
    }

}
