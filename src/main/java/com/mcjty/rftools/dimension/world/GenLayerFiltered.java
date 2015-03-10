package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.world.types.ControllerType;
import com.mcjty.rftools.items.dimlets.BiomeControllerMapping;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Map;

public class GenLayerFiltered extends GenLayer {

    private final GenericWorldChunkManager chunkManager;
    private final ControllerType type;

    public GenLayerFiltered(GenericWorldChunkManager chunkManager, long seed, GenLayer parent, ControllerType type) {
        super(seed);
        this.parent = parent;
        this.chunkManager = chunkManager;
        this.type = type;
    }

    private Map<Integer, Integer> getFilterFromType() {
        switch (type) {
            case CONTROLLER_DEFAULT:
            case CONTROLLER_SINGLE:
            case CONTROLLER_CHECKERBOARD:
                // Cannot happen
                return null;
            case CONTROLLER_COLD:
                return BiomeControllerMapping.coldBiomeReplacements;
            case CONTROLLER_MEDIUM:
                return BiomeControllerMapping.mediumBiomeReplacements;
            case CONTROLLER_WARM:
                return BiomeControllerMapping.warmBiomeReplacements;
            case CONTROLLER_DRY:
                return BiomeControllerMapping.dryBiomeReplacements;
            case CONTROLLER_WET:
                return BiomeControllerMapping.wetBiomeReplacements;
            case CONTROLLER_FIELDS:
                return BiomeControllerMapping.fieldsBiomeReplacements;
            case CONTROLLER_MOUNTAINS:
                return BiomeControllerMapping.mountainsBiomeReplacements;
            case CONTROLLER_MAGICAL:
                return BiomeControllerMapping.magicalBiomeReplacements;
            case CONTROLLER_FOREST:
                return BiomeControllerMapping.forestBiomeReplacements;
            case CONTROLLER_FILTERED:
                return chunkManager.getDimensionInformation().getBiomeMapping();
        }
        return null;
    }

    @Override
    public int[] getInts(int x, int z, int width, int length) {
        int[] ints = parent.getInts(x, z, width, length);
        int[] aint = IntCache.getIntCache(width * length);
        Map<Integer, Integer> filterMap = getFilterFromType();
        for (int i = 0; i < width * length; ++i) {
            aint[i] = filterMap.get(ints[i]);
        }
        return aint;
    }
}
