package mcjty.rftools.world;

import mcjty.rftools.config.GeneralConfiguration;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.ores.DimensionalShardBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.feature.WorldGenMinable;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayDeque;
import java.util.Random;
import java.util.Set;

public class RFToolsWorldGenerator implements IWorldGenerator {
    public static final String RETRO_NAME = "RFToolsGen";
    public static RFToolsWorldGenerator instance = new RFToolsWorldGenerator();

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        generateWorld(random, chunkX, chunkZ, world, true);
    }

    public void generateWorld(Random random, int chunkX, int chunkZ, World world, boolean newGen) {
        if (!newGen && !GeneralConfiguration.retrogen.get()) {
            return;
        }

        Set<Integer> oregen;
        if (RFTools.setup.rftoolsDimensions) {
            oregen = GeneralConfiguration.oregenDimensionsWithDimensions;
        } else {
            oregen = GeneralConfiguration.oregenDimensionsWithoutDimensions;
        }

        if (oregen.contains(world.provider.getDimension())) {
            BlockState ore;
            BlockState base;
            if (world.provider.getDimension() == 1) {
                ore = ModBlocks.dimensionalShardBlock.getDefaultState().withProperty(DimensionalShardBlock.ORETYPE, DimensionalShardBlock.OreType.ORE_END);
                base = Blocks.END_STONE.getDefaultState();
            } else if (world.provider.getDimension() == -1) {
                ore = ModBlocks.dimensionalShardBlock.getDefaultState().withProperty(DimensionalShardBlock.ORETYPE, DimensionalShardBlock.OreType.ORE_NETHER);
                base = Blocks.NETHERRACK.getDefaultState();
            } else {
                ore = ModBlocks.dimensionalShardBlock.getDefaultState();
                base = Blocks.STONE.getDefaultState();
            }
            addOreSpawn(ore, base, world, random, chunkX * 16, chunkZ * 16,
                        GeneralConfiguration.oreMinimumVeinSize.get(), GeneralConfiguration.oreMaximumVeinSize.get(), GeneralConfiguration.oreMaximumVeinCount.get(),
                        GeneralConfiguration.oreMinimumHeight.get(), GeneralConfiguration.oreMaximumHeight.get());
        }

        if (!newGen) {
            world.getChunkFromChunkCoords(chunkX, chunkZ).markDirty();
        }
    }



    public void addOreSpawn(BlockState block, BlockState targetBlock,
                            World world, Random random, int blockXPos, int blockZPos, int minVeinSize, int maxVeinSize, int chancesToSpawn, int minY, int maxY) {
        WorldGenMinable minable = new WorldGenMinable(block, (minVeinSize + random.nextInt(maxVeinSize - minVeinSize + 1)), state -> state.getBlock() == targetBlock.getBlock());
        for (int i = 0 ; i < chancesToSpawn ; i++) {
            int posX = blockXPos + random.nextInt(16);
            int posY = minY + random.nextInt(maxY - minY + 1);
            int posZ = blockZPos + random.nextInt(16);
            minable.generate(world, random, new BlockPos(posX, posY, posZ));
        }
    }

    @SubscribeEvent
    public void handleChunkSaveEvent(ChunkDataEvent.Save event) {
        CompoundNBT genTag = event.getData().getCompoundTag(RETRO_NAME);
        if (!genTag.hasKey("generated")) {
            // If we did not have this key then this is a new chunk and we will have proper ores generated.
            // Otherwise we are saving a chunk for which ores are not yet generated.
            genTag.setBoolean("generated", true);
        }
        event.getData().setTag(RETRO_NAME, genTag);
    }

    @SubscribeEvent
    public void handleChunkLoadEvent(ChunkDataEvent.Load event) {
        int dim = event.getWorld().provider.getDimension();

        boolean regen = false;
        CompoundNBT tag = (CompoundNBT) event.getData().getTag(RETRO_NAME);
        NBTTagList list = null;
        Pair<Integer,Integer> cCoord = Pair.of(event.getChunk().x, event.getChunk().z);

        if (tag != null) {
            boolean generated = GeneralConfiguration.retrogen.get() && !tag.hasKey("generated");
            if (generated) {
//                Logging.log("Queuing Retrogen for chunk: " + cCoord.toString() + ".");
                regen = true;
            }
        } else {
            regen = GeneralConfiguration.retrogen.get();
        }

        if (regen) {
            ArrayDeque<WorldTickHandler.RetroChunkCoord> chunks = WorldTickHandler.chunksToGen.get(dim);

            if (chunks == null) {
                WorldTickHandler.chunksToGen.put(dim, new ArrayDeque<>(128));
                chunks = WorldTickHandler.chunksToGen.get(dim);
            }
            if (chunks != null) {
                chunks.addLast(new WorldTickHandler.RetroChunkCoord(cCoord, list));
                WorldTickHandler.chunksToGen.put(dim, chunks);
            }
        }
    }

}
