package mcjty.rftools.blocks.infuser;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MachineInfuserSetup {
    public static MachineInfuserBlock machineInfuserBlock;

    public static void init() {
        machineInfuserBlock = new MachineInfuserBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        machineInfuserBlock.initModel();
    }

    public static void initCrafting() {

    }
}
