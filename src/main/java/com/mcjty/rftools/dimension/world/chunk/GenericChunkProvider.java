package com.mcjty.rftools.dimension.world.chunk;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;

public class GenericChunkProvider implements IChunkProvider {

    public GenericChunkProvider(World worldObj, long seed) {
    }

    @Override
    public boolean chunkExists(int p_73149_1_, int p_73149_2_) {
        return false;
    }

    @Override
    public Chunk provideChunk(int p_73154_1_, int p_73154_2_) {
        return null;
    }

    @Override
    public Chunk loadChunk(int p_73158_1_, int p_73158_2_) {
        return null;
    }

    @Override
    public void populate(IChunkProvider chunkProvider, int p_73153_2_, int p_73153_3_) {

    }

    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
        return false;
    }

    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public String makeString() {
        return null;
    }

    @Override
    public List getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_) {
        return null;
    }

    @Override
    public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
        return null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int p_82695_1_, int p_82695_2_) {

    }

    @Override
    public void saveExtraData() {

    }
}
