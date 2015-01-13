package com.mcjty.rftools.dimension.world.types;

public enum TerrainType {
    TERRAIN_VOID(true),
    TERRAIN_FLAT(false),
    TERRAIN_AMPLIFIED(false),
    TERRAIN_NORMAL(false),
    TERRAIN_CAVERN(true),
    TERRAIN_ISLAND(true),
    TERRAIN_ISLANDS(true),
    TERRAIN_CHAOTIC(true),
    TERRAIN_PLATEAUS(true);

    private final boolean noHorizon;

    TerrainType(boolean noHorizon) {
        this.noHorizon = noHorizon;
    }

    public boolean hasNoHorizon() {
        return noHorizon;
    }
}
