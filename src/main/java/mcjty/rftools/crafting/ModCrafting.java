package mcjty.rftools.crafting;

import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.RecipeSorter;

public final class ModCrafting {
    static {
        RecipeSorter.register("rftools:nbtmatchingrecipe", NBTMatchingRecipe.class, RecipeSorter.Category.SHAPED, "after:minecraft:shaped");
    }

    public static void init() {
        initBaseCrafting();

        EnvironmentalSetup.initCrafting();
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
