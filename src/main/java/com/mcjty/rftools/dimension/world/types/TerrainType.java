package com.mcjty.rftools.dimension.world.types;

public enum TerrainType {
    TERRAIN_VOID(false, true),
    TERRAIN_FLAT(true, false),
    TERRAIN_AMPLIFIED(true, false),
    TERRAIN_NORMAL(true, false),
    TERRAIN_CAVERN(true, true),
    TERRAIN_ISLAND(false, true);

    private final boolean supportsLakes;
    private final boolean noHorizon;

    TerrainType(boolean supportsLakes, boolean noHorizon) {
        this.supportsLakes = supportsLakes; this.noHorizon = noHorizon;
    }

    public boolean supportsLakes() {
        return supportsLakes;
    }

    public boolean hasNoHorizon() {
        return noHorizon;
    }
}
