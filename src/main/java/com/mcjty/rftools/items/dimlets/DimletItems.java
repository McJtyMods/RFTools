package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
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

        initMaterialItem(Blocks.diamond_block);
        initMaterialItem(Blocks.gold_block);
        initMaterialItem(Blocks.gold_ore);

        initFoliageItem();

        initLiquidItems();

        initMobItem(EntityZombie.class, "Zombie");
        initMobItem(EntitySkeleton.class, "Skeleton");

        initSkyItem("Clear");
        initSkyItem("Bright");

        initStructureItem("Village");
        initStructureItem("Stronghold");
        initStructureItem("Dungeon");
        initStructureItem("Fortress");

        initTerrainItem("Flat");
        initTerrainItem("Void");
        initTerrainItem("Amplified");
        initTerrainItem("Normal");
        initTerrainItem("Cave World");
        initTerrainItem("Island");
        initTerrainItem("Spheres");

        initTimeItem("Day");
        initTimeItem("Night");
        initTerrainItem("Day/Night");

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
            KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_BIOME, name);
        }
    }

    private static void initMaterialItem(Block block) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MATERIAL, block.getLocalizedName());
    }

    private static void initFoliageItem() {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_FOLIAGE, "Oak");
    }

    private static void initLiquidItems() {
        Map<String,Fluid> fluidMap = FluidRegistry.getRegisteredFluids();
        for (Map.Entry<String,Fluid> me : fluidMap.entrySet()) {
            KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_LIQUID, me.getKey());
        }
    }

    private static void initMobItem(Class <? extends EntityLiving> entity, String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_MOBS, name);
    }

    private static void initSkyItem(String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_SKY, name);
    }

    private static void initStructureItem(String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_STRUCTURES, name);
    }

    private static void initTerrainItem(String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_TERRAIN, name);
    }

    private static void initTimeItem(String name) {
        KnownDimletConfiguration.registerDimlet(DimletType.DIMLET_TIME, name);
    }

    /**
     * This main function tests rarity distribution of the dimlets.
     * @param args
     */
    public static void main(String[] args) {
//        init();
//        Map<String,Integer> counter = new HashMap<String, Integer>();
//        for (KnownDimlet dimlet : orderedDimlets) {
//            counter.put(dimlet.getUnlocalizedName(), 0);
//        }
//        int total = 1000000;
//        Random random = new Random();
//        for (int i = 0 ; i < total ; i++) {
//            for (KnownDimlet dimlet : orderedDimlets) {
//                if (random.nextFloat() < dimlet.getRarity()) {
//                    counter.put(dimlet.getUnlocalizedName(), counter.get(dimlet.getUnlocalizedName())+1);
//                    break;
//                }
//            }
//        }
//        for (KnownDimlet dimlet : orderedDimlets) {
//            int count = counter.get(dimlet.getUnlocalizedName());
//            float percentage = count * 100.0f / total;
//            System.out.println("ME: " + dimlet.getUnlocalizedName() + ", " + percentage);
//        }
    }
}
