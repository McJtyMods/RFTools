package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.dimension.world.types.ControllerType;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.Map;

public class BiomeControllerMapping {

    public static final Map<Integer, Integer> coldBiomeReplacements = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> warmBiomeReplacements = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> mediumBiomeReplacements = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> wetBiomeReplacements = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> dryBiomeReplacements = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> fieldsBiomeReplacements = new HashMap<Integer, Integer>();
    public static final Map<Integer, Integer> mountainsBiomeReplacements = new HashMap<Integer, Integer>();


    public static void setupControllerBiomes() {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();

        makeFilteredBiomeMap(biomeGenArray, coldBiomeReplacements, ControllerType.CONTROLLER_COLD);
        makeFilteredBiomeMap(biomeGenArray, warmBiomeReplacements, ControllerType.CONTROLLER_WARM);
        makeFilteredBiomeMap(biomeGenArray, mediumBiomeReplacements, ControllerType.CONTROLLER_MEDIUM);
        makeFilteredBiomeMap(biomeGenArray, wetBiomeReplacements, ControllerType.CONTROLLER_WET);
        makeFilteredBiomeMap(biomeGenArray, dryBiomeReplacements, ControllerType.CONTROLLER_DRY);
        makeFilteredBiomeMap(biomeGenArray, fieldsBiomeReplacements, ControllerType.CONTROLLER_FIELDS);
        makeFilteredBiomeMap(biomeGenArray, mountainsBiomeReplacements, ControllerType.CONTROLLER_MOUNTAINS);
    }

    private static void makeFilteredBiomeMap(BiomeGenBase[] biomeGenArray, Map<Integer, Integer> map, ControllerType type) {
        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                if (type.getFilter().match(biome)) {
                    map.put(biome.biomeID, biome.biomeID);
                } else {
                    map.put(biome.biomeID, findSuitableBiomes(biomeGenArray, biome, type.getFilter()));
                }
            }
        }
    }

    private static int findSuitableBiomes(BiomeGenBase[] biomeGenArray, BiomeGenBase biome, ControllerType.BiomeFilter filter) {
        double bestdist = 1000000000.0f;
        int bestidx = 0;        // Make sure we always have some matching biome.

        for (BiomeGenBase base : biomeGenArray) {
            if (base != null && filter.match(base)) {
                // This 'base' could be a replacement. Check if it is close enough.
                if (biome.getBiomeClass() == base.getBiomeClass()) {
                    // Same class, that's good enough for me.
                    return base.biomeID;
                }

                float dr = biome.getFloatRainfall() - base.getFloatRainfall();
                float dt = 0;//biome.temperature - base.temperature;
                float dv = biome.heightVariation - base.heightVariation;
                float dh = biome.rootHeight - base.rootHeight;
                double dist = Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
                if (dist < bestdist) {
                    bestdist = dist;
                    bestidx = base.biomeID;
                }
            }
        }
        return bestidx;
    }

}
