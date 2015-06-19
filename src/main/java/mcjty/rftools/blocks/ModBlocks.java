package mcjty.rftools.blocks;

import cpw.mods.fml.common.registry.GameRegistry;
import mcjty.rftools.blocks.blockprotector.BlockProtectorSetup;
import mcjty.rftools.blocks.crafter.CrafterSetup;
import mcjty.rftools.blocks.dimletconstruction.DimletConstructionSetup;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.endergen.EndergenicSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.infuser.MachineInfuserSetup;
import mcjty.rftools.blocks.itemfilter.ItemFilterSetup;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.monitor.MonitorSetup;
import mcjty.rftools.blocks.relay.RelaySetup;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.security.SecuritySetup;
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorSetup;
import mcjty.rftools.blocks.spawner.SpawnerSetup;
import mcjty.rftools.blocks.special.SpecialSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;

public final class ModBlocks {
    public static MachineFrame machineFrame;
    public static MachineBase machineBase;

    public static void init() {
        initBaseBlocks();

        MonitorSetup.setupBlocks();
        CrafterSetup.setupBlocks();
        RelaySetup.setupBlocks();
        ItemFilterSetup.setupBlocks();
        StorageScannerSetup.setupBlocks();
        MachineInfuserSetup.setupBlocks();
        EnvironmentalSetup.setupBlocks();
        SpawnerSetup.setupBlocks();
        SpecialSetup.setupBlocks();
        ScreenSetup.setupBlocks();
        DimletSetup.setupBlocks();
        TeleporterSetup.setupBlocks();
        EndergenicSetup.setupBlocks();
        LogicBlockSetup.setupBlocks();
        ShieldSetup.setupBlocks();
        DimletConstructionSetup.setupBlocks();
        SpaceProjectorSetup.setupBlocks();
        BlockProtectorSetup.setupBlocks();
        ModularStorageSetup.setupBlocks();
        SecuritySetup.setupBlocks();
    }

    private static void initBaseBlocks() {
        machineFrame = new MachineFrame();
        GameRegistry.registerBlock(machineFrame, "machineFrame");

        machineBase = new MachineBase();
        GameRegistry.registerBlock(machineBase, "machineBase");
    }

}
