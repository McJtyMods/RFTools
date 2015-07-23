package mcjty.rftools.dimension.world;

import mcjty.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class WorldRefresher {
    public static void refreshChunks(World world) {
        try {
            ChunkProviderServer chunkServer = (ChunkProviderServer) world.getChunkProvider();

            List<ChunkCoordIntPair> toUnload = new ArrayList<ChunkCoordIntPair>();
            for (Object obj : chunkServer.loadedChunks) {
                Chunk chunk = (Chunk) obj;
                toUnload.add(chunk.getChunkCoordIntPair());
            }

            for (ChunkCoordIntPair pair : toUnload) {
                Chunk oldChunk = world.getChunkFromChunkCoords(pair.chunkXPos, pair.chunkZPos);
                WorldServer worldServer = (WorldServer) world;
                ChunkProviderServer chunkProviderServer = worldServer.theChunkProviderServer;
//                IChunkProvider chunkProviderGenerate = ObfuscationReflectionHelper.getPrivateValue(ChunkProviderServer.class, chunkProviderServer, "d", "field_73246_d");
                IChunkProvider chunkProviderGenerate = chunkProviderServer.currentChunkProvider;

                Chunk newChunk = chunkProviderGenerate.provideChunk(oldChunk.xPosition, oldChunk.zPosition);

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        for (int y = 0; y < world.getHeight(); y++) {
                            Block blockID = newChunk.getBlock(x, y, z);
                            int metadata = newChunk.getBlockMetadata(x, y, z);
                            worldServer.setBlock(x + oldChunk.xPosition * 16, y, z + oldChunk.zPosition * 16, blockID, metadata, 2);
                            TileEntity tileEntity = newChunk.getTileEntityUnsafe(x, y, z);
                            if (tileEntity != null) {
                                worldServer.setTileEntity(x + oldChunk.xPosition * 16, y, z + oldChunk.zPosition * 16, tileEntity);
                            }
                        }
                    }
                }

                oldChunk.isTerrainPopulated = false;
                chunkProviderGenerate.populate(chunkProviderGenerate, oldChunk.xPosition, oldChunk.zPosition);
            }
        } catch (Exception e) {
            Logging.logError("Failed to regenerate chunks!");
            e.printStackTrace();
        }
    }

    public static void refreshChunksBad(World world) {
        try {
            ChunkProviderServer chunkServer = (ChunkProviderServer) world.getChunkProvider();

            Field u;
            try {
                u = ChunkProviderServer.class.getDeclaredField("field_73248_b"); // chunksToUnload
            } catch(NoSuchFieldException e) {
                u = ChunkProviderServer.class.getDeclaredField("chunksToUnload");
            }
            u.setAccessible(true);
            Set<?> unloadQueue = (Set<?>) u.get(chunkServer);

            Field m;
            try {
                m = ChunkProviderServer.class.getDeclaredField("field_73244_f"); // loadedChunkHashMap
            } catch(NoSuchFieldException e) {
                m = ChunkProviderServer.class.getDeclaredField("loadedChunkHashMap");
            }
            m.setAccessible(true);
            LongHashMap loadedMap = (LongHashMap) m.get(chunkServer);

            Field lc;
            try {
                lc = ChunkProviderServer.class.getDeclaredField("field_73245_g"); // loadedChunkHashMap
            } catch(NoSuchFieldException e) {
                lc = ChunkProviderServer.class.getDeclaredField("loadedChunks");
            }
            lc.setAccessible(true);
            @SuppressWarnings("unchecked") List<Chunk> loaded = (List<Chunk>) lc.get(chunkServer);

            Field p;
            try {
                p = ChunkProviderServer.class.getDeclaredField("field_73246_d"); // currentChunkProvider
            } catch(NoSuchFieldException e) {
                p = ChunkProviderServer.class.getDeclaredField("currentChunkProvider");
            }
            p.setAccessible(true);
            IChunkProvider chunkProvider = (IChunkProvider) p.get(chunkServer);

            List<ChunkCoordIntPair> toUnload = new ArrayList<ChunkCoordIntPair>();
            for (Object obj : chunkServer.loadedChunks) {
                Chunk chunk = (Chunk) obj;
                toUnload.add(chunk.getChunkCoordIntPair());
            }

            for (ChunkCoordIntPair pair : toUnload) {
                int x = pair.chunkXPos;
                int z = pair.chunkZPos;
                long pos = ChunkCoordIntPair.chunkXZ2Int(x, z);
                Chunk chunk;
                if (chunkServer.chunkExists(x, z)) {
                    chunk = chunkServer.loadChunk(x, z);
                    chunk.onChunkUnload();
                }
                unloadQueue.remove(pos);
                loadedMap.remove(pos);
                chunk = chunkProvider.provideChunk(x, z);
                loadedMap.add(pos, chunk);
                loaded.add(chunk);
                if (chunk != null) {
                    chunk.onChunkLoad();
                    chunk.populateChunk(chunkProvider, chunkProvider, x, z);
                }
            }
        } catch (Exception e) {
            Logging.logError("Failed to regenerate chunks!");
            e.printStackTrace();
        }
    }
}
