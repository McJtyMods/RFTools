package com.mcjty.rftools.dimension.world;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.MobDescriptor;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.world.terrain.*;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.MapGenCaves;
import net.minecraft.world.gen.MapGenRavine;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.*;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.*;

public class GenericChunkProvider implements IChunkProvider {
    public Random rand;

    private World worldObj;
    public DimensionInformation dimensionInformation;
    private List<BiomeGenBase.SpawnListEntry> extraSpawns;
    private List<Integer> extraSpawnsMax;

    private static final Map<TerrainType,BaseTerrainGenerator> terrainGeneratorMap = new HashMap<TerrainType, BaseTerrainGenerator>();

    static {
        terrainGeneratorMap.put(TerrainType.TERRAIN_VOID, new VoidTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_FLAT, new FlatTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_NORMAL, new NormalTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_AMPLIFIED, new AmplifiedTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_CAVES, new CavesTerrainGenerator());
        terrainGeneratorMap.put(TerrainType.TERRAIN_ISLAND, new IslandTerrainGenerator());
    }

    // Are map structures going to be generated (e.g. strongholds)
    public WorldType worldType;

    private MapGenBase caveGenerator = new MapGenCaves();

    // RFTools specific features.
    private MapGenBase tendrilGenerator = new MapGenTendrils(this);
    private MapGenBase canyonGenerator = new MapGenCanyons(this);
//    private MapGenBase sphereGenerator = new MapGenSpheres(this);

    // Holds Stronghold Generator
    private MapGenStronghold strongholdGenerator = new MapGenStronghold();

    // Holds Village Generator
    private MapGenVillage villageGenerator = new MapGenVillage();

    // Holds Mineshaft Generator
    private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();

    // For nether fortresses
    public MapGenNetherBridge genNetherBridge = new MapGenNetherBridge();

    private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();

    // Holds ravine generator
    private MapGenBase ravineGenerator = new MapGenRavine();

    // The biomes that are used to generate the chunk
    public BiomeGenBase[] biomesForGeneration;

    {
        caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
        tendrilGenerator = TerrainGen.getModdedMapGen(tendrilGenerator, CAVE);
        canyonGenerator = TerrainGen.getModdedMapGen(canyonGenerator, RAVINE);
//        sphereGenerator = TerrainGen.getModdedMapGen(sphereGenerator, RAVINE);
        strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
        villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
        mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
        scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
        ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
        genNetherBridge = (MapGenNetherBridge) TerrainGen.getModdedMapGen(genNetherBridge, NETHER_BRIDGE);
    }

