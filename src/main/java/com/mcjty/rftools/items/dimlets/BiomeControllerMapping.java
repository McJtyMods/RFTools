package com.mcjty.rftools.items.dimlets;

import com.mcjty.rftools.dimension.world.types.ControllerType;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.HashMap;
import java.util.Map;

public class BiomeControllerMapping {

    public final static Map<Integer, Integer> coldBiomeReplacements = new HashMap<Integer, Integer>();
    public final static Map<Integer, Integer> warmBiomeReplacements = new HashMap<Integer, Integer>();


    public static void setupControllerBiomes() {
        BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();

        for (BiomeGenBase biome : biomeGenArray) {
            if (biome != null) {
                if (biome.getTempCategory() == BiomeGenBase.TempCategory.COLD) {
                    coldBiomeReplacements.put(biome.biomeID, biome.biomeID);
                } else {
                    coldBiomeReplacements.put(biome.biomeID, findSuitableBiomes(biomeGenArray, biome, ControllerType.CONTROLLER_COLD.getFilter()));
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
