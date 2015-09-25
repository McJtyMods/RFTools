package mcjty.rftools.dimension.world.types;

import mcjty.rftools.items.dimlets.DimletType;

import java.util.*;

import static mcjty.rftools.dimension.world.types.TerrainType.*;

public enum FeatureType {
    FEATURE_NONE(null, null, 0, 0),
    FEATURE_CAVES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS, TERRAIN_NEARLANDS, TERRAIN_SOLID, TERRAIN_WAVES, TERRAIN_FILLEDWAVES, TERRAIN_ROUGH), null, 0, 0),
    FEATURE_RAVINES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS, TERRAIN_NEARLANDS, TERRAIN_SOLID, TERRAIN_WAVES, TERRAIN_FILLEDWAVES, TERRAIN_ROUGH), null, 0, 0),
    FEATURE_ORBS(null, new MMap(-1, DimletType.DIMLET_MATERIAL), 1, 0),
    FEATURE_OREGEN(null, new MMap(-1, DimletType.DIMLET_MATERIAL), 0, 0),
    FEATURE_LAKES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS, TERRAIN_NEARLANDS, TERRAIN_SOLID, TERRAIN_WAVES, TERRAIN_FILLEDWAVES, TERRAIN_ROUGH), new MMap(-1, DimletType.DIMLET_LIQUID), 0, 0),
    FEATURE_TENDRILS(null, new MMap(1, DimletType.DIMLET_MATERIAL), 2, 0),
    FEATURE_CANYONS(null, new MMap(1, DimletType.DIMLET_MATERIAL), 2, 0),
    FEATURE_MAZE(new TSet(TERRAIN_AMPLIFIED, TERRAIN_FLAT, TERRAIN_NORMAL, TERRAIN_NEARLANDS), null, 0, 0),
    FEATURE_LIQUIDORBS(null, new MMap(-1, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_LIQUID), 1, 1),
    FEATURE_SHALLOW_OCEAN(new TSet(TERRAIN_PLATEAUS, TERRAIN_ISLANDS, TERRAIN_ISLAND, TERRAIN_CHAOTIC), new MMap(1, DimletType.DIMLET_LIQUID), 0, 3),
    FEATURE_VOLCANOES(null, null, 0, 0),
    FEATURE_DENSE_CAVES(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_CHAOTIC, TERRAIN_FLAT, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NORMAL, TERRAIN_PLATEAUS, TERRAIN_NEARLANDS, TERRAIN_SOLID, TERRAIN_WAVES, TERRAIN_FILLEDWAVES, TERRAIN_ROUGH), null, 0, 0),
    FEATURE_HUGEORBS(null, new MMap(-1, DimletType.DIMLET_MATERIAL), 2, 0),
    FEATURE_HUGELIQUIDORBS(null, new MMap(-1, DimletType.DIMLET_MATERIAL, DimletType.DIMLET_LIQUID), 1, 2),
    FEATURE_NODIMLETBUILDINGS(null, null, 0, 0),
    FEATURE_PYRAMIDS(new TSet(TERRAIN_AMPLIFIED, TERRAIN_CAVERN, TERRAIN_CAVERN_OLD, TERRAIN_FLOODED_CAVERN, TERRAIN_FLAT, TERRAIN_CHAOTIC, TERRAIN_ISLAND, TERRAIN_ISLANDS, TERRAIN_LOW_CAVERN, TERRAIN_NEARLANDS, TERRAIN_NORMAL, TERRAIN_PLATEAUS, TERRAIN_SOLID, TERRAIN_WAVES, TERRAIN_FILLEDWAVES, TERRAIN_ROUGH),
            new MMap(-1, DimletType.DIMLET_MATERIAL), 1, 0),
    FEATURE_CLEAN(null, null, 0, 0);

    private final Set<TerrainType> supportedTerrains;
    private final Map<DimletType,Integer> supportedModifiers;
    private final int materialClass;            // A value indicating how expensive material modifiers should be for this feature (0 is cheapest, 3 is most expensive)
    private final int liquidClass;              // A value indicating how expensive liquid modifiers should be for this feature (0 is cheapest, 3 is most expensive)

    /**
     * If no terrain types are given then they are all supported.
     */
    FeatureType(Set<TerrainType> terrainTypes, Map<DimletType, Integer> modifiers, int materialClass, int liquidClass) {
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
        this.materialClass = materialClass;
        this.liquidClass = liquidClass;
    }

    public boolean isTerrainSupported(TerrainType type) {
        return supportedTerrains.isEmpty() || supportedTerrains.contains(type);
    }

    public boolean supportsAllTerrains() {
        return supportedTerrains.isEmpty();
    }

    public int getMaterialClass() {
        return materialClass;
    }

    public int getLiquidClass() {
        return liquidClass;
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
