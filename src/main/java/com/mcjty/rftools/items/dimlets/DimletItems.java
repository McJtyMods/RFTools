package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.ChestGenHooks;

import java.util.*;

public class DimletItems {
    public static final Map<String,KnownDimlet> dimlets = new HashMap<String, KnownDimlet>();
    public static final List<KnownDimlet> orderedDimlets = new ArrayList<KnownDimlet>();

    private static final float RARITY_MULTIPLIER = .5f;

    public static void init() {
        initBiomeItems(BiomeManager.getBiomes(BiomeManager.BiomeType.COOL));
        initBiomeItems(BiomeManager.getBiomes(BiomeManager.BiomeType.DESERT));
        initBiomeItems(BiomeManager.getBiomes(BiomeManager.BiomeType.ICY));
        initBiomeItems(BiomeManager.getBiomes(BiomeManager.BiomeType.WARM));

        ModItems.knownDimlet = new KnownDimlet();
        ModItems.knownDimlet.setUnlocalizedName("KnownDimlet");
        ModItems.knownDimlet.setCreativeTab(RFTools.tabRfToolsDimlets);
//        knownDimlet.setTextureName(RFTools.MODID + ":unknownDimletItem");
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


    private static void initBiomeItems(Collection<BiomeManager.BiomeEntry> biomes) {
        for (BiomeManager.BiomeEntry entry : biomes) {
            String name = entry.biome.getBiomeClass().getName();
        }
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
