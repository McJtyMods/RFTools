package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.*;

public class DimletItems {
    public static void init() {

        initBiomeItems();

        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MATERIAL, Blocks.diamond_block.getLocalizedName(), 2000, 2000, 1000);
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MATERIAL, Blocks.diamond_ore.getLocalizedName(), 1700, 1700, 800);
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MATERIAL, Blocks.gold_block.getLocalizedName(), 1000, 1000, 500);
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MATERIAL, Blocks.gold_ore.getLocalizedName(), 800, 800, 400);

        initFoliageItem();

        initLiquidItems();

        initMobItem(EntityZombie.class, "Zombie");
        initMobItem(EntitySkeleton.class, "Skeleton");

        initSkyItem("Clear");
        initSkyItem("Bright");

        int idStructureNone = initStructureItem("None", StructureType.STRUCTURE_NONE);
        initStructureItem("Village", StructureType.STRUCTURE_VILLAGE);
        initStructureItem("Stronghold", StructureType.STRUCTURE_STRONGHOLD);
        initStructureItem("Dungeon", StructureType.STRUCTURE_DUNGEON);
        initStructureItem("Fortress", StructureType.STRUCTURE_FORTRESS);
        initStructureItem("Mineshaft", StructureType.STRUCTURE_MINESHAFT);
        initStructureItem("Scattered", StructureType.STRUCTURE_SCATTERED);

        int idTerrainVoid = initTerrainItem("Void", TerrainType.TERRAIN_VOID);
        initTerrainItem("Flat", TerrainType.TERRAIN_FLAT);
        initTerrainItem("Amplified", TerrainType.TERRAIN_AMPLIFIED);
        initTerrainItem("Normal", TerrainType.TERRAIN_NORMAL);
        initTerrainItem("Cave World", TerrainType.TERRAIN_CAVES);
        initTerrainItem("Island", TerrainType.TERRAIN_ISLAND);
        initTerrainItem("Spheres", TerrainType.TERRAIN_SPHERES);

        int idFeatureNone = initFeatureItem("None", FeatureType.FEATURE_NONE);
        initFeatureItem("Caves", FeatureType.FEATURE_CAVES);
        initFeatureItem("Ravines", FeatureType.FEATURE_RAVINES);

        initTimeItem("Day");
        initTimeItem("Night");
        initTimeItem("Day/Night");

        ModItems.knownDimlet = new KnownDimlet();
        ModItems.knownDimlet.setUnlocalizedName("KnownDimlet");
        ModItems.knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(ModItems.knownDimlet, "knownDimlet");

        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idFeatureNone), new Object[] { " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.string, 'p', Items.paper } );
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idStructureNone), new Object[] { " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.bone, 'p', Items.paper } );
        GameRegistry.addRecipe(new ItemStack(ModItems.knownDimlet, 1, idTerrainVoid), new Object[] { " r ", "rwr", "ppp", 'r', Items.redstone, 'w', Items.brick, 'p', Items.paper } );

        setupChestLoot();
    }

    private static void setupChestLoot() {
        setupChestLoot(ChestGenHooks.DUNGEON_CHEST);
        setupChestLoot(ChestGenHooks.MINESHAFT_CORRIDOR);
        setupChestLoot(ChestGenHooks.PYRAMID_DESERT_CHEST);
        setupChestLoot(ChestGenHooks.PYRAMID_JUNGLE_CHEST);
        setupChestLoot(ChestGenHooks.STRONGHOLD_CORRIDOR);
        setupChestLoot(ChestGenHooks.VILLAGE_BLACKSMITH);
    }


    private static void setupChestLoot(String category) {
        ChestGenHooks chest = ChestGenHooks.getInfo(category);
        chest.addItem(new WeightedRandomChestContent(ModItems.unknownDimlet, 0, 1, 3, 50));
    }

    private static void initBiomeItems() {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                String name = biome.biomeName;
                int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_BIOME, name, -1, -1, -1);
                KnownDimletConfiguration.idToBiome.put(id, name);
            }
        }
    }

    private static void initFoliageItem() {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_FOLIAGE, "Oak", -1, -1, -1);
    }

    private static void initLiquidItems() {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_LIQUID, me.getKey(), -1, -1, -1);
        }
    }

    private static int initMobItem(Class <? extends EntityLiving> entity, String name) {
        return KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MOBS, name, -1, -1, -1);
    }

    private static int initSkyItem(String name) {
        return KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_SKY, name, -1, -1, -1);
    }

    private static int initStructureItem(String name, StructureType structureType) {
        int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_STRUCTURE, name, -1, -1, -1);
        KnownDimletConfiguration.idToStructureType.put(id, structureType);
        return id;
    }

    private static int initTerrainItem(String name, TerrainType terrainType) {
        int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_TERRAIN, name, -1, -1, -1);
        KnownDimletConfiguration.idToTerrainType.put(id, terrainType);
        return id;
    }

    private static int initFeatureItem(String name, FeatureType featureType) {
        int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_FEATURE, name, -1, -1, -1);
        KnownDimletConfiguration.idToFeatureType.put(id, featureType);
        return id;
    }

    private static int initTimeItem(String name) {
        return KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_TIME, name, -1, -1, -1);
    }
}
