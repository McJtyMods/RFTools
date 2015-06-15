package mcjty.rftools.dimension.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.world.biome.BiomeGenBase;

public class BiomeMutator {

    public static BiomeGenBase mutateBiome(int index, BiomeGenBase biome) {
        return new RfToolsBiomeMutator(index, biome) {
            @Override
            @SideOnly(Side.CLIENT)
            public int getBiomeGrassColor(int p_150558_1_, int p_150558_2_, int p_150558_3_) {
                return 0xfff472d0;          // Pink grass
            }
        };
    }
}
