package mcjty.rftools.blocks;

import mcjty.rftools.blocks.crafter.CrafterSetup;
import mcjty.rftools.blocks.generator.CoalGeneratorSetup;
import mcjty.rftools.blocks.infuser.MachineInfuserSetup;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.ores.DimensionalShardBlock;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ModBlocks {
    public static MachineFrame machineFrame;
    public static MachineBase machineBase;

    public static DimensionalShardBlock dimensionalShardBlock;

    public static void init() {
        initBaseBlocks();

        CoalGeneratorSetup.init();
        CrafterSetup.init();
        ModularStorageSetup.init();
        TeleporterSetup.init();
        ScreenSetup.init();
        LogicBlockSetup.init();
        MachineInfuserSetup.init();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        machineFrame.initModel();
        machineBase.initModel();
        dimensionalShardBlock.initModel();

        CoalGeneratorSetup.initClient();
        CrafterSetup.initClient();
        ModularStorageSetup.initClient();
        TeleporterSetup.initClient();
        ScreenSetup.initClient();
        LogicBlockSetup.initClient();
        MachineInfuserSetup.initClient();
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame();
        machineBase = new MachineBase();

        dimensionalShardBlock = new DimensionalShardBlock();
    }

}
