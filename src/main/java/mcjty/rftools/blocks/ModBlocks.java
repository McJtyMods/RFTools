package mcjty.rftools.blocks;

import mcjty.rftools.blocks.generator.CoalGeneratorSetup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModBlocks {
    public static MachineFrame machineFrame;
    public static MachineBase machineBase;

    public static void init() {
        initBaseBlocks();

        CoalGeneratorSetup.init();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        CoalGeneratorSetup.initClient();
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame();
        machineBase = new MachineBase();
    }

}
