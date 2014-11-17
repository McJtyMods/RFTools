package com.mcjty.rftools.items.dimlets;

public enum DimletType {
    DIMLET_BIOME("biomeDimlet", "Biome", 0),
    DIMLET_FOLIAGE("foliageDimlet", "Foliage", 1000),
    DIMLET_LIQUID("liquidDimlet", "Liquid", 2000),
    DIMLET_MATERIAL("materialDimlet", "Material", 3000),
    DIMLET_MOBS("mobsDimlet", "Mob", 4000),
    DIMLET_SKY("skyDimlet", "Sky", 5000),
    DIMLET_STRUCTURES("structuresDimlet", "Structure", 6000),
    DIMLET_TERRAIN("terrainDimlet", "Terrain", 7000),
    DIMLET_TIME("timeDimlet", "Time", 8000);

    private final String textureName;
    private final String name;
    private final int idOffset;

    DimletType(String textureName, String name, int idOffset) {
        this.textureName = textureName;
        this.name = name;
        this.idOffset = idOffset;
    }

    public String getName() {
        return name;
    }

    public String getTextureName() {
        return textureName;
    }

    public int getIdOffset() {
        return idOffset;
    }
}
