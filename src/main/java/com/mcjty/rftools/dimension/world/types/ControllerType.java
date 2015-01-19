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

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, false, true, false);
        }
    }),
    CONTROLLER_MEDIUM(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getTempCategory() == BiomeGenBase.TempCategory.MEDIUM;
        }

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, false, true, false);
        }
    }),
    CONTROLLER_WARM(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getTempCategory() == BiomeGenBase.TempCategory.WARM;
        }

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, false, true, false);
        }
    }),
    CONTROLLER_DRY(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.getFloatRainfall() < 0.1;
        }

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, true, false, false);
        }
    }),
    CONTROLLER_WET(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.isHighHumidity();
        }

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, true, false, false);
        }
    }),
    CONTROLLER_FIELDS(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.heightVariation < 0.11 && biome.rootHeight < 0.25f;
        }

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, false, false, true);
        }
    }),
    CONTROLLER_MOUNTAINS(0, new BiomeFilter() {
        @Override
        public boolean match(BiomeGenBase biome) {
            return biome.heightVariation > 0.45f;
        }

        @Override
        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
            return calculateBiomeDistance(a, b, false, false, true);
        }
    }),
    CONTROLLER_FILTERED(-1, null);

    private final int neededBiomes;
    private final BiomeFilter filter;

    ControllerType(int neededBiomes, BiomeFilter filter) {
        this.neededBiomes = neededBiomes;
        this.filter = filter;
    }

    /**
     * Return the amount of biomes needed for this controller. -1 means that it can use any number of biomes.
     * @return
     */
    public int getNeededBiomes() {
        return neededBiomes;
    }

    public BiomeFilter getFilter() {
        return filter;
    }

    public abstract static class BiomeFilter {
        /**
         * Return true if this biome should be selected by this filter.
         */
        public abstract boolean match(BiomeGenBase biome);

        /**
         * Return the similarity distance between two biomes.
         */
        public abstract double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b);

        public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b, boolean ignoreRain, boolean ignoreTemperature, boolean ignoreHeight) {
            float dr = a.getFloatRainfall() - b.getFloatRainfall();
            if (ignoreRain) {
                dr = 0.0f;
            }
            float dt = a.temperature - b.temperature;
            if (ignoreTemperature) {
                dt = 0.0f;
            }
            float dv = a.heightVariation - b.heightVariation;
            float dh = a.rootHeight - b.rootHeight;
            if (ignoreHeight) {
                dv = 0.0f;
                dh = 0.0f;
            }
            return Math.sqrt(dr * dr + dt * dt + dv * dv + dh * dh);
        }
    }
}
