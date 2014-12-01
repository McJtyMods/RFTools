package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.FeatureType;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.TerrainType;
import com.mcjty.rftools.dimension.world.terrain.*;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.*;

public class GenericChunkProvider implements IChunkProvider {
    private Random rand;

    private NoiseGeneratorOctaves noiseGen1;
    private NoiseGeneratorOctaves noiseGen2;
    private NoiseGeneratorOctaves noiseGen3;
    private NoiseGeneratorPerlin noiseGen4;

    //A NoiseGeneratorOctaves used in generating terrain
    public NoiseGeneratorOctaves noiseGen5;

    // A NoiseGeneratorOctaves used in generating terrain
    public NoiseGeneratorOctaves noiseGen6;
    public NoiseGeneratorOctaves mobSpawnerNoise;

    private World worldObj;
    private DimensionInformation dimensionInformation;

    private static Map<TerrainType,BaseTerrainGenerator> terrainGeneratorMap = new HashMap<TerrainType, BaseTerrainGenerator>();

    static {
        terrainGeneratorMap.put(TerrainType.TERRAIN_VOID, new VoidTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_FLAT, new FlatTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_NORMAL, new NormalTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_AMPLIFIED, new AmplifiedTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_CAVES, new CavesTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_ISLAND, new IslandTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_SPHERES, new SpheresTerrainGenerator());
    }

    // Are map structures going to be generated (e.g. strongholds)
    private final boolean mapFeaturesEnabled;
    private WorldType worldType;
    public final double[] field_147434_q;                   // @todo better name once we figure out what this actually is
    private final float[] parabolicField;
    private double[] stoneNoise = new double[256];
    private MapGenBase caveGenerator = new MapGenCaves();

    // Holds Stronghold Generator
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();

    // Holds Village Generator
    private MapGenVillage villageGenerator = new MapGenVillage();

    // Holds Mineshaft Generator
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();

    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();

    // Holds ravine generator
    private MapGenBase ravineGenerator = new MapGenRavine();

    // The biomes that are used to generate the chunk
    private BiomeGenBase[] biomesForGeneration;

    double[] field_147427_d;
    double[] field_147428_e;
    double[] field_147425_f;
    double[] field_147426_g;
    int[][] field_73219_j = new int[32][32];

    {
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
        strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
        villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
        mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
        scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
    }

