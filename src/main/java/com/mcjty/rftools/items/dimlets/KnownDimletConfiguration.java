package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.dimension.FeatureType;
import com.mcjty.rftools.dimension.TerrainType;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class KnownDimletConfiguration {
    public static final String CATEGORY_KNOWNDIMLETS = "KnownDimlets";
    public static final String CATEGORY_TYPERARIRTY = "TypeRarity";
    public static final String CATEGORY_TYPERFCREATECOST = "TypeRfCreateCost";
    public static final String CATEGORY_TYPERFMAINTAINCOST = "TypeRfMaintainCost";
    public static final String CATEGORY_TYPETICKCOST = "TypeTickCost";
    public static final String CATEGORY_GENERAL = "General";

    public static final Map<Integer,DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    public static final Map<DimletEntry,Integer> dimletToID = new HashMap<DimletEntry, Integer>();
    public static final List<Integer> dimletIds = new ArrayList<Integer>();

    public static final Map<DimletType,Integer> typeRarity = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfCreateCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfMaintainCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeTickCost = new HashMap<DimletType, Integer>();

    public static final Map<Integer,TerrainType> idToTerrainType = new HashMap<Integer, TerrainType>();
    public static final Map<Integer,FeatureType> idToFeatureType = new HashMap<Integer, FeatureType>();

    public static int baseDimensionCreationCost = 1000;
    public static int baseDimensionMaintenanceCost = 10;
    public static int baseDimensionTickCost = 100;

    private static final Pattern PATTERN = Pattern.compile(".");
    private static int lastId = 0;

    public static void initGeneralConfig(Configuration cfg) {
        baseDimensionCreationCost = cfg.get(CATEGORY_GENERAL, "baseDimensionCreationCost", baseDimensionCreationCost,
                "The base cost (in RF/tick) for creating a dimension").getInt();
        baseDimensionMaintenanceCost = cfg.get(CATEGORY_GENERAL, "baseDimensionMaintenanceCost", baseDimensionMaintenanceCost,
                "The base cost (in RF/tick) for maintaining a dimension").getInt();
        baseDimensionTickCost = cfg.get(CATEGORY_GENERAL, "baseDimensionTickCost", baseDimensionTickCost,
                "The base time (in ticks) for creating a dimension").getInt();
    }

    public static void initKnownDimlets(Configuration cfg) {
        ConfigCategory configCategory = cfg.getCategory(CATEGORY_KNOWNDIMLETS);
        for (Map.Entry<String,Property> me : configCategory.entrySet()) {
            String key = me.getKey();
            String[] keys = PATTERN.split(key);
            if (keys.length > 2 && "dimlet".equals(keys[0])) {
                String typeName = keys[1];
                String name = keys[2];
                int id = me.getValue().getInt();
                DimletType type = DimletType.getTypeByName(typeName);
                if (type != null) {
                    int rfCreate = -1, rfMaintain = -1, tickCost = -1;
                    String k = "rfcreate." + typeName + "." + name;
                    if (configCategory.keySet().contains(k)) {
                        rfCreate = configCategory.get(k).getInt();
                    }
                    k = "rfmaintain." + typeName + "." + name;
                    if (configCategory.keySet().contains(k)) {
                        rfMaintain = configCategory.get(k).getInt();
                    }
                    k = "ticks." + typeName + "." + name;
                    if (configCategory.keySet().contains(k)) {
                        tickCost = configCategory.get(k).getInt();
                    }

                    DimletEntry dimletEntry = new DimletEntry(type, name, rfCreate, rfMaintain, tickCost);
                    registerDimletEntry(id, dimletEntry);
                }
            }
        }
    }

    public static void initTypeRarity(Configuration cfg) {
        typeRarity.clear();
        initRarity(cfg, DimletType.DIMLET_BIOME, 3);
        initRarity(cfg, DimletType.DIMLET_TIME, 1);
        initRarity(cfg, DimletType.DIMLET_FOLIAGE, 2);
        initRarity(cfg, DimletType.DIMLET_LIQUID, 1);
        initRarity(cfg, DimletType.DIMLET_MATERIAL, 2);
        initRarity(cfg, DimletType.DIMLET_MOBS, 1);
        initRarity(cfg, DimletType.DIMLET_SKY, 1);
        initRarity(cfg, DimletType.DIMLET_STRUCTURES, 1);
        initRarity(cfg, DimletType.DIMLET_TERRAIN, 1);
        initRarity(cfg, DimletType.DIMLET_FEATURE, 1);
    }

    private static void initRarity(Configuration cfg, DimletType type, int rarity) {
        typeRarity.put(type, cfg.get(CATEGORY_TYPERARIRTY, "rarity." + type.getName(), rarity).getInt());
    }

    public static void initTypeRfCreateCost(Configuration cfg) {
        typeRfCreateCost.clear();
        initTypeRfCreateCost(cfg, DimletType.DIMLET_BIOME, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_TIME, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_FOLIAGE, 200);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_LIQUID, 150);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_MATERIAL, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_MOBS, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_SKY, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_STRUCTURES, 600);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_TERRAIN, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_FEATURE, 100);
    }

    private static void initTypeRfCreateCost(Configuration cfg, DimletType type, int cost) {
        typeRfCreateCost.put(type, cfg.get(CATEGORY_TYPERFCREATECOST, "rfcreate." + type.getName(), cost).getInt());
    }

    public static void initTypeRfMaintainCost(Configuration cfg) {
        typeRfMaintainCost.clear();
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_BIOME, 0);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_TIME, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_FOLIAGE, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_LIQUID, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_MATERIAL, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_MOBS, 100);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_SKY, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_STRUCTURES, 100);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_TERRAIN, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_FEATURE, 1);
    }

    private static void initTypeRfMaintainCost(Configuration cfg, DimletType type, int cost) {
        typeRfMaintainCost.put(type, cfg.get(CATEGORY_TYPERFMAINTAINCOST, "rfmaintain." + type.getName(), cost).getInt());
    }

    public static void initTypeTickCost(Configuration cfg) {
        typeTickCost.clear();
        initTypeTickCost(cfg, DimletType.DIMLET_BIOME, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_TIME, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_FOLIAGE, 10);
        initTypeTickCost(cfg, DimletType.DIMLET_LIQUID, 10);
        initTypeTickCost(cfg, DimletType.DIMLET_MATERIAL, 100);
        initTypeTickCost(cfg, DimletType.DIMLET_MOBS, 200);
        initTypeTickCost(cfg, DimletType.DIMLET_SKY, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_STRUCTURES, 900);
        initTypeTickCost(cfg, DimletType.DIMLET_TERRAIN, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_FEATURE, 1);
    }

    private static void initTypeTickCost(Configuration cfg, DimletType type, int cost) {
        typeTickCost.put(type, cfg.get(CATEGORY_TYPETICKCOST, "ticks." + type.getName(), cost).getInt());
    }

    private static int registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry, id);
        dimletIds.add(id);
        if (id > lastId) {
            lastId = id;
        }
        return id;
    }

    public static void registerDimlets(Configuration cfg) {
        for (Map.Entry<Integer,DimletEntry> me : idToDimlet.entrySet()) {
            DimletEntry entry = me.getValue();
            String typeName = entry.getType().getName();
            String name = entry.getName();
            cfg.get(CATEGORY_KNOWNDIMLETS, "dimlet." + typeName + "." + name, me.getKey());
            if (entry.getRfCreateCost() != -1) {
                cfg.get(CATEGORY_KNOWNDIMLETS, "rfcreate." + typeName + "." + name, entry.getRfCreateCost());
            }
            if (entry.getRfMaintainCost() != -1) {
                cfg.get(CATEGORY_KNOWNDIMLETS, "rfmaintain." + typeName + "." + name, entry.getRfMaintainCost());
            }
            if (entry.getTickCost() != -1) {
                cfg.get(CATEGORY_KNOWNDIMLETS, "ticks." + typeName + "." + name, entry.getTickCost());
            }
        }
    }

    public static int registerDimlet(DimletType type, String name, int rfCreateCost, int rfMaintainCost, int tickCost) {
        DimletEntry entry = new DimletEntry(type, name, rfCreateCost, rfMaintainCost, tickCost);
        if (dimletToID.containsKey(entry)) {
            // This known dimlet is already 'known'.
            return dimletToID.get(entry);
        }

        return registerDimletEntry(lastId + 1, entry);
    }
}
