package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.RFTools;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;

import java.util.HashMap;
import java.util.Map;

public class DimletItems {
    public static final Map<String,Item> dimletItems = new HashMap<String, Item>();

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

        initItem("terrainVoidDimlet", "terrainVoidDimletItem");
        initItem("terrainFlatDimlet", "terrainFlatDimletItem");
        initItem("terrainOverworldDimlet", "terrainOverworldDimletItem");
        initItem("terrainAmplifiedDimlet", "terrainAmplifiedDimletItem");
        initItem("biomePlainsDimlet", "biomePlainsDimletItem");
        initItem("structuresMineshaftDimlet", "structuresMineshaftDimletItem");
        initItem("structuresDungeonDimlet", "structuresDungeonDimletItem");
        initItem("structuresVillagesDimlet", "structuresVillagesDimletItem");
        initItem("foliageFlowersDimlet", "foliageFlowersDimletItem");
        initItem("foliageOakDimlet", "foliageOakDimletItem");
        initItem("materialDirtDimlet", "materialDirtDimletItem");
        initItem("materialOresDimlet", "materialOresDimletItem");
        initItem("materialStoneDimlet", "materialStoneDimletItem");
        initItem("skyNormalDimlet", "skyNormalDimletItem");
        initItem("timeDayDimlet", "timeDayDimletItem");
        initItem("timeDayNightDimlet", "timeDayNightDimletItem");
        initItem("timeNightDimlet", "timeNightDimletItem");
        initItem("liquidWaterDimlet", "liquidWaterDimletItem");
        initItem("liquidLavaDimlet", "liquidLavaDimletItem");
        initItem("mobsStandardDimlet", "mobsStandardDimletItem");
    }

    private static void initItem(String itemName, String materialName) {
        Item item = new KnownDimlet();
        item.setUnlocalizedName(itemName);
        item.setCreativeTab(RFTools.tabRfToolsDimlets);
        item.setTextureName(RFTools.MODID + ":" + materialName);
        GameRegistry.registerItem(item, itemName);
        dimletItems.put(itemName, item);
    }
}
