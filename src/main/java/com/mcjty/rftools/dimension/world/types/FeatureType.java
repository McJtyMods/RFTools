package com.mcjty.rftools.dimension.world.types;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.mcjty.rftools.dimension.world.types.TerrainType.*;

public enum FeatureType {
    FEATURE_NONE,
    FEATURE_CAVES(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS),
    FEATURE_RAVINES(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS),
    FEATURE_ORBS,
    FEATURE_OREGEN,
    FEATURE_LAKES(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS),
    FEATURE_TENDRILS,
    FEATURE_CANYONS,
    FEATURE_MAZE(TERRAIN_AMPLIFIED, TERRAIN_FLAT, TERRAIN_NORMAL),
    FEATURE_LIQUIDORBS,
    FEATURE_SHALLOW_OCEAN(TERRAIN_PLATEAUS, TERRAIN_ISLANDS, TERRAIN_ISLAND, TERRAIN_CHAOTIC),
    FEATURE_VOLCANOES;

    private final Set<TerrainType> supportedTerrains;

    /**
     * If no terrain types are given then they are all supported.
     */
    FeatureType(TerrainType... terrainTypes) {
        supportedTerrains = new HashSet<TerrainType>();
        Collections.addAll(supportedTerrains, terrainTypes);
    }

    public boolean isTerrainSupported(TerrainType type) {
        return supportedTerrains.isEmpty() || supportedTerrains.contains(type);
    }
}
