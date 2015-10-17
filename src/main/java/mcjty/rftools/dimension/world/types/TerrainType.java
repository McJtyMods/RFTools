package mcjty.rftools.dimension.world.types;

public enum TerrainType {
    TERRAIN_VOID(true, true, 0.3f, 0.3f),
    TERRAIN_FLAT(false, true, 1, 0.8f),
    TERRAIN_AMPLIFIED(false, true, 1.5f, 1),
    TERRAIN_NORMAL(false, true, 1, 1),
    TERRAIN_CAVERN_OLD(true, false, 1, 1),
    TERRAIN_ISLAND(true, true, 0.5f, 0.5f),
    TERRAIN_ISLANDS(true, true, 0.5f, 0.5f),
    TERRAIN_CHAOTIC(true, true, 0.5f, 0.5f),
    TERRAIN_PLATEAUS(true, true, 0.5f, 0.5f),
    TERRAIN_GRID(true, true, 0.4f, 0.3f),
    TERRAIN_CAVERN(true, false, 1, 1),
    TERRAIN_LOW_CAVERN(true, true, 1, 1),
    TERRAIN_FLOODED_CAVERN(true, true, 1, 1.5f),
    TERRAIN_NEARLANDS(false, true, 1, 1),
    TERRAIN_LIQUID(true, true, 0.3f, 2),
    TERRAIN_SOLID(false, true, 2, 0.6f),
    TERRAIN_WAVES(false, true, 1, 0.6f),
    TERRAIN_FILLEDWAVES(false, true, 1, 1.2f),
    TERRAIN_ROUGH(false, true, 1, 0.6f);

    private final boolean noHorizon;
    private final boolean sky;
    private final float materialCostFactor;
    private final float liquidCostFactor;

    TerrainType(boolean noHorizon, boolean sky, float materialCostFactor, float liquidCostFactor) {
        this.noHorizon = noHorizon;
        this.sky = sky;
        this.materialCostFactor = materialCostFactor;
        this.liquidCostFactor = liquidCostFactor;
    }

    public boolean hasNoHorizon() {
        return noHorizon;
    }

    public boolean hasSky() {
        return sky;
    }

    public float getLiquidCostFactor() {
        return liquidCostFactor;
    }

    public float getMaterialCostFactor() {
        return materialCostFactor;
    }
}
