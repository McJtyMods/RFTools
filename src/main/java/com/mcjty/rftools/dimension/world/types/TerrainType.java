package com.mcjty.rftools.dimension.world.types;

public enum TerrainType {
    TERRAIN_VOID(true, true),
    TERRAIN_FLAT(false, true),
    TERRAIN_AMPLIFIED(false, true),
    TERRAIN_NORMAL(false, true),
    TERRAIN_CAVERN_OLD(true, false),
    TERRAIN_ISLAND(true, true),
    TERRAIN_ISLANDS(true, true),
    TERRAIN_CHAOTIC(true, true),
    TERRAIN_PLATEAUS(true, true),
    TERRAIN_GRID(true, true),
    TERRAIN_CAVERN(true, false),
    TERRAIN_LOW_CAVERN(true, true),
    TERRAIN_FLOODED_CAVERN(true, true);

    private final boolean noHorizon;
    private final boolean sky;

    TerrainType(boolean noHorizon, boolean sky) {
        this.noHorizon = noHorizon;
        this.sky = sky;
    }

    public boolean hasNoHorizon() {
        return noHorizon;
    }

    public boolean hasSky() {
        return sky;
    }
}
