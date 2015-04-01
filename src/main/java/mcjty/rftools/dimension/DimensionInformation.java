package mcjty.rftools.dimension;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.description.*;
import mcjty.rftools.dimension.world.types.*;
import mcjty.rftools.items.dimlets.*;
import mcjty.rftools.items.dimlets.types.IDimletType;
import mcjty.rftools.network.ByteBufTools;
import mcjty.varia.BlockMeta;
import mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class DimensionInformation {
    private final DimensionDescriptor descriptor;
    private final String name;

    private Coordinate spawnPoint = null;

    private int probeCounter = 0;

    private long forcedDimensionSeed = 0;               // Seed was forced using a seed dimlet.
    private long baseSeed = 0;                          // World seed we start to generate own seed from.

    private int worldVersion = VERSION_OLD;             // Used for compatilibity checking between generated worlds.
    public static final int VERSION_OLD = 0;            // Old version of worlds. Seed is incorrect.
    public static final int VERSION_CORRECTSEED = 1;    // New version of worlds. Seed is correct.

    private TerrainType terrainType = TerrainType.TERRAIN_VOID;
    private BlockMeta baseBlockForTerrain = null;
    private Block fluidForTerrain = null;
    private BlockMeta tendrilBlock = null;
    private BlockMeta canyonBlock = null;
    private BlockMeta sphereBlock = null;
    private BlockMeta liquidSphereBlock = null;
    private Block liquidSphereFluid = null;

    private List<MobDescriptor> extraMobs = new ArrayList<MobDescriptor>();
    private boolean peaceful = false;
    private boolean shelter = false;
    private boolean respawnHere = false;

    private Set<FeatureType> featureTypes = new HashSet<FeatureType>();
    private BlockMeta[] extraOregen = new BlockMeta[] {};
    private Block[] fluidsForLakes = new Block[] {};

    private Set<StructureType> structureTypes = new HashSet<StructureType>();
    private Set<EffectType> effectTypes = new HashSet<EffectType>();

    private ControllerType controllerType = null;
    private List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
    private static final Map<Integer, Integer> biomeMapping = new HashMap<Integer, Integer>();

    private String digitString = "";

    private Float celestialAngle = null;
    private Float timeSpeed = null;

    private SkyDescriptor skyDescriptor;
    private List<CelestialBodyDescriptor> celestialBodyDescriptors;

    private String[] dimensionTypes = new String[0];    // Used for Recurrent Complex if that's present.

    private WeatherDescriptor weatherDescriptor;

    // The actual RF cost after taking into account the features we got in our world.
    private int actualRfCost;

    public DimensionInformation(String name, DimensionDescriptor descriptor, World world) {
        this.name = name;
        this.descriptor = descriptor;

        this.forcedDimensionSeed = descriptor.getForcedSeed();
        if (DimletConfiguration.randomizeSeed) {
            baseSeed = (long) (Math.random() * 10000 + 1);
        } else {
            baseSeed = world.getSeed();
        }

        worldVersion = VERSION_CORRECTSEED;

        setupFromDescriptor(world.getSeed());
        setupBiomeMapping();

        dump(null);
    }

    private void setupFromDescriptor(long seed) {
        List<Pair<DimletKey,List<DimletKey>>> dimlets = descriptor.getDimletsWithModifiers();

        Random random = new Random(descriptor.calculateSeed(seed));

        actualRfCost = 0;

        DimletType.DIMLET_TERRAIN.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_FEATURE.dimletType.constructDimension(dimlets, random, this);

        DimletType.DIMLET_STRUCTURE.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_BIOME.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_DIGIT.dimletType.constructDimension(dimlets, random, this);

        DimletType.DIMLET_SKY.dimletType.constructDimension(dimlets, random, this);

        DimletType.DIMLET_MOBS.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_SPECIAL.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_TIME.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_EFFECT.dimletType.constructDimension(dimlets, random, this);
        DimletType.DIMLET_WEATHER.dimletType.constructDimension(dimlets, random, this);

        actualRfCost += descriptor.getRfMaintainCost();
    }

    public String loadFromJson(String filename) {
        File file = new File(filename);
        String json;
        try {
            json = FileUtils.readFileToString(file);
        } catch (IOException e) {
            return "Error reading file!";
        }

        NBTTagCompound tagCompound;
        try {
            tagCompound = (NBTTagCompound) JsonToNBT.func_150315_a(json);
        } catch (NBTException e) {
            return "NBT Error: " + e.getMessage();
        }

        readFromNBT(tagCompound);

        return null;
    }

    private static StringBuffer appendIndent(StringBuffer buffer, int indent) {
        return buffer.append("                                                  ".substring(0, indent));
    }

    private static void convertNBTtoJson(StringBuffer buffer, NBTTagList tagList, int indent) {
        for (int i = 0 ; i < tagList.tagCount() ; i++) {
            NBTTagCompound compound = tagList.getCompoundTagAt(i);
            appendIndent(buffer, indent).append("{\n");
            convertNBTtoJson(buffer, compound, indent + 4);
            appendIndent(buffer, indent).append("},\n");
        }
    }

    private static void convertNBTtoJson(StringBuffer buffer, NBTTagCompound tagCompound, int indent) {
        boolean first = true;
        for (Object o : tagCompound.func_150296_c()) {
            if (!first) {
                buffer.append(",\n");
            }
            first = false;

            String key = (String) o;
            NBTBase tag = tagCompound.getTag(key);
            appendIndent(buffer, indent).append(key).append(':');
            if (tag instanceof NBTTagCompound) {
                NBTTagCompound compound = (NBTTagCompound) tag;
                buffer.append("{\n");
                convertNBTtoJson(buffer, compound, indent + 4);
                appendIndent(buffer, indent).append('}');
            } else if (tag instanceof NBTTagList) {
                NBTTagList list = (NBTTagList) tag;
                buffer.append("[\n");
                convertNBTtoJson(buffer, list, indent + 4);
                appendIndent(buffer, indent).append(']');
            } else {
                buffer.append(tag);
            }
        }
        if (!first) {
            buffer.append("\n");
        }
    }


    public String buildJson(String filename) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBT(tagCompound);
        StringBuffer buffer = new StringBuffer();
        buffer.append("{\n");
        convertNBTtoJson(buffer, tagCompound, 4);
        buffer.append("}");
        String json = buffer.toString();
//        String json = tagCompound.toString();

        try {
            FileWriter writer = new FileWriter(filename);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            return "Error writing file!";
        }

        return null;
    }

    public void injectDimlet(DimletKey key) {
        DimletType type = key.getType();
        IDimletType itype = type.dimletType;
        if (itype.isInjectable()) {
            addToCost(key);
            itype.inject(key, this);
        }
    }

    public DimensionInformation(DimensionDescriptor descriptor, NBTTagCompound tagCompound) {
        this.name = tagCompound.getString("name");
        this.descriptor = descriptor;

        setSpawnPoint(Coordinate.readFromNBT(tagCompound, "spawnPoint"));
        setProbeCounter(tagCompound.getInteger("probeCounter"));

        int version = tagCompound.getInteger("version");
        if (version == 1) {
            // This version of the dimension information has the random information persisted.
            setupFromNBT(tagCompound);
        } else {
            // This is an older version. Here we have to calculate the random information again.
            setupFromDescriptor(1);
        }

        setupBiomeMapping();
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        setSpawnPoint(Coordinate.readFromNBT(tagCompound, "spawnPoint"));
        setProbeCounter(tagCompound.getInteger("probeCounter"));
        setupFromNBT(tagCompound);

        setupBiomeMapping();
    }

    public static int[] getIntArraySafe(NBTTagCompound tagCompound, String name) {
        if (!tagCompound.hasKey(name)) {
            return new int[0];
        }
        NBTBase tag = tagCompound.getTag(name);
        if (tag instanceof NBTTagIntArray) {
            return tagCompound.getIntArray(name);
        } else {
            return new int[0];
        }
    }

    private void setupFromNBT(NBTTagCompound tagCompound) {
        terrainType = TerrainType.values()[tagCompound.getInteger("terrain")];
        featureTypes = toEnumSet(getIntArraySafe(tagCompound, "features"), FeatureType.values());
        structureTypes = toEnumSet(getIntArraySafe(tagCompound, "structures"), StructureType.values());
        effectTypes = toEnumSet(getIntArraySafe(tagCompound, "effects"), EffectType.values());

        biomes.clear();
        for (int a : getIntArraySafe(tagCompound, "biomes")) {
            biomes.add(BiomeGenBase.getBiome(a));
        }
        if (tagCompound.hasKey("controller")) {
            controllerType = ControllerType.values()[tagCompound.getInteger("controller")];
        } else {
            // Support for old type.
            if (biomes.isEmpty()) {
                controllerType = ControllerType.CONTROLLER_DEFAULT;
            } else {
                controllerType = ControllerType.CONTROLLER_SINGLE;
            }
        }

        digitString = tagCompound.getString("digits");

        forcedDimensionSeed = tagCompound.getLong("forcedSeed");
        baseSeed = tagCompound.getLong("baseSeed");
        worldVersion = tagCompound.getInteger("worldVersion");

        baseBlockForTerrain = getBlockMeta(tagCompound, "baseBlock");
        tendrilBlock = getBlockMeta(tagCompound, "tendrilBlock");
        sphereBlock = getBlockMeta(tagCompound, "sphereBlock");
        liquidSphereBlock = getBlockMeta(tagCompound, "liquidSphereBlock");
        liquidSphereFluid = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("liquidSphereFluid"));
        canyonBlock = getBlockMeta(tagCompound, "canyonBlock");
        fluidForTerrain = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("fluidBlock"));

        readOresFromNBT(tagCompound);
        readFluidsFromNBT(tagCompound);

        peaceful = tagCompound.getBoolean("peaceful");
        shelter = tagCompound.getBoolean("shelter");
        respawnHere = tagCompound.getBoolean("respawnHere");
        if (tagCompound.hasKey("celestialAngle")) {
            celestialAngle = tagCompound.getFloat("celestialAngle");
        } else {
            celestialAngle = null;
        }
        if (tagCompound.hasKey("timeSpeed")) {
            timeSpeed = tagCompound.getFloat("timeSpeed");
        } else {
            timeSpeed = null;
        }
        probeCounter = tagCompound.getInteger("probes");
        actualRfCost = tagCompound.getInteger("actualCost");

        skyDescriptor = new SkyDescriptor.Builder().fromNBT(tagCompound).build();
        calculateCelestialBodyDescriptors();

        weatherDescriptor = new WeatherDescriptor.Builder().fromNBT(tagCompound).build();

        extraMobs.clear();
        NBTTagList list = tagCompound.getTagList("mobs", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tc = list.getCompoundTagAt(i);
            String className = tc.getString("class");
            int chance = tc.getInteger("chance");
            int minGroup = tc.getInteger("minGroup");
            int maxGroup = tc.getInteger("maxGroup");
            int maxLoaded = tc.getInteger("maxLoaded");
            Class<? extends EntityLiving> c = null;
            try {
                c = (Class<? extends EntityLiving>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            MobDescriptor mob = new MobDescriptor(null, c, chance, minGroup, maxGroup, maxLoaded);
            extraMobs.add(mob);
        }

        String ds = tagCompound.getString("dimensionTypes");
        dimensionTypes = StringUtils.split(ds, ",");
        if (dimensionTypes == null) {
            dimensionTypes = new String[0];
        }
    }

    private void readFluidsFromNBT(NBTTagCompound tagCompound) {
        List<Block> ores = new ArrayList<Block>();
        for (int a : getIntArraySafe(tagCompound, "lakeFluids")) {
            ores.add((Block) Block.blockRegistry.getObjectById(a));
        }
        fluidsForLakes = ores.toArray(new Block[ores.size()]);
    }

    private void readOresFromNBT(NBTTagCompound tagCompound) {
        List<BlockMeta> ores = new ArrayList<BlockMeta>();
        int[] extraOregens = getIntArraySafe(tagCompound, "extraOregen");
        int[] extraOregenMetas = getIntArraySafe(tagCompound, "extraOregen_meta");
        for (int i = 0 ; i < extraOregens.length ; i++) {
            int id = extraOregens[i];
            Block block = (Block) Block.blockRegistry.getObjectById(id);
            int meta = 0;
            if (i < extraOregenMetas.length) {
                meta = extraOregenMetas[i];
            }
            ores.add(new BlockMeta(block, meta));
        }
        extraOregen = ores.toArray(new BlockMeta[ores.size()]);
    }

    private BlockMeta getBlockMeta(NBTTagCompound tagCompound, String name) {
        Block block = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger(name));
        int meta = tagCompound.getInteger(name+"_meta");
        return new BlockMeta(block, meta);
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setString("name", getName());
        Coordinate spawnPoint = getSpawnPoint();
        if (spawnPoint != null) {
            Coordinate.writeToNBT(tagCompound, "spawnPoint", spawnPoint);
        }
        tagCompound.setInteger("probeCounter", getProbeCounter());
        tagCompound.setInteger("version", 1);           // Version number so that we can detect incompatible changes in persisted dimension information objects.

        tagCompound.setInteger("terrain", terrainType == null ? TerrainType.TERRAIN_VOID.ordinal() : terrainType.ordinal());
        tagCompound.setIntArray("features", toIntArray(featureTypes));
        tagCompound.setIntArray("structures", toIntArray(structureTypes));
        tagCompound.setIntArray("effects", toIntArray(effectTypes));

        List<Integer> c = new ArrayList<Integer>(biomes.size());
        for (BiomeGenBase t : biomes) {
            c.add(t.biomeID);
        }
        tagCompound.setIntArray("biomes", ArrayUtils.toPrimitive(c.toArray(new Integer[c.size()])));
        tagCompound.setInteger("controller", controllerType == null ? ControllerType.CONTROLLER_DEFAULT.ordinal() : controllerType.ordinal());
        tagCompound.setString("digits", digitString);

        tagCompound.setLong("forcedSeed", forcedDimensionSeed);
        tagCompound.setLong("baseSeed", baseSeed);
        tagCompound.setInteger("worldVersion", worldVersion);

        setBlockMeta(tagCompound, baseBlockForTerrain, "baseBlock");
        setBlockMeta(tagCompound, tendrilBlock, "tendrilBlock");
        setBlockMeta(tagCompound, sphereBlock, "sphereBlock");
        setBlockMeta(tagCompound, liquidSphereBlock, "liquidSphereBlock");
        tagCompound.setInteger("liquidSphereFluid", Block.blockRegistry.getIDForObject(liquidSphereFluid));
        setBlockMeta(tagCompound, canyonBlock, "canyonBlock");
        tagCompound.setInteger("fluidBlock", Block.blockRegistry.getIDForObject(fluidForTerrain));

        writeOresToNBT(tagCompound);
        writeFluidsToNBT(tagCompound);

        tagCompound.setBoolean("peaceful", peaceful);
        tagCompound.setBoolean("shelter", shelter);
        tagCompound.setBoolean("respawnHere", respawnHere);
        if (celestialAngle != null) {
            tagCompound.setFloat("celestialAngle", celestialAngle);
        }
        if (timeSpeed != null) {
            tagCompound.setFloat("timeSpeed", timeSpeed);
        }
        tagCompound.setInteger("probes", probeCounter);
        tagCompound.setInteger("actualCost", actualRfCost);

        skyDescriptor.writeToNBT(tagCompound);
        weatherDescriptor.writeToNBT(tagCompound);

        NBTTagList list = new NBTTagList();
        for (MobDescriptor mob : extraMobs) {
            NBTTagCompound tc = new NBTTagCompound();

            if (mob != null) {
                if (mob.getEntityClass() != null) {
                    tc.setString("class", mob.getEntityClass().getName());
                    tc.setInteger("chance", mob.getSpawnChance());
                    tc.setInteger("minGroup", mob.getMinGroup());
                    tc.setInteger("maxGroup", mob.getMaxGroup());
                    tc.setInteger("maxLoaded", mob.getMaxLoaded());
                    list.appendTag(tc);
                }
            }
        }

        tagCompound.setTag("mobs", list);
        tagCompound.setString("dimensionTypes", StringUtils.join(dimensionTypes, ","));
    }

    private void setBlockMeta(NBTTagCompound tagCompound, BlockMeta blockMeta, String name) {
        tagCompound.setInteger(name, Block.blockRegistry.getIDForObject(blockMeta.getBlock()));
        tagCompound.setInteger(name+"_meta", blockMeta.getMeta());
    }

    private void writeFluidsToNBT(NBTTagCompound tagCompound) {
        List<Integer> c;
        c = new ArrayList<Integer>(fluidsForLakes.length);
        for (Block t : fluidsForLakes) {
            c.add(Block.blockRegistry.getIDForObject(t));
        }
        tagCompound.setIntArray("lakeFluids", ArrayUtils.toPrimitive(c.toArray(new Integer[c.size()])));
    }

    private void writeOresToNBT(NBTTagCompound tagCompound) {
        List<Integer> ids = new ArrayList<Integer>(extraOregen.length);
        List<Integer> meta = new ArrayList<Integer>(extraOregen.length);
        for (BlockMeta t : extraOregen) {
            ids.add(Block.blockRegistry.getIDForObject(t.getBlock()));
            meta.add((int)t.getMeta());
        }
        tagCompound.setIntArray("extraOregen", ArrayUtils.toPrimitive(ids.toArray(new Integer[ids.size()])));
        tagCompound.setIntArray("extraOregen_meta", ArrayUtils.toPrimitive(meta.toArray(new Integer[meta.size()])));
    }

    private static <T extends Enum> int[] toIntArray(Collection<T> collection) {
        List<Integer> c = new ArrayList<Integer>(collection.size());
        for (T t : collection) {
            c.add(t.ordinal());
        }
        return ArrayUtils.toPrimitive(c.toArray(new Integer[c.size()]));
    }

    private static <T extends Enum> Set<T> toEnumSet(int[] arr, T[] values) {
        Set<T> list = new HashSet<T>();
        for (int a : arr) {
            list.add(values[a]);
        }
        return list;
    }


    private void logDebug(EntityPlayer player, String message) {
        if (player == null) {
            RFTools.log(message);
        } else {
            RFTools.message(player, EnumChatFormatting.YELLOW + message);
        }
    }

    public void dump(EntityPlayer player) {
        String digits = getDigitString();
        if (!digits.isEmpty()) {
            logDebug(player, "    Digits: " + digits);
        }
        if (forcedDimensionSeed != 0) {
            logDebug(player, "    Forced seed: " + forcedDimensionSeed);
        }
        if (baseSeed != 0) {
            logDebug(player, "    Base seed: " + baseSeed);
        }
        logDebug(player, "    World version: " + worldVersion);
        TerrainType terrainType = getTerrainType();
        logDebug(player, "    Terrain: " + terrainType.toString());
        logDebug(player, "        Base block: " + new ItemStack(baseBlockForTerrain.getBlock(), 1, baseBlockForTerrain.getMeta()).getDisplayName());
        if (featureTypes.contains(FeatureType.FEATURE_TENDRILS)) {
            logDebug(player, "        Tendril block: " + new ItemStack(tendrilBlock.getBlock(), 1, tendrilBlock.getMeta()).getDisplayName());
        }
        if (featureTypes.contains(FeatureType.FEATURE_ORBS)) {
            logDebug(player, "        Orbs block: " + new ItemStack(sphereBlock.getBlock(), 1, sphereBlock.getMeta()).getDisplayName());
        }
        if (featureTypes.contains(FeatureType.FEATURE_LIQUIDORBS)) {
            logDebug(player, "        Liquid Orbs block: " + new ItemStack(liquidSphereBlock.getBlock(), 1, liquidSphereBlock.getMeta()).getDisplayName());
        }
        if (featureTypes.contains(FeatureType.FEATURE_CANYONS)) {
            logDebug(player, "        Canyon block: " + new ItemStack(canyonBlock.getBlock(), 1, canyonBlock.getMeta()).getDisplayName());
        }
        logDebug(player, "        Base fluid: " + new ItemStack(fluidForTerrain).getDisplayName());
        logDebug(player, "    Biome controller: " + (controllerType == null ? "<null>" : controllerType.name()));
        for (BiomeGenBase biome : getBiomes()) {
            logDebug(player, "    Biome: " + biome.biomeName);
        }
        for (FeatureType featureType : getFeatureTypes()) {
            logDebug(player, "    Feature: " + featureType.toString());
        }
        for (BlockMeta block : extraOregen) {
            if (block != null) {
                logDebug(player, "        Extra ore: " + new ItemStack(block.getBlock(), 1, block.getMeta()).getDisplayName());
            }
        }
        for (Block block : fluidsForLakes) {
            logDebug(player, "        Lake fluid: " + new ItemStack(block).getDisplayName());
        }
        if (featureTypes.contains(FeatureType.FEATURE_LIQUIDORBS)) {
            logDebug(player, "        Liquid orb fluid: " + new ItemStack(liquidSphereFluid).getDisplayName());

        }
        for (StructureType structureType : getStructureTypes()) {
            logDebug(player, "    Structure: " + structureType.toString());
        }
        if (structureTypes.contains(StructureType.STRUCTURE_RECURRENTCOMPLEX)) {
            for (String type : dimensionTypes) {
                logDebug(player, "    RR DimensionType: " + type);
            }
        }
        for (EffectType effectType : getEffectTypes()) {
            logDebug(player, "    Effect: " + effectType.toString());
        }
        logDebug(player, "    Sun brightness: " + skyDescriptor.getSunBrightnessFactor());
        logDebug(player, "    Star brightness: " + skyDescriptor.getStarBrightnessFactor());
        float r = skyDescriptor.getSkyColorFactorR();
        float g = skyDescriptor.getSkyColorFactorG();
        float b = skyDescriptor.getSkyColorFactorB();
        logDebug(player, "    Sky color: " + r + ", " + g + ", " + b);
        r = skyDescriptor.getFogColorFactorR();
        g = skyDescriptor.getFogColorFactorG();
        b = skyDescriptor.getFogColorFactorB();
        logDebug(player, "    Fog color: " + r + ", " + g + ", " + b);
        SkyType skyType = skyDescriptor.getSkyType();
        if (skyType != SkyType.SKY_NORMAL) {
            logDebug(player, "    Sky type: " + skyType.toString());
        }
        for (CelestialBodyType bodyType : skyDescriptor.getCelestialBodies()) {
            logDebug(player, "    Sky body: " + bodyType.name());
        }

        if (weatherDescriptor.getRainStrength() > -0.5f) {
            logDebug(player, "    Weather rain: " + weatherDescriptor.getRainStrength());
        }
        if (weatherDescriptor.getThunderStrength() > -0.5f) {
            logDebug(player, "    Weather thunder " + weatherDescriptor.getThunderStrength());
        }


        for (MobDescriptor mob : extraMobs) {
            if (mob != null) {
                if (mob.getEntityClass() == null) {
                    logDebug(player, "    Mob: " + mob);
                } else {
                    logDebug(player, "    Mob: " + mob.getEntityClass().getName());
                }
            }
        }
        if (peaceful) {
            logDebug(player, "    Peaceful mode");
        }
        if (shelter) {
            logDebug(player, "    Safe shelter");
        }
        if (respawnHere) {
            logDebug(player, "    Respawn local");
        }
        if (celestialAngle != null) {
            logDebug(player, "    Celestial angle: " + celestialAngle);
        }
        if (timeSpeed != null) {
            logDebug(player, "    Time speed: " + timeSpeed);
        }
        if (probeCounter > 0) {
            logDebug(player, "    Probes: " + probeCounter);
        }
    }

    public void toBytes(ByteBuf buf) {
        ByteBufTools.writeEnum(buf, terrainType, TerrainType.TERRAIN_VOID);
        ByteBufTools.writeEnumCollection(buf, featureTypes);
        ByteBufTools.writeEnumCollection(buf, structureTypes);
        ByteBufTools.writeEnumCollection(buf, effectTypes);

        buf.writeInt(biomes.size());
        for (BiomeGenBase entry : biomes) {
            buf.writeInt(entry.biomeID);
        }
        ByteBufTools.writeEnum(buf, controllerType, ControllerType.CONTROLLER_DEFAULT);

        ByteBufTools.writeString(buf, digitString);
        buf.writeLong(forcedDimensionSeed);
        buf.writeLong(baseSeed);
        buf.writeInt(worldVersion);

        buf.writeInt(Block.blockRegistry.getIDForObject(baseBlockForTerrain.getBlock()));
        buf.writeInt(baseBlockForTerrain.getMeta());
        buf.writeInt(Block.blockRegistry.getIDForObject(tendrilBlock.getBlock()));
        buf.writeInt(tendrilBlock.getMeta());
        buf.writeInt(Block.blockRegistry.getIDForObject(sphereBlock.getBlock()));
        buf.writeInt(sphereBlock.getMeta());
        buf.writeInt(Block.blockRegistry.getIDForObject(liquidSphereBlock.getBlock()));
        buf.writeInt(liquidSphereBlock.getMeta());
        buf.writeInt(Block.blockRegistry.getIDForObject(liquidSphereFluid));
        buf.writeInt(Block.blockRegistry.getIDForObject(canyonBlock.getBlock()));
        buf.writeInt(canyonBlock.getMeta());
        buf.writeInt(Block.blockRegistry.getIDForObject(fluidForTerrain));

        buf.writeInt(extraOregen.length);
        for (BlockMeta block : extraOregen) {
            buf.writeInt(Block.blockRegistry.getIDForObject(block.getBlock()));
            buf.writeInt(block.getMeta());
        }
        buf.writeInt(fluidsForLakes.length);
        for (Block block : fluidsForLakes) {
            buf.writeInt(Block.blockRegistry.getIDForObject(block));
        }

        buf.writeBoolean(peaceful);
        buf.writeBoolean(shelter);
        buf.writeBoolean(respawnHere);
        ByteBufTools.writeFloat(buf, celestialAngle);
        ByteBufTools.writeFloat(buf, timeSpeed);

        buf.writeInt(probeCounter);
        buf.writeInt(actualRfCost);

        skyDescriptor.toBytes(buf);
        weatherDescriptor.toBytes(buf);

        buf.writeInt(extraMobs.size());
        for (MobDescriptor mob : extraMobs) {
            if (mob != null) {
                if (mob.getEntityClass() != null) {
                    ByteBufTools.writeString(buf, mob.getEntityClass().getName());
                    buf.writeInt(mob.getSpawnChance());
                    buf.writeInt(mob.getMinGroup());
                    buf.writeInt(mob.getMaxGroup());
                    buf.writeInt(mob.getMaxLoaded());
                }
            }
        }

        buf.writeInt(dimensionTypes.length);
        for (String type : dimensionTypes) {
            ByteBufTools.writeString(buf, type);
        }

    }

    public DimensionInformation(String name, DimensionDescriptor descriptor, ByteBuf buf) {
        this.name = name;
        this.descriptor = descriptor;

        terrainType = ByteBufTools.readEnum(buf, TerrainType.values());
        ByteBufTools.readEnumCollection(buf, featureTypes, FeatureType.values());
        ByteBufTools.readEnumCollection(buf, structureTypes, StructureType.values());
        ByteBufTools.readEnumCollection(buf, effectTypes, EffectType.values());

        biomes.clear();
        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            biomes.add(BiomeGenBase.getBiome(buf.readInt()));
        }
        controllerType = ByteBufTools.readEnum(buf, ControllerType.values());
        digitString = ByteBufTools.readString(buf);

        forcedDimensionSeed = buf.readLong();
        baseSeed = buf.readLong();
        worldVersion = buf.readInt();

        Block block = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        int meta = buf.readInt();
        baseBlockForTerrain = new BlockMeta(block, meta);
        block = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        meta = buf.readInt();
        tendrilBlock = new BlockMeta(block, meta);

        block = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        meta = buf.readInt();
        sphereBlock = new BlockMeta(block, meta);
        block = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        meta = buf.readInt();
        liquidSphereBlock = new BlockMeta(block, meta);
        liquidSphereFluid = (Block) Block.blockRegistry.getObjectById(buf.readInt());

        block = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        meta = buf.readInt();
        canyonBlock = new BlockMeta(block, meta);
        fluidForTerrain = (Block) Block.blockRegistry.getObjectById(buf.readInt());

        size = buf.readInt();
        List<BlockMeta> blocksMeta = new ArrayList<BlockMeta>();
        for (int i = 0 ; i < size ; i++) {
            Block b = (Block) Block.blockRegistry.getObjectById(buf.readInt());
            int m = buf.readInt();
            blocksMeta.add(new BlockMeta(b, m));
        }
        extraOregen = blocksMeta.toArray(new BlockMeta[blocksMeta.size()]);

        List<Block> blocks = new ArrayList<Block>();
        size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            blocks.add((Block) Block.blockRegistry.getObjectById(buf.readInt()));
        }
        fluidsForLakes = blocks.toArray(new Block[blocks.size()]);

        peaceful = buf.readBoolean();
        shelter = buf.readBoolean();
        respawnHere = buf.readBoolean();

        celestialAngle = ByteBufTools.readFloat(buf);
        timeSpeed = ByteBufTools.readFloat(buf);

        probeCounter = buf.readInt();
        actualRfCost = buf.readInt();

        skyDescriptor = new SkyDescriptor.Builder().fromBytes(buf).build();
        calculateCelestialBodyDescriptors();

        weatherDescriptor = new WeatherDescriptor.Builder().fromBytes(buf).build();

        extraMobs.clear();
        size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            String className = ByteBufTools.readString(buf);
            try {
                Class<? extends EntityLiving> c = (Class<? extends EntityLiving>) Class.forName(className);
                int chance = buf.readInt();
                int minGroup = buf.readInt();
                int maxGroup = buf.readInt();
                int maxLoaded = buf.readInt();
                MobDescriptor mob = new MobDescriptor(null, c, chance, minGroup, maxGroup, maxLoaded);
                extraMobs.add(mob);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        size = buf.readInt();
        dimensionTypes = new String[size];
        for (int i = 0 ; i < size ; i++) {
            dimensionTypes[i] = ByteBufTools.readString(buf);
        }

        setupBiomeMapping();
    }

    public Coordinate getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Coordinate spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public void setSkyDescriptor(SkyDescriptor sd) {
        skyDescriptor = sd;
        calculateCelestialBodyDescriptors();
    }

    private void calculateCelestialBodyDescriptors() {
        List<CelestialBodyType> celestialBodies = skyDescriptor.getCelestialBodies();
        // Find the most suitable sun and moon. This is typically the largest sun in the list of celestial bodies.
        int sunidx = -1;
        int bestsun = 0;
        int moonidx = -1;
        int bestmoon = 0;
        for (int i = 0 ; i < celestialBodies.size() ; i++) {
            CelestialBodyType type = celestialBodies.get(i);
            if (type.getGoodSunFactor() > bestsun) {
                bestsun = type.getGoodSunFactor();
                sunidx = i;
            }
            if (type.getGoodMoonFactor() > bestmoon) {
                bestmoon = type.getGoodMoonFactor();
                moonidx = i;
            }
        }

        // Always the same random series.
        Random random = new Random(123);
        random.nextFloat();
        celestialBodyDescriptors = new ArrayList<CelestialBodyDescriptor>();
        for (int i = 0 ; i < celestialBodies.size() ; i++) {
            CelestialBodyType type = celestialBodies.get(i);
            if (i == sunidx || i == moonidx) {
                celestialBodyDescriptors.add(new CelestialBodyDescriptor(type, 0.0f, 1.0f, -90.0f));
            } else {
                celestialBodyDescriptors.add(new CelestialBodyDescriptor(type, random.nextFloat() * 100.0f, random.nextFloat() * 3.0f, -random.nextFloat() * 180.0f));
            }
        }
    }

    public static List<Pair<DimletKey,List<DimletKey>>> extractType(DimletType type, List<Pair<DimletKey,List<DimletKey>>> dimlets) {
        List<Pair<DimletKey,List<DimletKey>>> result = new ArrayList<Pair<DimletKey, List<DimletKey>>>();
        for (Pair<DimletKey, List<DimletKey>> dimlet : dimlets) {
            if (dimlet.getLeft().getType() == type) {
                result.add(dimlet);
            }
        }
        return result;
    }

    public void updateCostFactor(DimletKey key) {
        actualRfCost += calculateCostFactor(key);
    }

    private int calculateCostFactor(DimletKey key) {
        DimletEntry dimletEntry = KnownDimletConfiguration.getEntry(key);
        if (dimletEntry == null) {
            RFTools.logError("Something went wrong for key: "+key);
            return 0;
        }
        return (int) (dimletEntry.getRfMaintainCost() * DimletConfiguration.afterCreationCostFactor);
    }

    private void addToCost(DimletKey key) {
        DimletEntry dimletEntry = KnownDimletConfiguration.getEntry(key);
        int rfMaintainCost = dimletEntry.getRfMaintainCost();

        if (rfMaintainCost < 0) {
            actualRfCost = actualRfCost - (actualRfCost * (-rfMaintainCost) / 100);
            if (actualRfCost < 10) {
                actualRfCost = 10;        // Never consume less then 10 RF/tick
            }
        } else {
            actualRfCost += rfMaintainCost;
        }
    }

    public static void getMaterialAndFluidModifiers(List<DimletKey> modifiers, List<BlockMeta> blocks, List<Block> fluids) {
        if (modifiers != null) {
            for (DimletKey modifier : modifiers) {
                if (modifier.getType() == DimletType.DIMLET_MATERIAL) {
                    BlockMeta block = DimletObjectMapping.idToBlock.get(modifier);
                    blocks.add(block);
                } else if (modifier.getType() == DimletType.DIMLET_LIQUID) {
                    Block fluid = DimletObjectMapping.idToFluid.get(modifier);
                    fluids.add(fluid);
                }
            }
        }
    }

    public BlockMeta getFeatureBlock(Random random, Map<FeatureType, List<DimletKey>> modifiersForFeature, FeatureType featureType) {
        BlockMeta block;
        if (featureTypes.contains(featureType)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(featureType), blocks, fluids);

            if (!blocks.isEmpty()) {
                block = blocks.get(random.nextInt(blocks.size()));
                if (block == null) {
                    block = new BlockMeta(Blocks.stone, 0);     // This is the default in case None was specified.
                }
            } else {
                // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
                if (random.nextFloat() < DimletConfiguration.randomFeatureMaterialChance) {
                    DimletKey key = DimletRandomizer.getRandomMaterialBlock(random, true);
                    actualRfCost += calculateCostFactor(key);
                    block = DimletObjectMapping.idToBlock.get(key);
                } else {
                    block = new BlockMeta(Blocks.stone, 0);
                }
            }
        } else {
            block = new BlockMeta(Blocks.stone, 0);
        }
        return block;
    }

    public Block getFeatureLiquid(Random random, Map<FeatureType, List<DimletKey>> modifiersForFeature, FeatureType featureType) {
        Block block;
        if (featureTypes.contains(featureType)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(featureType), blocks, fluids);

            if (!fluids.isEmpty()) {
                block = fluids.get(random.nextInt(fluids.size()));
                if (block == null) {
                    block = Blocks.water;     // This is the default in case None was specified.
                }
            } else {
                // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
                if (random.nextFloat() < DimletConfiguration.randomOrbFluidChance) {
                    DimletKey key = DimletRandomizer.getRandomFluidBlock(random, true);
                    actualRfCost += calculateCostFactor(key);
                    block = DimletObjectMapping.idToFluid.get(key);
                } else {
                    block = Blocks.water;
                }
            }
        } else {
            block = Blocks.water;
        }
        return block;
    }



    private void setupBiomeMapping() {
        biomeMapping.clear();
        if (controllerType == ControllerType.CONTROLLER_FILTERED) {
            BiomeGenBase[] biomeGenArray = BiomeGenBase.getBiomeGenArray();
            final Set<Integer> ids = new HashSet<Integer>();
            for (BiomeGenBase biome : biomes) {
                ids.add(biome.biomeID);
            }

            ControllerType.BiomeFilter biomeFilter = new ControllerType.BiomeFilter() {
                @Override
                public boolean match(BiomeGenBase biome) {
                    return ids.contains(biome.biomeID);
                }

                @Override
                public double calculateBiomeDistance(BiomeGenBase a, BiomeGenBase b) {
                    return calculateBiomeDistance(a, b, false, false, false);
                }
            };
            BiomeControllerMapping.makeFilteredBiomeMap(biomeGenArray, biomeMapping, biomeFilter);
        }
    }

    public DimensionDescriptor getDescriptor() {
        return descriptor;
    }

    public String getName() {
        return name;
    }

    public TerrainType getTerrainType() {
        return terrainType;
    }

    public void setTerrainType(TerrainType type) {
        terrainType = type;
    }

    public boolean hasFeatureType(FeatureType type) {
        return featureTypes.contains(type);
    }

    public Set<FeatureType> getFeatureTypes() {
        return featureTypes;
    }

    public boolean hasStructureType(StructureType type) {
        return structureTypes.contains(type);
    }

    public Set<StructureType> getStructureTypes() {
        return structureTypes;
    }

    public boolean hasEffectType(EffectType type) {
        return effectTypes.contains(type);
    }

    public Set<EffectType> getEffectTypes() {
        return effectTypes;
    }

    public List<BiomeGenBase> getBiomes() {
        return biomes;
    }

    public Map<Integer, Integer> getBiomeMapping() {
        if (biomeMapping.isEmpty()) {
            setupBiomeMapping();
        }
        return biomeMapping;
    }

    public ControllerType getControllerType() {
        return controllerType;
    }

    public void setControllerType(ControllerType controllerType) {
        this.controllerType = controllerType;
    }

    public String getDigitString() {
        return digitString;
    }

    public void setDigitString(String digitString) {
        this.digitString = digitString;
    }

    public BlockMeta getBaseBlockForTerrain() {
        return baseBlockForTerrain;
    }

    public void setBaseBlockForTerrain(BlockMeta blockMeta) { baseBlockForTerrain = blockMeta; }

    public BlockMeta getTendrilBlock() {
        return tendrilBlock;
    }

    public void setTendrilBlock(BlockMeta block) {
        tendrilBlock = block;
    }

    public BlockMeta getCanyonBlock() {
        return canyonBlock;
    }

    public void setCanyonBlock(BlockMeta canyonBlock) {
        this.canyonBlock = canyonBlock;
    }

    public BlockMeta getSphereBlock() {
        return sphereBlock;
    }

    public void setSphereBlock(BlockMeta sphereBlock) {
        this.sphereBlock = sphereBlock;
    }

    public BlockMeta getLiquidSphereBlock() {
        return liquidSphereBlock;
    }

    public void setLiquidSphereBlock(BlockMeta liquidSphereBlock) {
        this.liquidSphereBlock = liquidSphereBlock;
    }

    public Block getLiquidSphereFluid() {
        return liquidSphereFluid;
    }

    public void setLiquidSphereFluid(Block liquidSphereFluid) {
        this.liquidSphereFluid = liquidSphereFluid;
    }

    public BlockMeta[] getExtraOregen() {
        return extraOregen;
    }

    public void setExtraOregen(BlockMeta[] blocks) {
        extraOregen = blocks;
    }

    public Block getFluidForTerrain() {
        return fluidForTerrain;
    }

    public void setFluidForTerrain(Block block) { fluidForTerrain = block; }

    public Block[] getFluidsForLakes() {
        return fluidsForLakes;
    }

    public void setFluidsForLakes(Block[] blocks) {
        fluidsForLakes = blocks;
    }

    public SkyDescriptor getSkyDescriptor() {
        return skyDescriptor;
    }

    public WeatherDescriptor getWeatherDescriptor() {
        return weatherDescriptor;
    }

    public void setWeatherDescriptor(WeatherDescriptor weatherDescriptor) {
        this.weatherDescriptor = weatherDescriptor;
    }

    public List<CelestialBodyDescriptor> getCelestialBodyDescriptors() {
        return celestialBodyDescriptors;
    }

    public String[] getDimensionTypes() {
        return dimensionTypes;
    }

    public void setDimensionTypes(String[] dimensionTypes) {
        this.dimensionTypes = dimensionTypes;
    }

    public List<MobDescriptor> getExtraMobs() {
        return extraMobs;
    }

    public boolean isPeaceful() {
        return peaceful;
    }

    public void setPeaceful(boolean peaceful) {
        this.peaceful = peaceful;
    }

    public boolean isShelter() {
        return shelter;
    }

    public void setShelter(boolean shelter) {
        this.shelter = shelter;
    }

    public boolean isRespawnHere() {
        return respawnHere;
    }

    public void setRespawnHere(boolean respawnHere) {
        this.respawnHere = respawnHere;
    }

    public Float getCelestialAngle() {
        return celestialAngle;
    }

    public Float getTimeSpeed() {
        return timeSpeed;
    }

    public void setCelestialAngle(Float celestialAngle) {
        this.celestialAngle = celestialAngle;
    }

    public void setTimeSpeed(Float timeSpeed) {
        this.timeSpeed = timeSpeed;
    }

    public void addProbe() {
        probeCounter++;
    }

    public void removeProbe() {
        probeCounter--;
        if (probeCounter < 0) {
            probeCounter = 0;
        }
    }

    public int getProbeCounter() {
        return probeCounter;
    }

    public void setProbeCounter(int probeCounter) {
        this.probeCounter = probeCounter;
    }

    public int getActualRfCost() {
        return actualRfCost;
    }

    public long getForcedDimensionSeed() {
        return forcedDimensionSeed;
    }

    public long getBaseSeed() {
        return baseSeed;
    }

    public int getWorldVersion() {
        return worldVersion;
    }
}
