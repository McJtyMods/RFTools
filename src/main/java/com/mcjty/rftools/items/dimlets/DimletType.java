package com.mcjty.rftools.items.dimlets;

import java.util.HashMap;
import java.util.Map;

public enum DimletType {
    DIMLET_BIOME("biomeDimlet", "Biome", "B"),
    DIMLET_FOLIAGE("foliageDimlet", "Foliage", "F"),
    DIMLET_LIQUID("liquidDimlet", "Liquid", "L"),
    DIMLET_MATERIAL("materialDimlet", "Material", "m"),
    DIMLET_MOBS("mobsDimlet", "Mob", "M"),
    DIMLET_SKY("skyDimlet", "Sky", "s"),
    DIMLET_STRUCTURES("structuresDimlet", "Structure", "S"),
    DIMLET_TERRAIN("terrainDimlet", "Terrain", "T"),
    DIMLET_TIME("timeDimlet", "Time", "t");

    private final String textureName;
    private final String name;
    private final String opcode;

    private static final Map<String,DimletType> typeByName = new HashMap<String, DimletType>();
    private static final Map<String,DimletType> typeByOpcode = new HashMap<String, DimletType>();

    static {
        for (DimletType type : values()) {
            typeByName.put(type.getName(), type);
            typeByOpcode.put(type.getOpcode(), type);
        }
    }

    DimletType(String textureName, String name, String opcode) {
        this.textureName = textureName;
        this.name = name;
        this.opcode = opcode;
    }

    public String getName() {
        return name;
    }

    public String getTextureName() {
        return textureName;
    }

    public String getOpcode() {
        return opcode;
    }

    public static DimletType getTypeByName(String name) {
        return typeByName.get(name);
    }

    public static DimletType getTypeByOpcode(String opcode) {
        return typeByOpcode.get(opcode);
    }

}
