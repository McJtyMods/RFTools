package mcjty.rftools.dimension.network;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.rftools.dimension.RfToolsDimensionManager;
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
