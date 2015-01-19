package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.world.types.ControllerType;
import com.mcjty.rftools.items.dimlets.BiomeControllerMapping;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

import java.util.List;
import java.util.Map;

public class GenLayerFiltered extends GenLayer {

    private final GenLayer parent;
    private final GenericWorldChunkManager chunkManager;
    private final ControllerType type;
    private Map<Integer, Integer> filterMap;

    public GenLayerFiltered(GenericWorldChunkManager chunkManager, long seed, GenLayer parent, ControllerType type) {
        super(seed);
        this.parent = parent;
        this.chunkManager = chunkManager;
        this.type = type;
        switch (type) {
            case CONTROLLER_DEFAULT:
            case CONTROLLER_SINGLE:
            case CONTROLLER_CHECKERBOARD:
                // Cannot happen
                filterMap = null;
                break;
            case CONTROLLER_COLD:
                filterMap = BiomeControllerMapping.coldBiomeReplacements;
                break;
            case CONTROLLER_MEDIUM:
                break;
            case CONTROLLER_WARM:
                break;
            case CONTROLLER_DRY:
                break;
            case CONTROLLER_WET:
                break;
            case CONTROLLER_FIELDS:
                break;
            case CONTROLLER_MOUNTAINS:
                break;
        }
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
