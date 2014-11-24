package com.mcjty.rftools.items.dimlets;

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
    public static final String CATEGORY_GENERAL = "General";

    public static int firstDimensionId = 2;

    public static final Map<Integer,DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    public static final Map<DimletEntry,Integer> dimletToID = new HashMap<DimletEntry, Integer>();
    public static final List<Integer> dimletIds = new ArrayList<Integer>();
    public static final Map<DimletType,Integer> typeRarity = new HashMap<DimletType, Integer>();

    private static final Pattern PATTERN = Pattern.compile(".");
    private static int lastId = 0;

    public static void initGeneralConfig(Configuration cfg) {
        firstDimensionId = cfg.get(CATEGORY_GENERAL, "firstDimensionId", firstDimensionId,
                "The first dimension ID to use for RFTools dimensions").getInt();
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
                    DimletEntry dimletEntry = new DimletEntry(type, name);
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
    }

    private static void initRarity(Configuration cfg, DimletType type, int rarity) {
        typeRarity.put(type, cfg.get(CATEGORY_TYPERARIRTY, "rarity." + type.getName(), rarity).getInt());
    }

    private static void registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry, id);
        dimletIds.add(id);
        if (id > lastId) {
            lastId = id;
        }
    }

    public static void registerDimlets(Configuration cfg) {
        for (Map.Entry<Integer,DimletEntry> me : idToDimlet.entrySet()) {
            DimletEntry entry = me.getValue();
            String typeName = entry.getType().getName();
            String name = entry.getName();
            cfg.get(CATEGORY_KNOWNDIMLETS, "dimlet." + typeName + "." + name, me.getKey());
        }
    }

    public static void registerDimlet(DimletType type, String name) {
        DimletEntry entry = new DimletEntry(type, name);
        if (dimletToID.containsKey(entry)) {
            // This known dimlet is already 'known'.
            return;
        }

        registerDimletEntry(lastId + 1, entry);
    }
}
