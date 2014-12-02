package com.mcjty.rftools.dimension;

import com.google.common.collect.ImmutableList;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraftforge.common.BiomeManager;

import java.util.*;

public class DimensionInformation {
    private final DimensionDescriptor descriptor;
    private final String name;

    private TerrainType terrainType;
    private Set<FeatureType> featureTypes = new HashSet<FeatureType>();
    private Set<StructureType> structureTypes = new HashSet<StructureType>();
    private List<BiomeManager.BiomeEntry> biomes = new ArrayList<BiomeManager.BiomeEntry>();

    public DimensionInformation(String name, DimensionDescriptor descriptor) {
        this.name = name;
        this.descriptor = descriptor;

        Map<DimletType,List<Integer>> dimlets = descriptor.getDimlets();
        Random random = getRandom(dimlets);
        calculateTerrainType(dimlets, random);
        calculateFeatureType(dimlets, random);
        calculateStructureType(dimlets, random);
        calculateBiomes(dimlets, random);
    }

    private Random getRandom(Map<DimletType, List<Integer>> dimlets) {
        int seed = 1;
        for (DimletType type : DimletType.values()) {
            for (Integer id : dimlets.get(type)) {
                seed = 31 * seed + id;
            }
        }
        return new Random(seed);
    }

    private void calculateTerrainType(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_TERRAIN);
        terrainType = TerrainType.TERRAIN_VOID;
        if (list.isEmpty()) {
            // Pick a random terrain type with a seed that is generated from all the
            // dimlets so we always get the same random value for these dimlets.
            terrainType = TerrainType.values()[random.nextInt(TerrainType.values().length)];
        } else {
            terrainType = KnownDimletConfiguration.idToTerrainType.get(list.get(random.nextInt(list.size())));
        }
    }

    private void calculateFeatureType(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_FEATURE);
        if (list.isEmpty()) {
            for (FeatureType type : FeatureType.values()) {
                if (random.nextBoolean()) {
                    featureTypes.add(type);
                }
            }
        } else {
            for (Integer id : list) {
                featureTypes.add(KnownDimletConfiguration.idToFeatureType.get(id));
            }
        }
    }

    private void calculateStructureType(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_STRUCTURE);
        if (list.isEmpty()) {
            for (StructureType type : StructureType.values()) {
                if (random.nextBoolean()) {
                    structureTypes.add(type);
                }
            }
        } else {
            for (Integer id : list) {
                structureTypes.add(KnownDimletConfiguration.idToStructureType.get(id));
            }
        }
    }

    private BiomeManager.BiomeEntry findBiomeEntry(String name, BiomeManager.BiomeType type) {
        ImmutableList<BiomeManager.BiomeEntry> biomeList = BiomeManager.getBiomes(type);
        if (biomeList == null) {
            return null;
        }
        for (BiomeManager.BiomeEntry entry : biomeList) {
            if (name.equals(entry.biome.biomeName)) {
                return entry;
            }
        }
        return null;
    }

    private void calculateBiomes(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_BIOME);
        // @@@ TODO: distinguish between random overworld biome, random nether biome, random biome and specific biomes
        for (Integer id : list) {
            String biomeName = KnownDimletConfiguration.idToBiome.get(id);
            BiomeManager.BiomeEntry entry = findBiomeEntry(biomeName, BiomeManager.BiomeType.COOL);
            entry = entry != null ? entry : findBiomeEntry(biomeName, BiomeManager.BiomeType.WARM);
            entry = entry != null ? entry : findBiomeEntry(biomeName, BiomeManager.BiomeType.DESERT);
            entry = entry != null ? entry : findBiomeEntry(biomeName, BiomeManager.BiomeType.ICY);
            if (entry != null) {
                biomes.add(entry);
            }
        }
    }


    public DimensionDescriptor getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public boolean hasFeatureType(FeatureType type) {
        return featureTypes.contains(type);
    }

    public boolean hasStructureType(StructureType type) {
        return structureTypes.contains(type);
    }

    public List<BiomeManager.BiomeEntry> getBiomes() {
        return biomes;
    }
}
