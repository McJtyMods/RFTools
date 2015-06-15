package mcjty.rftools.dimension.world;

import mcjty.rftools.dimension.DimensionInformation;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

import java.util.Arrays;

public class SingleBiomeWorldChunkManager extends WorldChunkManager {
    private BiomeGenBase biomeGenBase;

    public SingleBiomeWorldChunkManager(long seed, WorldType worldType, DimensionInformation dimensionInformation) {
        super(seed, worldType);
        biomeGenBase = dimensionInformation.getBiomes().get(0);
    }

    /**
     * Returns an array of biomes for the location input.
     */
    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] biomeArray, int x, int z, int sizex, int sizez) {
        if (biomeArray == null || biomeArray.length < sizex * sizez) {
            biomeArray = new BiomeGenBase[sizex * sizez];
        }
        Arrays.fill(biomeArray, 0, sizex * sizez, this.biomeGenBase);
        return biomeArray;
    }
}
