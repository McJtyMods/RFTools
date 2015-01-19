package com.mcjty.rftools.dimension.world.types;

import net.minecraft.world.biome.BiomeGenBase;

public enum ControllerType {
    CONTROLLER_DEFAULT(0, null),
    CONTROLLER_SINGLE(1, null),
    CONTROLLER_CHECKERBOARD(2, null),
    CONTROLLER_COLD(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getTempCategory() == BiomeGenBase.TempCategory.COLD;
        }
    }),
    CONTROLLER_MEDIUM(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getTempCategory() == BiomeGenBase.TempCategory.MEDIUM;
        }
    }),
    CONTROLLER_WARM(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getTempCategory() == BiomeGenBase.TempCategory.WARM;
        }
    }),
    CONTROLLER_DRY(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getFloatRainfall() < 0.1;
        }
    }),
    CONTROLLER_WET(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.isHighHumidity();
        }
    }),
    CONTROLLER_FIELDS(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.heightVariation < 0.11 && biome.rootHeight < 0.25f;
        }
    }),
    CONTROLLER_MOUNTAINS(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.heightVariation > 0.45f;
        }
    });

    private final int neededBiomes;
    private final BiomeFilter filter;

    ControllerType(int neededBiomes, BiomeFilter filter) {
        this.neededBiomes = neededBiomes;
        this.filter = filter;
    }

    public int getNeededBiomes() {
        return neededBiomes;
    }

    public BiomeFilter getFilter() {
        return filter;
    }

    public static class BiomeFilter {
        public boolean match(BiomeGenBase biome) {
            return false;
        }

        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            float dr = a.getFloatRainfall() - b.getFloatRainfall();
            float dt = 0;//a.temperature - b.temperature;
            float dv = a.heightVariation - b.heightVariation;
            float dh = a.rootHeight - b.rootHeight;
            return Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
        }
    }
}