    public GenericChunkProvider(World world, long seed) {
        this.worldObj = world;

        dimensionInformation = RfToolsDimensionManager.getDimensionManager(world).getDimensionInformation(world.provider.dimensionId);

        this.worldType = world.getWorldInfo().getTerrainType();

        if (dimensionInformation.getTerrainType() == TerrainType.TERRAIN_AMPLIFIED) {
            worldType = WorldType.AMPLIFIED;
        } else if (dimensionInformation.getTerrainType() == TerrainType.TERRAIN_FLAT) {
            worldType = WorldType.FLAT;
        }

        this.rand = new Random((seed + 516) * 314);

        terrainGeneratorMap.get(dimensionInformation.getTerrainType()).setup(world, this);

        extraSpawns = new ArrayList<BiomeGenBase.SpawnListEntry>();
        extraSpawnsMax = new ArrayList<Integer>();
        for (MobDescriptor mob : dimensionInformation.getExtraMobs()) {
            Class<? extends EntityLiving> entityClass = mob.getEntityClass();
            extraSpawns.add(new BiomeGenBase.SpawnListEntry(entityClass, mob.getSpawnChance(), mob.getMinGroup(), mob.getMaxGroup()));
            extraSpawnsMax.add(mob.getMaxLoaded());
            System.out.println("MOB: entityClass = " + entityClass);
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

        BaseTerrainGenerator terrainGenerator = terrainGeneratorMap.get(dimensionInformation.getTerrainType());

        this.biomesForGeneration = this.worldObj.getWorldChunkManager().getBiomesForGeneration(this.biomesForGeneration, chunkX * 4 - 2, chunkZ * 4 - 2, 10, 10);

        // @todo: is this right? Are we not overwriting standard biome information?
        if (!dimensionInformation.getTerrainType().supportsLakes()) {
            for (BiomeGenBase biome : biomesForGeneration) {
                biome.theBiomeDecorator.generateLakes = false;
            }
        }


        terrainGenerator.generate(chunkX, chunkZ, ablock);
        this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, chunkX * 16, chunkZ * 16, 16, 16);
        terrainGenerator.replaceBlocksForBiome(chunkX, chunkZ, ablock, abyte, this.biomesForGeneration);

        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_TENDRILS)) {
            this.tendrilGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_CANYONS)) {
            this.canyonGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
//        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_SPHERES)) {
//            this.sphereGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
//        }
        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_CAVES)) {
            this.caveGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasFeatureType(FeatureType.FEATURE_RAVINES)) {
            this.ravineGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }

        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_MINESHAFT)) {
            this.mineshaftGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_VILLAGE)) {
            this.villageGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_STRONGHOLD)) {
            this.strongholdGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_FORTRESS)) {
            this.genNetherBridge.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_SCATTERED)) {
            this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, ablock);
        }

        Chunk chunk = new Chunk(this.worldObj, ablock, abyte, chunkX, chunkZ);
        byte[] abyte1 = chunk.getBiomeArray();

        for (int k = 0; k < abyte1.length; ++k) {
            abyte1[k] = (byte) this.biomesForGeneration[k].biomeID;
        }

        chunk.generateSkylightMap();
        return chunk;
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

        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_MINESHAFT)) {
            this.mineshaftGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_VILLAGE)) {
            flag = this.villageGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_STRONGHOLD)) {
            this.strongholdGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_FORTRESS)) {
            this.genNetherBridge.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_SCATTERED)) {
            this.scatteredFeatureGenerator.generateStructuresInChunk(this.worldObj, this.rand, chunkX, chunkZ);
        }

        int k1;
        int l1;
        int i2;


        if (dimensionInformation.getTerrainType().supportsLakes() && dimensionInformation.hasFeatureType(FeatureType.FEATURE_LAKES)) {
            if (dimensionInformation.getFluidsForLakes().length == 0) {
                // No specific liquid dimlets specified: we generate default lakes (water and lava were appropriate).
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
            } else {
                // Generate lakes for the specified biomes.
                for (Block liquid : dimensionInformation.getFluidsForLakes()) {
                    if (!flag && this.rand.nextInt(4) == 0
                            && TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, LAKE)) {
                        k1 = x + this.rand.nextInt(16) + 8;
                        l1 = this.rand.nextInt(256);
                        i2 = z + this.rand.nextInt(16) + 8;
                        (new WorldGenLakes(liquid)).generate(this.worldObj, this.rand, k1, l1, i2);
                    }
                }
            }
        }

        boolean doGen = false;
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_DUNGEON)) {
            doGen = TerrainGen.populate(chunkProvider, worldObj, rand, chunkX, chunkZ, flag, DUNGEON);
            for (k1 = 0; doGen && k1 < 8; ++k1) {
                l1 = x + this.rand.nextInt(16) + 8;
                i2 = this.rand.nextInt(256);
                int j2 = z + this.rand.nextInt(16) + 8;
                (new WorldGenDungeons()).generate(this.worldObj, this.rand, l1, i2, j2);
            }
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
        List creatures = getDefaultCreatures(creatureType, x, y, z);
        if (extraSpawns.isEmpty()) {
            return creatures;
        }

        if (creatureType == EnumCreatureType.ambient) {
            creatures = new ArrayList(creatures);
            for (int i = 0 ; i < extraSpawns.size() ; i++) {
                Class entityClass = extraSpawns.get(i).entityClass;
                if (EntityAnimal.class.isAssignableFrom(entityClass)) {
                    int count = worldObj.countEntities(entityClass);
                    if (count < extraSpawnsMax.get(i)) {
//                        System.out.println("ANIMAL:" + entityClass + ": count = " + count + " / max = " + extraSpawnsMax.get(i));
                        creatures.add(extraSpawns.get(i));
                    }
                }
            }
        } else if (creatureType == EnumCreatureType.monster) {
            creatures = new ArrayList(creatures);
            for (int i = 0 ; i < extraSpawns.size() ; i++) {
                Class entityClass = extraSpawns.get(i).entityClass;
                if (EntityMob.class.isAssignableFrom(entityClass)) {
                    int count = worldObj.countEntities(entityClass);
                    if (count < extraSpawnsMax.get(i)) {
//                        System.out.println("HOSTILE:" + entityClass + ": count = " + count + " / max = " + extraSpawnsMax.get(i));
                        creatures.add(extraSpawns.get(i));
                    }
                }
            }
        }


        return creatures;
    }

    private List getDefaultCreatures(EnumCreatureType creatureType, int x, int y, int z) {
        BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(x, z);
        if (creatureType == EnumCreatureType.monster) {
            if (dimensionInformation.isPeaceful()) {
                return Collections.emptyList();
            }
            if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_SCATTERED)) {
                if (this.scatteredFeatureGenerator.func_143030_a(x, y, z)) {
                    return this.scatteredFeatureGenerator.getScatteredFeatureSpawnList();
                }
            }

            if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_FORTRESS)) {
                if (this.genNetherBridge.hasStructureAt(x, y, z)) {
                    return this.genNetherBridge.getSpawnList();
                }

                if (this.genNetherBridge.func_142038_b(x, y, z) && this.worldObj.getBlock(x, y - 1, z) == Blocks.nether_brick) {
                    return this.genNetherBridge.getSpawnList();
                }
            }
        }

        return biomegenbase.getSpawnableList(creatureType);
    }

    @Override
    public ChunkPosition func_147416_a(World world, String name, int x, int y, int z) {
        return "Stronghold".equals(name) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(world, x, y, z) : null;
    }

    @Override
    public int getLoadedChunkCount() {
        return 0;
    }

    @Override
    public void recreateStructures(int chunkX, int chunkZ) {
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_MINESHAFT)) {
            this.mineshaftGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, null);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_VILLAGE)) {
            this.villageGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, null);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_STRONGHOLD)) {
            this.strongholdGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, null);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_FORTRESS)) {
            this.genNetherBridge.func_151539_a(this, this.worldObj, chunkX, chunkZ, null);
        }
        if (dimensionInformation.hasStructureType(StructureType.STRUCTURE_SCATTERED)) {
            this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, chunkX, chunkZ, null);
        }
    }
}