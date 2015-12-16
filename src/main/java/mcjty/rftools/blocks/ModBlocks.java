package mcjty.rftools.blocks;

import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModBlocks {
    public static MachineFrame machineFrame;
    public static MachineBase machineBase;

    public static void init() {
        initBaseBlocks();
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame();
        GameRegistry.registerBlock(machineFrame, "machineFrame");

        machineBase = new MachineBase();
        GameRegistry.registerBlock(machineBase, "machineBase");
    }

}
