package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.items.dimlets.types.*;

import java.util.HashMap;
import java.util.Map;

public enum DimletType {
    DIMLET_BIOME(new BiomeDimletType()),
    DIMLET_FOLIAGE(new FoliageDimletType()),
    DIMLET_LIQUID(new LiquidDimletType()),
    DIMLET_MATERIAL(new MaterialDimletType()),
    DIMLET_MOBS(new MobDimletType()),
    DIMLET_SKY(new SkyDimletType()),
    DIMLET_STRUCTURE(new StructureDimletType()),
    DIMLET_TERRAIN(new TerrainDimletType()),
    DIMLET_FEATURE(new FeatureDimletType()),
    DIMLET_TIME(new TimeDimletType()),
    DIMLET_DIGIT(new DigitDimletType()),
    DIMLET_EFFECT(new EffectDimletType()),
    DIMLET_SPECIAL(new SpecialDimletType()),
    DIMLET_CONTROLLER(new ControllerDimletType()),
    DIMLET_WEATHER(new WeatherDimletType());

    public final IDimletType dimletType;

    private static final Map<String,DimletType> typeByName = new HashMap<String, DimletType>();
    private static final Map<String,DimletType> typeByOpcode = new HashMap<String, DimletType>();

    static {
        for (DimletType type : values()) {
            typeByName.put(type.dimletType.getName(), type);
            typeByOpcode.put(type.dimletType.getOpcode(), type);
        }
    }

    DimletType(IDimletType dimletType) {
        this.dimletType = dimletType;

    }

    public static DimletType getTypeByName(String name) {
        return typeByName.get(name);
    }

    public static DimletType getTypeByOpcode(String opcode) {
        return typeByOpcode.get(opcode);
    }
}
