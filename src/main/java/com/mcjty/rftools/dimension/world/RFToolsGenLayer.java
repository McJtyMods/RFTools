package com.mcjty.rftools.dimension.world;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class RFToolsGenLayer extends GenLayer {

    private final GenLayer parent;

    public RFToolsGenLayer(long seed, GenLayer parent) {
        super(seed);
        this.parent = parent;
    }

    @Override
    public int[] getInts(int x, int z, int width, int length) {
//        return parent.getInts(x, z, width, length);
        boolean b = ((x >> 3) & 1) == ((z >> 3) & 1);
        int[] aint = IntCache.getIntCache(width * length);
        for (int i = 0; i < width * length; ++i) {
            if (b) {
                aint[i] = BiomeGenBase.plains.biomeID;
            } else {
                aint[i] = BiomeGenBase.desert.biomeID;
            }
        }
        return aint;
    }
}
