package mcjty.rftools.world;

import com.google.common.base.Predicate;
import mcjty.rftools.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.fml.common.IWorldGenerator;

import java.util.Random;
import java.util.Set;

public class RFToolsWorldGenerator implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkProvider chunkGenerator, IChunkProvider chunkProvider) {
        Set<Integer> oregen;
        if (RFTools.instance.rftoolsDimensions) {
            oregen = GeneralConfiguration.oregenDimensionsWithDimensions;
        } else {
            oregen = GeneralConfiguration.oregenDimensionsWithoutDimensions;
        }

        if (oregen.contains(world.provider.getDimensionId())) {
            addOreSpawn(ModBlocks.dimensionalShardBlock.getDefaultState(), Blocks.stone.getDefaultState(), world, random, chunkX * 16, chunkZ * 16,
                        GeneralConfiguration.oreMinimumVeinSize, GeneralConfiguration.oreMaximumVeinSize, GeneralConfiguration.oreMaximumVeinCount,
                        GeneralConfiguration.oreMinimumHeight, GeneralConfiguration.oreMaximumHeight);
        }
    }

    public void addOreSpawn(IBlockState block, IBlockState targetBlock,
                            World world, Random random, int blockXPos, int blockZPos, int minVeinSize, int maxVeinSize, int chancesToSpawn, int minY, int maxY) {
        WorldGenMinable minable = new WorldGenMinable(block, (minVeinSize - random.nextInt(maxVeinSize - minVeinSize)), new Predicate<IBlockState>() {
            @Override
            public boolean apply(IBlockState input) {
                return input.getBlock() == targetBlock.getBlock();
            }
        });
        for (int i = 0 ; i < chancesToSpawn ; i++) {
            int posX = blockXPos + random.nextInt(16);
            int posY = minY + random.nextInt(maxY - minY);
            int posZ = blockZPos + random.nextInt(16);
            minable.generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }
}