    public GenericChunkProvider(World world, long seed, boolean features) {
        this.worldObj = world;

        dimensionInformation = RfToolsDimensionManager.getDimensionManager(world).getDimensionInformation(world.provider.dimensionId);

        this.mapFeaturesEnabled = features;
        this.worldType = world.getWorldInfo().getTerrainType();
        this.rand = new Random((seed + 516) * 314);
//        this.noiseGen1 = new NoiseGeneratorOctaves(this.rand, 16);
//        this.noiseGen2 = new NoiseGeneratorOctaves(this.rand, 16);
//        this.noiseGen3 = new NoiseGeneratorOctaves(this.rand, 8);
//        this.noiseGen4 = new NoiseGeneratorPerlin(this.rand, 4);
//        this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
//        this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
        this.noiseGen1 = new NoiseGeneratorOctaves(this.rand, 18);
        this.noiseGen2 = new NoiseGeneratorOctaves(this.rand, 8);
        this.noiseGen3 = new NoiseGeneratorOctaves(this.rand, 16);
        this.noiseGen4 = new NoiseGeneratorPerlin(this.rand, 8);
        this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 12);
        this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 18);

        this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
        this.field_147434_q = new double[825];
        this.parabolicField = new float[25];

        for (int j = -2; j <= 2; ++j) {
            for (int k = -2; k <= 2; ++k) {
                float f = 10.0F / MathHelper.sqrt_float((float) (j * j + k * k) + 0.2F);
                this.parabolicField[j + 2 + (k + 2) * 5] = f;
            }
        }

        NoiseGenerator[] noiseGens = {noiseGen1, noiseGen2, noiseGen3, noiseGen4, noiseGen5, noiseGen6, mobSpawnerNoise};
        noiseGens = TerrainGen.getModdedNoiseGenerators(world, this.rand, noiseGens);
        this.noiseGen1 = (NoiseGeneratorOctaves) noiseGens[0];
        this.noiseGen2 = (NoiseGeneratorOctaves) noiseGens[1];
        this.noiseGen3 = (NoiseGeneratorOctaves) noiseGens[2];
        this.noiseGen4 = (NoiseGeneratorPerlin) noiseGens[3];
        this.noiseGen5 = (NoiseGeneratorOctaves) noiseGens[4];
        this.noiseGen6 = (NoiseGeneratorOctaves) noiseGens[5];
        this.mobSpawnerNoise = (NoiseGeneratorOctaves) noiseGens[6];
    }

    public void generateTerrain(int chunkX, int chunkZ, Block[] aBlock) {
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);
        this.func_147423_a(chunkX * 4, 0, chunkZ * 4);
        terrainGeneratorMap.get(dimensionInformation.getTerrainType()).generate(this, chunkX, chunkZ, aBlock);
    }


    public void replaceBlocksForBiome(int chunkX, int chunkZ, Block[] aBlock, byte[] abyte, BiomeGenBase[] biomeGenBases) {
        ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, chunkX, chunkZ, aBlock, abyte, biomeGenBases, this.worldObj);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.getResult() == Event.Result.DENY) return;

        double d0 = 0.03125D;
        this.stoneNoise = this.noiseGen4.func_151599_a(this.stoneNoise, (chunkX * 16), (chunkZ * 16), 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

        for (int k = 0; k < 16; ++k) {
            for (int l = 0; l < 16; ++l) {
                BiomeGenBase biomegenbase = biomeGenBases[l + k * 16];
                biomegenbase.genTerrainBlocks(this.worldObj, this.rand, aBlock, abyte, chunkX * 16 + k, chunkZ * 16 + l, this.stoneNoise[l + k * 16]);
            }
        }
    }

    /**
     * loads or generates the chunk at the chunk location specified
     */
    @Override
    public Chunk loadChunk(int chunkX, int chunkZ) {
        return this.provideChunk(chunkX, chunkZ);
    }

    /**
     * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
     * specified chunk from the map seed and chunk seed
     */
    @Override
    public Chunk provideChunk(int chunkX, int chunkZ) {
        this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L + 123456);
        Block[] ablock = new Block[65536];
        byte[] abyte = new byte[65536];

        this.generateTerrain(chunkX, chunkZ, ablock);

        this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
        this.replaceBlocksForBiome(chunkX, chunkZ, ablock, abyte, this.biomesForGeneration);
        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_CAVES)) {
            this.caveGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_RAVINES)) {
            this.ravineGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }

