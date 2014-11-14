package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.items.ModItems;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraftforge.common.ChestGenHooks;

import java.util.*;

public class DimletItems {
    public static final Map<String,KnownDimlet> dimlets = new HashMap<String, KnownDimlet>();
    public static final List<KnownDimlet> orderedDimlets = new ArrayList<KnownDimlet>();

    private static final float RARITY_MULTIPLIER = .5f;

    public static void init() {
        /*
         * Dimlets:
         *
         * - Unknown Dimlet: not craftable, researching yields a random dimlet
         * - Craftable dimlets:
         *      - Terrain Void Dimlet
         *      - Terrain Flat Dimlet
         *      - Sky Normal Dimlet
         *      - Biome Plains Dimlet
         *      - Material Dirt Dimlet
         *      - Material Stone Dimlet
         *      - Time Normal day/night
         *      - Foliage Oak
         *      - Liquid Water
         * - Uncraftable:
         *      - Terrain Overworld
         *      - Terrain Amplified
         *      - Time Constant Daylight
         *      - Time Constant Night
         *      - Material Ore Generation Dimlet
         *      - Structures Dungeon Dimlet
         *      - Structures Mineshaft Dimlet
         *      - Structures Villages Dimlet
         *      - Foliage Flowers
         *      - Mobs Standard
         *      - Liquid Lava
         */

        // Roughly ordered by relative rarity.
        initItem("terrainVoidDimlet", "terrainVoidDimletItem", 0.1f);
        initItem("terrainFlatDimlet", "terrainFlatDimletItem", 0.1f);
        initItem("terrainOverworldDimlet", "terrainOverworldDimletItem", 0.1f);
        initItem("terrainAmplifiedDimlet", "terrainAmplifiedDimletItem", 0.1f);
        initItem("biomePlainsDimlet", "biomePlainsDimletItem", 0.1f);
        initItem("foliageFlowersDimlet", "foliageFlowersDimletItem", 0.1f);
        initItem("foliageOakDimlet", "foliageOakDimletItem", 0.1f);
        initItem("materialDirtDimlet", "materialDirtDimletItem", 0.1f);
        initItem("materialStoneDimlet", "materialStoneDimletItem", 0.1f);
        initItem("skyNormalDimlet", "skyNormalDimletItem", 0.1f);
        initItem("timeDayNightDimlet", "timeDayNightDimletItem", 0.1f);
        initItem("timeNightDimlet", "timeNightDimletItem", 0.1f);
        initItem("liquidWaterDimlet", "liquidWaterDimletItem", 0.1f);
        initItem("timeDayDimlet", "timeDayDimletItem", 0.06f);
        initItem("liquidLavaDimlet", "liquidLavaDimletItem", 0.05f);
        initItem("mobsStandardDimlet", "mobsStandardDimletItem", 0.04f);
        initItem("materialOresDimlet", "materialOresDimletItem", 0.03f);
        initItem("structuresMineshaftDimlet", "structuresMineshaftDimletItem", 0.03f);
        initItem("structuresDungeonDimlet", "structuresDungeonDimletItem", 0.03f);
        initItem("structuresVillagesDimlet", "structuresVillagesDimletItem", 0.03f);

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

    private static void initItem(String itemName, String materialName, float rarity) {
        KnownDimlet item = new KnownDimlet();
        item.setUnlocalizedName(itemName);
        item.setCreativeTab(RFTools.tabRfToolsDimlets);
        item.setTextureName(RFTools.MODID + ":dimlets/" + materialName);
        item.setRarity(rarity * RARITY_MULTIPLIER);
        GameRegistry.registerItem(item, itemName);
        dimlets.put(itemName, item);
        orderedDimlets.add(item);
    }

    /**
     * This main function tests rarity distribution of the dimlets.
     * @param args
     */
    public static void main(String[] args) {
        init();
        Map<String,Integer> counter = new HashMap<String, Integer>();
        for (KnownDimlet dimlet : orderedDimlets) {
            counter.put(dimlet.getUnlocalizedName(), 0);
        }
        int total = 1000000;
        Random random = new Random();
        for (int i = 0 ; i < total ; i++) {
            for (KnownDimlet dimlet : orderedDimlets) {
                if (random.nextFloat() < dimlet.getRarity()) {
                    counter.put(dimlet.getUnlocalizedName(), counter.get(dimlet.getUnlocalizedName())+1);
                    break;
                }
            }
        }
        for (KnownDimlet dimlet : orderedDimlets) {
            int count = counter.get(dimlet.getUnlocalizedName());
            float percentage = count * 100.0f / total;
            System.out.println("ME: " + dimlet.getUnlocalizedName() + ", " + percentage);
        }
    }
}
