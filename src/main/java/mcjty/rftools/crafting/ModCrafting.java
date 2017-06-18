package mcjty.rftools.crafting;

import mcjty.rftools.GeneralConfiguration;
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
import mcjty.rftools.blocks.powercell.PowerCellSetup;
import mcjty.rftools.blocks.relay.RelaySetup;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.security.SecuritySetup;
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.spawner.SpawnerSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerSetup;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.RecipeSorter;

public final class ModCrafting {
    static {
        RecipeSorter.register("rftools:shapedpreserving", PreservingShapedRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:shapedorepreserving", PreservingShapedOreRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:containeranditem", ContainerAndItemRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:containertoitem", ContainerToItemRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
        RecipeSorter.register("rftools:nbtmatchingrecipe", NBTMatchingRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }

    public static void init() {
        initBaseCrafting();

        CoalGeneratorSetup.initCrafting();
        CrafterSetup.initCrafting();
        ModularStorageSetup.initCrafting();
        TeleporterSetup.initCrafting();
        ScreenSetup.initCrafting();
        LogicBlockSetup.initCrafting();
        MachineInfuserSetup.initCrafting();
        BuilderSetup.initCrafting();
        PowerCellSetup.initCrafting();
        RelaySetup.initCrafting();
        MonitorSetup.initCrafting();
        ShieldSetup.initCrafting();
        EnvironmentalSetup.initCrafting();
        SpawnerSetup.initCrafting();
        BlockProtectorSetup.initCrafting();
        ItemFilterSetup.initCrafting();
        SecuritySetup.initCrafting();
        EndergenicSetup.initCrafting();
        StorageScannerSetup.initCrafting();
        ElevatorSetup.initCrafting();
        BoosterSetup.initCrafting();
    }

    private static void initBaseCrafting() {

        String[] syringeMatcher = new String[] { "level", "mobId" };

        // @todo recipes
//        MyGameReg.addRecipe(new NBTMatchingRecipe(3, 3,
//                                                     new ItemStack[]{SyringeItem.createMobSyringe(EntityIronGolem.class), SyringeItem.createMobSyringe(EntityEnderman.class), SyringeItem.createMobSyringe(EntitySnowman.class),
//                                                             SyringeItem.createMobSyringe(EntityBat.class), SyringeItem.createMobSyringe(EntityOcelot.class), SyringeItem.createMobSyringe(EntityGuardian.class),
//                                                             SyringeItem.createMobSyringe(EntityWolf.class), SyringeItem.createMobSyringe(EntityPigZombie.class), SyringeItem.createMobSyringe(EntityMooshroom.class)},
//                                                     new String[][]{syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher},
//                                                     new ItemStack(ModItems.peaceEssenceItem)));

        int dimShardCraftability;
        if (Loader.isModLoaded("rftoolsdim")) {
            dimShardCraftability = GeneralConfiguration.dimensionalShardRecipeWithDimensions;
        } else {
            dimShardCraftability = GeneralConfiguration.dimensionalShardRecipeWithoutDimensions;
        }

        switch (dimShardCraftability) {
            case GeneralConfiguration.CRAFT_NONE:
                break;
            case GeneralConfiguration.CRAFT_EASY:

                break;
            case GeneralConfiguration.CRAFT_HARD:

                break;
        }
    }
}
