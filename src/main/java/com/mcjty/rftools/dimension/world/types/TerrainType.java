package com.mcjty.rftools.dimension.world.types;

public enum TerrainType {
    TERRAIN_VOID(false),
    TERRAIN_FLAT(true),
    TERRAIN_AMPLIFIED(true),
    TERRAIN_NORMAL(true),
    TERRAIN_CAVES(true),
    TERRAIN_ISLAND(false),
    TERRAIN_SPHERES(false);

    private final boolean supportsLakes;

    TerrainType(boolean supportsLakes) {
        this.supportsLakes = supportsLakes;
    }

    public boolean supportsLakes() {
        return supportsLakes;
    }
}
