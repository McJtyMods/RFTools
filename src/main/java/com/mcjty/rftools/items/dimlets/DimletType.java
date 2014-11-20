package com.mcjty.rftools.items.dimlets;

import java.util.HashMap;
import java.util.Map;

public enum DimletType {
    DIMLET_BIOME("biomeDimlet", "Biome"),
    DIMLET_FOLIAGE("foliageDimlet", "Foliage"),
    DIMLET_LIQUID("liquidDimlet", "Liquid"),
    DIMLET_MATERIAL("materialDimlet", "Material"),
    DIMLET_MOBS("mobsDimlet", "Mob"),
    DIMLET_SKY("skyDimlet", "Sky"),
    DIMLET_STRUCTURES("structuresDimlet", "Structure"),
    DIMLET_TERRAIN("terrainDimlet", "Terrain"),
    DIMLET_TIME("timeDimlet", "Time");

    private final String textureName;
    private final String name;

    private static final Map<String,DimletType> typeByName = new HashMap<String, DimletType>();

    static {
        for (DimletType type : values()) {
            typeByName.put(type.getName(), type);
        }
    }

    DimletType(String textureName, String name) {
        this.textureName = textureName;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getTextureName() {
        return textureName;
    }

    public static DimletType getTypeByName(String name) {
        return typeByName.get(name);
    }

}
