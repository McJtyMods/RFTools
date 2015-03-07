package com.mcjty.rftools.items.dimlets;

import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

public class DimletCosts {
    public static final Map<DimletType,Integer> typeRfCreateCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeRfMaintainCost = new HashMap<DimletType, Integer>();
    public static final Map<DimletType,Integer> typeTickCost = new HashMap<DimletType, Integer>();

    // First element in the pair is the modifier type. Second element is the type that is being modified.
    public static final Map<Pair<DimletType,DimletType>,Integer> rfCreateModifierMultiplier = new HashMap<Pair<DimletType, DimletType>, Integer>();
    public static final Map<Pair<DimletType,DimletType>,Integer> rfMaintainModifierMultiplier = new HashMap<Pair<DimletType, DimletType>, Integer>();
    public static final Map<Pair<DimletType,DimletType>,Integer> tickCostModifierMultiplier = new HashMap<Pair<DimletType, DimletType>, Integer>();

    static final Map<DimletKey,Integer> dimletBuiltinRfCreate = new HashMap<DimletKey, Integer>();
    static final Map<DimletKey,Integer> dimletBuiltinRfMaintain = new HashMap<DimletKey, Integer>();
    static final Map<DimletKey,Integer> dimletBuiltinTickCost = new HashMap<DimletKey, Integer>();

    public static int baseDimensionCreationCost = 1000;
    public static int baseDimensionMaintenanceCost = 10;
    public static int baseDimensionTickCost = 100;

    public static void initTypeRfCreateCost(Configuration cfg) {
        typeRfCreateCost.clear();
        initTypeRfCreateCost(cfg, DimletType.DIMLET_BIOME, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_TIME, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_FOLIAGE, 200);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_LIQUID, 500);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_MATERIAL, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_MOBS, 300);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_SKY, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_STRUCTURE, 600);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_TERRAIN, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_FEATURE, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_DIGIT, 0);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_EFFECT, 200);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_SPECIAL, 1000);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_CONTROLLER, 100);
        initTypeRfCreateCost(cfg, DimletType.DIMLET_WEATHER, 100);

        rfCreateModifierMultiplier.clear();
        initRfCreateModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_TERRAIN, 5);
        initRfCreateModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_FEATURE, 1);
        initRfCreateModifierMultiplier(cfg, DimletType.DIMLET_LIQUID, DimletType.DIMLET_TERRAIN, 5);
    }

    private static void initRfCreateModifierMultiplier(Configuration cfg, DimletType type1, DimletType type2, int value) {
        rfCreateModifierMultiplier.put(Pair.of(type1, type2), cfg.get(KnownDimletConfiguration.CATEGORY_TYPERFCREATECOST, "multiplier." + type1.getName() + "." + type2.getName(), value).getInt());
    }

    private static void initTypeRfCreateCost(Configuration cfg, DimletType type, int cost) {
        typeRfCreateCost.put(type, cfg.get(KnownDimletConfiguration.CATEGORY_TYPERFCREATECOST, "rfcreate." + type.getName(), cost).getInt());
    }

    public static void initTypeRfMaintainCost(Configuration cfg) {
        typeRfMaintainCost.clear();
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_BIOME, 0);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_TIME, 20);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_FOLIAGE, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_LIQUID, 300);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_MATERIAL, 10);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_MOBS, 100);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_SKY, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_STRUCTURE, 100);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_TERRAIN, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_FEATURE, 1);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_DIGIT, 0);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_EFFECT, 0);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_SPECIAL, 1000);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_CONTROLLER, 0);
        initTypeRfMaintainCost(cfg, DimletType.DIMLET_WEATHER, 50);

        rfMaintainModifierMultiplier.clear();
        initRfMaintainModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_TERRAIN, 5);
        initRfMaintainModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_FEATURE, 1);
        initRfMaintainModifierMultiplier(cfg, DimletType.DIMLET_LIQUID, DimletType.DIMLET_TERRAIN, 5);
    }

    private static void initRfMaintainModifierMultiplier(Configuration cfg, DimletType type1, DimletType type2, int value) {
        rfMaintainModifierMultiplier.put(Pair.of(type1, type2), cfg.get(KnownDimletConfiguration.CATEGORY_TYPERFMAINTAINCOST, "multiplier." + type1.getName() + "." + type2.getName(), value).getInt());
    }

    private static void initTypeRfMaintainCost(Configuration cfg, DimletType type, int cost) {
        typeRfMaintainCost.put(type, cfg.get(KnownDimletConfiguration.CATEGORY_TYPERFMAINTAINCOST, "rfmaintain." + type.getName(), cost).getInt());
    }

    public static void initTypeTickCost(Configuration cfg) {
        typeTickCost.clear();
        initTypeTickCost(cfg, DimletType.DIMLET_BIOME, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_TIME, 10);
        initTypeTickCost(cfg, DimletType.DIMLET_FOLIAGE, 10);
        initTypeTickCost(cfg, DimletType.DIMLET_LIQUID, 200);
        initTypeTickCost(cfg, DimletType.DIMLET_MATERIAL, 100);
        initTypeTickCost(cfg, DimletType.DIMLET_MOBS, 200);
        initTypeTickCost(cfg, DimletType.DIMLET_SKY, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_STRUCTURE, 900);
        initTypeTickCost(cfg, DimletType.DIMLET_TERRAIN, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_FEATURE, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_DIGIT, 0);
        initTypeTickCost(cfg, DimletType.DIMLET_EFFECT, 100);
        initTypeTickCost(cfg, DimletType.DIMLET_SPECIAL, 1000);
        initTypeTickCost(cfg, DimletType.DIMLET_CONTROLLER, 1);
        initTypeTickCost(cfg, DimletType.DIMLET_WEATHER, 10);

        tickCostModifierMultiplier.clear();
        initTickCostModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_TERRAIN, 2);
        initTickCostModifierMultiplier(cfg, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_FEATURE, 1);
        initTickCostModifierMultiplier(cfg, DimletType.DIMLET_LIQUID, DimletType.DIMLET_TERRAIN, 2);
    }

    private static void initTickCostModifierMultiplier(Configuration cfg, DimletType type1, DimletType type2, int value) {
        tickCostModifierMultiplier.put(Pair.of(type1, type2), cfg.get(KnownDimletConfiguration.CATEGORY_TYPETICKCOST, "multiplier." + type1.getName() + "." + type2.getName(), value).getInt());
    }

    private static void initTypeTickCost(Configuration cfg, DimletType type, int cost) {
        typeTickCost.put(type, cfg.get(KnownDimletConfiguration.CATEGORY_TYPETICKCOST, "ticks." + type.getName(), cost).getInt());
    }
}
