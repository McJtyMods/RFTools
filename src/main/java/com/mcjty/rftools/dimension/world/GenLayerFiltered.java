package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.world.types.ControllerType;
import com.mcjty.rftools.items.dimlets.BiomeControllerMapping;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.util.Map;

public class GenLayerFiltered extends GenLayer {

    private final GenLayer parent;
    private final GenericWorldChunkManager chunkManager;
    private Map<Integer, Integer> filterMap;

    public GenLayerFiltered(GenericWorldChunkManager chunkManager, long seed, GenLayer parent, Map<Integer, Integer> filterMap) {
        super(seed);
        this.parent = parent;
        this.chunkManager = chunkManager;
        this.filterMap = filterMap;
    }

    public GenLayerFiltered(GenericWorldChunkManager chunkManager, long seed, GenLayer parent, ControllerType type) {
        this(chunkManager, seed, parent, getFilterFromType(type));
    }

    private static Map<Integer, Integer> getFilterFromType(ControllerType type) {
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
        }
        return null;
    }

    @Override
    public int[] getInts(int x, int z, int width, int length) {
        int[] ints = parent.getInts(x, z, width, length);
        int[] aint = IntCache.getIntCache(width * length);
        for (int i = 0; i < width * length; ++i) {
            aint[i] = filterMap.get(ints[i]);
        }
        return aint;
    }
}
