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
import net.minecraft.util.WeightedRandomChestContent;
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

        initStructureItem("None", StructureType.STRUCTURE_NONE);
        initStructureItem("Village", StructureType.STRUCTURE_VILLAGE);
        initStructureItem("Stronghold", StructureType.STRUCTURE_STRONGHOLD);
        initStructureItem("Dungeon", StructureType.STRUCTURE_DUNGEON);
        initStructureItem("Fortress", StructureType.STRUCTURE_FORTRESS);
        initStructureItem("Mineshaft", StructureType.STRUCTURE_MINESHAFT);
        initStructureItem("Scattered", StructureType.STRUCTURE_SCATTERED);

        initTerrainItem("Flat", TerrainType.TERRAIN_FLAT);
        initTerrainItem("Void", TerrainType.TERRAIN_VOID);
        initTerrainItem("Amplified", TerrainType.TERRAIN_AMPLIFIED);
        initTerrainItem("Normal", TerrainType.TERRAIN_NORMAL);
        initTerrainItem("Cave World", TerrainType.TERRAIN_CAVES);
        initTerrainItem("Island", TerrainType.TERRAIN_ISLAND);
        initTerrainItem("Spheres", TerrainType.TERRAIN_SPHERES);

        initFeatureItem("None", FeatureType.FEATURE_NONE);
        initFeatureItem("Caves", FeatureType.FEATURE_CAVES);
        initFeatureItem("Ravines", FeatureType.FEATURE_RAVINES);

        initTimeItem("Day");
        initTimeItem("Night");
        initTimeItem("Day/Night");

        ModItems.knownDimlet = new KnownDimlet();
        ModItems.knownDimlet.setUnlocalizedName("KnownDimlet");
        ModItems.knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
        GameRegistry.registerItem(ModItems.knownDimlet, "knownDimlet");

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
        Map<String,BiomeManager.BiomeEntry> biomes = new HashMap<String, BiomeManager.BiomeEntry>();
        addBiomes(biomes, BiomeManager.getBiomes(BiomeManager.BiomeType.COOL));
        addBiomes(biomes, BiomeManager.getBiomes(BiomeManager.BiomeType.DESERT));
        addBiomes(biomes, BiomeManager.getBiomes(BiomeManager.BiomeType.ICY));
        addBiomes(biomes, BiomeManager.getBiomes(BiomeManager.BiomeType.WARM));

    }

    private static void addBiomes(Map<String,BiomeManager.BiomeEntry> biomes, Collection<BiomeManager.BiomeEntry> list) {
        if (list == null) {
            return;
        }
        for (BiomeManager.BiomeEntry entry : list) {
            if (!biomes.containsKey(entry.biome.biomeName)) {
                biomes.put(entry.biome.biomeName, entry);
            }
        }
        for (String name : biomes.keySet()) {
            int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_BIOME, name, -1, -1, -1);
            KnownDimletConfiguration.idToBiome.put(id, name);
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

    private static void initMobItem(Class <? extends EntityLiving> entity, String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MOBS, name, -1, -1, -1);
    }

    private static void initSkyItem(String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_SKY, name, -1, -1, -1);
    }

    private static void initStructureItem(String name, StructureType structureType) {
        int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_STRUCTURE, name, -1, -1, -1);
        KnownDimletConfiguration.idToStructureType.put(id, structureType);
    }

    private static void initTerrainItem(String name, TerrainType terrainType) {
        int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_TERRAIN, name, -1, -1, -1);
        KnownDimletConfiguration.idToTerrainType.put(id, terrainType);
    }

    private static void initFeatureItem(String name, FeatureType featureType) {
        int id = KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_FEATURE, name, -1, -1, -1);
        KnownDimletConfiguration.idToFeatureType.put(id, featureType);
    }

    private static void initTimeItem(String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_TIME, name, -1, -1, -1);
    }
}
