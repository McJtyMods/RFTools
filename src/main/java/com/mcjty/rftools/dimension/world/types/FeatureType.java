package com.mcjty.rftools.dimension.world.types;

import com.mcjty.rftools.items.dimlets.DimletType;

import java.util.*;

import static com.mcjty.rftools.dimension.world.types.TerrainType.*;

public enum FeatureType {
    FEATURE_NONE(null, null),
    FEATURE_CAVES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS), null),
    FEATURE_RAVINES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS), null),
    FEATURE_ORBS(null, new MMap(1, DimletType.DIMLET_MATERIAL)),
    FEATURE_OREGEN(null, new MMap(-1, DimletType.DIMLET_MATERIAL)),
    FEATURE_LAKES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS), new MMap(-1, DimletType.DIMLET_LIQUID)),
    FEATURE_TENDRILS(null, new MMap(1, DimletType.DIMLET_MATERIAL)),
    FEATURE_CANYONS(null, new MMap(1, DimletType.DIMLET_MATERIAL)),
    FEATURE_MAZE(new TSet(TERRAIN_AMPLIFIED, TERRAIN_FLAT, TERRAIN_NORMAL), null),
    FEATURE_LIQUIDORBS(null, new MMap(1, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_LIQUID)),
    FEATURE_SHALLOW_OCEAN(new TSet(TERRAIN_PLATEAUS, TERRAIN_ISLANDS, TERRAIN_ISLAND, TERRAIN_CHAOTIC), new MMap(1, DimletType.DIMLET_LIQUID)),
    FEATURE_VOLCANOES(null, null);

    private final Set<TerrainType> supportedTerrains;
    private final Map<DimletType,Integer> supportedModifiers;

    /**
     * If no terrain types are given then they are all supported.
     */
    FeatureType(Set<TerrainType> terrainTypes, Map<DimletType,Integer> modifiers) {
        if (terrainTypes == null) {
            supportedTerrains = Collections.emptySet();
        } else {
            supportedTerrains = new HashSet<TerrainType>(terrainTypes);
        }
        if (modifiers == null) {
            supportedModifiers = Collections.emptyMap();
        } else {
            supportedModifiers = new HashMap<DimletType, Integer>(modifiers);
        }
    }

    public boolean isTerrainSupported(TerrainType type) {
        return supportedTerrains.isEmpty() || supportedTerrains.contains(type);
    }

    public boolean supportsAllTerrains() {
        return supportedTerrains.isEmpty();
    }

    public Integer getSupportedModifierAmount(DimletType type) {
        return supportedModifiers.get(type);
    }

    private static class TSet extends HashSet<TerrainType> {
        private TSet(TerrainType... terrainTypes) {
            for (TerrainType type : terrainTypes) {
                add(type);
            }
        }
    }

    private static class MMap extends HashMap<DimletType,Integer> {
        private MMap(int amount, DimletType... dimletTypes) {
            for (DimletType type : dimletTypes) {
                put(type, amount);
            }
        }
    }
}
