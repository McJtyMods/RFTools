package mcjty.rftools.crafting;

import mcjty.rftools.blocks.environmental.EnvironmentalSetup;

public final class ModCrafting {
    public static void init() {
        initBaseCrafting();

        EnvironmentalSetup.initCrafting();
    }

    private static void initBaseCrafting() {

        String[] syringeMatcher = new String[]{"level", "mobId"};

        // @todo 1.14
//        ForgeRegistries.RECIPES.register(
//                new NBTMatchingRecipe(3, 3,
//                        new ItemStack[]{SyringeItem.createMobSyringe(EntityIronGolem.class), SyringeItem.createMobSyringe(EntityEnderman.class), SyringeItem.createMobSyringe(EntitySnowman.class),
//                                SyringeItem.createMobSyringe(EntityBat.class), SyringeItem.createMobSyringe(EntityOcelot.class), SyringeItem.createMobSyringe(EntityGuardian.class),
//                                SyringeItem.createMobSyringe(EntityWolf.class), SyringeItem.createMobSyringe(EntityPigZombie.class), SyringeItem.createMobSyringe(EntityMooshroom.class)},
//                        new String[][]{syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher},
//                        new ItemStack(ModItems.peaceEssenceItem)).setRegistryName(new ResourceLocation(RFTools.MODID, "syringe_peace")));
    }
}
