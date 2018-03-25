package mcjty.rftools.blocks;

import mcjty.rftools.blocks.blockprotector.BlockProtectorSetup;
import mcjty.rftools.blocks.booster.BoosterSetup;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.crafter.CrafterSetup;
import mcjty.rftools.blocks.elevator.ElevatorSetup;
import mcjty.rftools.blocks.endergen.EndergenicSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.generator.CoalGeneratorSetup;
import mcjty.rftools.blocks.infuser.MachineInfuserSetup;
import mcjty.rftools.blocks.itemfilter.ItemFilterSetup;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.monitor.MonitorSetup;
import mcjty.rftools.blocks.ores.DimensionalShardBlock;
import mcjty.rftools.blocks.powercell.PowerCellSetup;
import mcjty.rftools.blocks.relay.RelaySetup;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.security.SecuritySetup;
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.spawner.SpawnerSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import net.minecraft.client.renderer.color.BlockColors;
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
        BuilderSetup.init();
        PowerCellSetup.init();
        RelaySetup.init();
        MonitorSetup.init();
        ShieldSetup.init();
        EnvironmentalSetup.init();
        SpawnerSetup.init();
        BlockProtectorSetup.init();
        ItemFilterSetup.init();
        SecuritySetup.init();
        EndergenicSetup.init();
        StorageScannerSetup.init();
        ElevatorSetup.init();
        BoosterSetup.init();
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
        BuilderSetup.initClient();
        PowerCellSetup.initClient();
        RelaySetup.initClient();
        MonitorSetup.initClient();
        ShieldSetup.initClient();
        EnvironmentalSetup.initClient();
        SpawnerSetup.initClient();
        BlockProtectorSetup.initClient();
        ItemFilterSetup.initClient();
        SecuritySetup.initClient();
        EndergenicSetup.initClient();
        StorageScannerSetup.initClient();
        ElevatorSetup.initClient();
        BoosterSetup.initClient();
    }

    @SideOnly(Side.CLIENT)
    public static void initClientPost() {
        ShieldSetup.initClientPost();
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame();
        machineBase = new MachineBase();

        dimensionalShardBlock = new DimensionalShardBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initColorHandlers(BlockColors blockColors) {
        ShieldSetup.initColorHandlers(blockColors);
    }

}
