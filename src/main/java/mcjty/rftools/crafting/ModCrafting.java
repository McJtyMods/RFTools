package mcjty.rftools.crafting;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.SyringeItem;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public final class ModCrafting {
    public static void init() {
        initBaseCrafting();

        EnvironmentalSetup.initCrafting();
    }

    private static void initBaseCrafting() {

        String[] syringeMatcher = new String[]{"level", "mobId"};

        ForgeRegistries.RECIPES.register(
                new NBTMatchingRecipe(3, 3,
                        new ItemStack[]{SyringeItem.createMobSyringe(EntityIronGolem.class), SyringeItem.createMobSyringe(EntityEnderman.class), SyringeItem.createMobSyringe(EntitySnowman.class),
                                SyringeItem.createMobSyringe(EntityBat.class), SyringeItem.createMobSyringe(EntityOcelot.class), SyringeItem.createMobSyringe(EntityGuardian.class),
                                SyringeItem.createMobSyringe(EntityWolf.class), SyringeItem.createMobSyringe(EntityPigZombie.class), SyringeItem.createMobSyringe(EntityMooshroom.class)},
                        new String[][]{syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher, syringeMatcher},
                        new ItemStack(ModItems.peaceEssenceItem)).setRegistryName(new ResourceLocation(RFTools.MODID, "syringe")));
    }
}