//        if (this.mapFeaturesEnabled) {
//            this.mineshaftGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
//            this.villageGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
//            this.strongholdGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
//            this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
//        }

        Chunk chunk = new Chunk(this.worldObj, ablock, abyte, chunkX, chunkZ);
        byte[] abyte1 = chunk.getBiomeArray();

        for (int k = 0; k < abyte1.length; ++k) {
            abyte1[k] = (byte) this.biomesForGeneration[k].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
    }

    private void func_147423_a(int chunkX4, int chunkY4, int chunkZ4) {
        double d0 = 684.412D;
        double d1 = 684.412D;
        double d2 = 512.0D;
        double d3 = 512.0D;
        this.field_147426_g = this.noiseGen6.generateNoiseOctaves(this.field_147426_g, chunkX4, chunkZ4, 5, 5, 200.0D, 200.0D, 0.5D);
        this.field_147427_d = this.noiseGen3.generateNoiseOctaves(this.field_147427_d, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 8.555150000000001D, 4.277575000000001D, 8.555150000000001D);
        this.field_147428_e = this.noiseGen1.generateNoiseOctaves(this.field_147428_e, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        this.field_147425_f = this.noiseGen2.generateNoiseOctaves(this.field_147425_f, chunkX4, chunkY4, chunkZ4, 5, 33, 5, 684.412D, 684.412D, 684.412D);
        boolean flag1 = false;
        boolean flag = false;
        int l = 0;
        int i1 = 0;
        double d4 = 8.5D;

        for (int j1 = 0; j1 < 5; ++j1) {
            for (int k1 = 0; k1 < 5; ++k1) {
                float f = 0.0F;
                float f1 = 0.0F;
                float f2 = 0.0F;
                byte b0 = 2;
                BiomeGenBase biomegenbase = this.biomesForGeneration[j1 + 2 + (k1 + 2) * 10];

                for (int l1 = -b0; l1 <= b0; ++l1) {
                    for (int i2 = -b0; i2 <= b0; ++i2) {
                        BiomeGenBase biomegenbase1 = this.biomesForGeneration[j1 + l1 + 2 + (k1 + i2 + 2) * 10];
                        float f3 = biomegenbase1.rootHeight;
                        float f4 = biomegenbase1.heightVariation;

                        if (this.worldType == WorldType.AMPLIFIED && f3 > 0.0F) {
                            f3 = 1.0F + f3 * 2.0F;
                            f4 = 1.0F + f4 * 4.0F;
                        }

                        float f5 = this.parabolicField[l1 + 2 + (i2 + 2) * 5] / (f3 + 2.0F);

                        if (biomegenbase1.rootHeight > biomegenbase.rootHeight) {
                            f5 /= 2.0F;
                        }

                        f += f4 * f5;
                        f1 += f3 * f5;
                        f2 += f5;
                    }
                }

                f /= f2;
                f1 /= f2;
                f = f * 0.9F + 0.1F;
                f1 = (f1 * 4.0F - 1.0F) / 8.0F;
                double d12 = this.field_147426_g[i1] / 8000.0D;

                if (d12 < 0.0D) {
                    d12 = -d12 * 0.3D;
                }

                d12 = d12 * 3.0D - 2.0D;

                if (d12 < 0.0D) {
                    d12 /= 2.0D;

                    if (d12 < -1.0D) {
                        d12 = -1.0D;
                    }

                    d12 /= 1.4D;
                    d12 /= 2.0D;
                } else {
                    if (d12 > 1.0D) {
                        d12 = 1.0D;
                    }

                    d12 /= 8.0D;
                }

                ++i1;
                double d13 = (double) f1;
                double d14 = (double) f;
                d13 += d12 * 0.2D;
                d13 = d13 * 8.5D / 8.0D;
                double d5 = 8.5D + d13 * 4.0D;

                for (int j2 = 0; j2 < 33; ++j2) {
                    double d6 = ((double) j2 - d5) * 12.0D * 128.0D / 256.0D / d14;

                    if (d6 < 0.0D) {
                        d6 *= 4.0D;
                    }

                    double d7 = this.field_147428_e[l] / 512.0D;
                    double d8 = this.field_147425_f[l] / 512.0D;
                    double d9 = (this.field_147427_d[l] / 10.0D + 1.0D) / 2.0D;
                    double d10 = MathHelper.denormalizeClamp(d7, d8, d9) - d6;

                    if (j2 > 29) {
                        double d11 = (double) ((float) (j2 - 29) / 3.0F);
                        d10 = d10 * (1.0D - d11) + -10.0D * d11;
                    }

                    this.field_147434_q[l] = d10;
                    ++l;
                }
            }
        }
    }

    /**
     * Checks to see if a chunk exists at x, y
     */
    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return true;
    }

    /**
     * Populates chunk with ores etc etc
     */
    @Override
    public void populate(IChunkProvider chunkProvider, int chunkX, int chunkZ) {
        BlockFalling.fallInstantly = true;
        int x = chunkX * 16;
        int z = chunkZ * 16;
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(x + 16, z + 16);
        this.rand.setSeed(this.worldObj.getSeed());
        long i1 = this.rand.nextLong() / 2L * 2L + 1L;
        long j1 = this.rand.nextLong() / 2L * 2L + 1L;
        this.rand.setSeed(chunkX * i1 + chunkZ * j1 ^ this.worldObj.getSeed());
        boolean flag = false;

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(chunkProvider, worldObj, rand, chunkX, chunkZ, flag));

        if (this.mapFeaturesEnabled) {
            this.mineshaftGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
            flag = this.villageGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
            this.strongholdGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
            this.scatteredFeatureGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
        }

        int k1;
        int l1;
        int i2;


        if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && !flag && this.rand.nextInt(4) == 0
                && TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, LAKE)) {
            k1 = x + this.rand.nextInt(16) + 8;
            l1 = this.rand.nextInt(256);
            i2 = z + this.rand.nextInt(16) + 8;
            (new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.rand, k1, l1, i2);
        }

        if (TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, LAVA) && !flag && this.rand.nextInt(8) == 0) {
            k1 = x + this.rand.nextInt(16) + 8;
            l1 = this.rand.nextInt(this.rand.nextInt(248) + 8);
            i2 = z + this.rand.nextInt(16) + 8;

            if (l1 < 63 || this.rand.nextInt(10) == 0) {
                (new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.rand, k1, l1, i2);
            }
        }

        boolean doGen = TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, DUNGEON);
        for (k1 = 0; doGen && k1 < 8; ++k1) {
            l1 = x + this.rand.nextInt(16) + 8;
            i2 = this.rand.nextInt(256);
            int j2 = z + this.rand.nextInt(16) + 8;
            (new WorldGenDungeons()).generate(this.worldObj, this.rand, l1, i2, j2);
        }

        biomegenbase.decorate(this.worldObj, this.rand, x, z);
        if (TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, ANIMALS)) {
            SpawnerAnimals.performWorldGenSpawning(this.worldObj, biomegenbase, x + 8, z + 8, 16, 16, this.rand);
        }
        x += 8;
        z += 8;

        doGen = TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, ICE);
        for (k1 = 0; doGen && k1 < 16; ++k1) {
            for (l1 = 0; l1 < 16; ++l1) {
                i2 = this.worldObj.getPrecipitationHeight(x + k1, z + l1);

                if (this.worldObj.isBlockFreezable(k1 + x, i2 - 1, l1 + z)) {
                    this.worldObj.setBlock(k1 + x, i2 - 1, l1 + z, Blocks.ice, 0, 2);
                }

                if (this.worldObj.func_147478_e(k1 + x, i2, l1 + z, true)) {
                    this.worldObj.setBlock(k1 + x, i2, l1 + z, Blocks.snow_layer, 0, 2);
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(chunkProvider, worldObj, rand, chunkX, chunkZ, flag));

        BlockFalling.fallInstantly = false;
    }

    /**
     * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
     * Return true if all chunks have been saved.
     */
    @Override
    public boolean saveChunks(boolean p_73151_1_, IProgressUpdate progressUpdate) {
        return true;
    }

    /**
     * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
     * unimplemented.
     */
    @Override
    public void saveExtraData() {
    }

    /**
     * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
     */
    @Override
    public boolean unloadQueuedChunks() {
        return false;
    }

    /**
     * Returns if the IChunkProvider supports saving.
     */
    @Override
    public boolean canSave() {
        return true;
    }

    /**
     * Converts the instance data to a readable string.
     */
    @Override
    public String makeString() {
        return "RandomLevelSource";
    }

    /**
     * Returns a list of creatures of the specified type that can spawn at the given location.
     */
    @Override
    public List getPossibleCreatures(EnumCreatureType creatureType, int x, int y, int z) {
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(x, z);
        return creatureType == EnumCreatureType.monster && this.scatteredFeatureGenerator.func_143030_a(x, y, z) ? this.scatteredFeatureGenerator.getScatteredFeatureSpawnList() : biomegenbase.getSpawnableList(creatureType);
    }

    @Override
    public ChunkPosition func_147416_a(World world, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
        return "Stronghold".equals(p_147416_2_) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(world, p_147416_3_, p_147416_4_, p_147416_5_) : null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int p_82695_1_, int p_82695_2_) {
        if (this.mapFeaturesEnabled) {
            this.mineshaftGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, (Block[]) null);
            this.villageGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, (Block[]) null);
            this.strongholdGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, (Block[]) null);
            this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, (Block[]) null);
        }
    }
}