package com.mcjty.rftools.dimension;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.description.*;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.rftools.items.dimlets.*;
import com.mcjty.rftools.network.ByteBufTools;
import com.mcjty.varia.BlockMeta;
import com.mcjty.varia.Coordinate;
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
        List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets = descriptor.getDimletsWithModifiers();

        Random random = new Random(descriptor.calculateSeed(seed));

        actualRfCost = 0;

        calculateTerrainType(dimlets, random);

        calculateFeatureType(dimlets, random);

        calculateStructureType(dimlets, random);
        calculateBiomes(dimlets, random);
        calculateDigitString(dimlets);

        calculateSky(dimlets, random);

        calculateMobs(dimlets, random);
        calculateSpecial(dimlets);
        calculateTime(dimlets, random);
        calculateEffects(dimlets, random);
        calculateWeather(dimlets, random);

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
        switch (type) {
            case DIMLET_BIOME:
            case DIMLET_FOLIAGE:
            case DIMLET_LIQUID:
            case DIMLET_MATERIAL:
            case DIMLET_STRUCTURE:
            case DIMLET_TERRAIN:
            case DIMLET_DIGIT:
            case DIMLET_FEATURE:
            case DIMLET_CONTROLLER:
                // Not supported
                return;
            case DIMLET_MOBS:
                injectMobDimlet(key);
                break;
            case DIMLET_SKY:
                injectSkyDimlet(key);
                break;
            case DIMLET_TIME:
                injectTimeDimlet(key);
                break;
            case DIMLET_SPECIAL:
                injectSpecialDimlet(key);
                break;
            case DIMLET_EFFECT:
                injectEffectDimlet(key);
                break;
            case DIMLET_WEATHER:
                injectWeatherDimlet(key);
                break;
        }
    }

    private void injectMobDimlet(DimletKey key) {
        addToCost(key);
        MobDescriptor mobDescriptor = DimletObjectMapping.idtoMob.get(key);
        if (mobDescriptor != null && mobDescriptor.getEntityClass() != null) {
            extraMobs.add(mobDescriptor);
        }
    }

    private void injectSkyDimlet(DimletKey key) {
        addToCost(key);
        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        builder.combine(skyDescriptor);
        SkyDescriptor newDescriptor = DimletObjectMapping.idToSkyDescriptor.get(key);
        if (newDescriptor.specifiesFogColor()) {
            builder.resetFogColor();
        }
        if (newDescriptor.specifiesSkyColor()) {
            builder.resetSkyColor();
        }
        builder.combine(newDescriptor);
        skyDescriptor = builder.build();
        calculateCelestialBodyDescriptors();
    }

    private void injectTimeDimlet(DimletKey key) {
        addToCost(key);
        celestialAngle = DimletObjectMapping.idToCelestialAngle.get(key);
        timeSpeed = DimletObjectMapping.idToSpeed.get(key);
    }

    private void injectWeatherDimlet(DimletKey key) {
        addToCost(key);
        WeatherDescriptor.Builder builder = new WeatherDescriptor.Builder();
        builder.combine(weatherDescriptor);
        WeatherDescriptor newDescriptor = DimletObjectMapping.idToWeatherDescriptor.get(key);
        builder.combine(newDescriptor);
        weatherDescriptor = builder.build();
    }

    private void injectEffectDimlet(DimletKey key) {
        addToCost(key);
        effectTypes.add(DimletObjectMapping.idToEffectType.get(key));
    }

    private void injectSpecialDimlet(DimletKey key) {
        addToCost(key);
        if (DimletObjectMapping.idToSpecialType.get(key) == SpecialType.SPECIAL_PEACEFUL) {
            peaceful = true;
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
            MobDescriptor mob = new MobDescriptor(c, chance, minGroup, maxGroup, maxLoaded);
            extraMobs.add(mob);
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
            logDebug(player, "        Extra ore: " + new ItemStack(block.getBlock(), 1, block.getMeta()).getDisplayName());
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
                MobDescriptor mob = new MobDescriptor(c, chance, minGroup, maxGroup, maxLoaded);
                extraMobs.add(mob);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        setupBiomeMapping();
    }

    public Coordinate getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Coordinate spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    private void calculateTime(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_TIME, dimlets);
        if (dimlets.isEmpty()) {
            if (random.nextFloat() < DimletConfiguration.randomSpecialTimeChance) {
                celestialAngle = null;      // Default
                timeSpeed = null;
            } else {
                List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToCelestialAngle.keySet());
                DimletKey key = keys.get(random.nextInt(keys.size()));
                celestialAngle = DimletObjectMapping.idToCelestialAngle.get(key);
                timeSpeed = DimletObjectMapping.idToSpeed.get(key);
            }
        } else {
            DimletKey key = dimlets.get(random.nextInt(dimlets.size())).getKey().getKey();
            celestialAngle = DimletObjectMapping.idToCelestialAngle.get(key);
            timeSpeed = DimletObjectMapping.idToSpeed.get(key);
        }
    }

    private void calculateWeather(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_WEATHER, dimlets);
        WeatherDescriptor.Builder builder = new WeatherDescriptor.Builder();
        if (dimlets.isEmpty()) {
            while (random.nextFloat() > DimletConfiguration.randomWeatherChance) {
                List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToWeatherDescriptor.keySet());
                DimletKey key = keys.get(random.nextInt(keys.size()));
                builder.combine(DimletObjectMapping.idToWeatherDescriptor.get(key));
            }
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
                DimletKey key = dimlet.getKey().getKey();
                builder.combine(DimletObjectMapping.idToWeatherDescriptor.get(key));
            }
        }
        weatherDescriptor = builder.build();
    }

    private void calculateSpecial(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets) {
        dimlets = extractType(DimletType.DIMLET_SPECIAL, dimlets);
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            DimletKey key = dimlet.getLeft().getKey();
            SpecialType specialType = DimletObjectMapping.idToSpecialType.get(key);
            if (specialType == SpecialType.SPECIAL_PEACEFUL) {
                peaceful = true;
            } else if (specialType == SpecialType.SPECIAL_SHELTER) {
                shelter = true;
            }
        }
    }

    private void calculateMobs(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_MOBS, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomExtraMobsChance) {
                DimletKey key = DimletRandomizer.getRandomMob(random, false);
                actualRfCost += calculateCostFactor(key);
                extraMobs.add(DimletObjectMapping.idtoMob.get(key));
            }
        } else {
            DimletKey key = dimlets.get(0).getLeft().getKey();
            if (dimlets.size() == 1 && DimletObjectMapping.idtoMob.get(key).getEntityClass() == null) {
                // Just default.
            } else {
                for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
                    DimletKey modifierKey = dimletWithModifiers.getLeft().getKey();
                    MobDescriptor descriptor = DimletObjectMapping.idtoMob.get(modifierKey);
                    if (descriptor.getEntityClass() != null) {
                        extraMobs.add(descriptor);
                    }
                }
            }
        }
    }

    private void calculateDigitString(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets) {
        dimlets = extractType(DimletType.DIMLET_DIGIT, dimlets);
        digitString = "";
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            DimletKey key = dimletWithModifiers.getKey().getKey();
            digitString += DimletObjectMapping.idToDigit.get(key);
        }
    }

    private void calculateEffects(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_EFFECT, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomEffectChance) {
                DimletKey key = DimletRandomizer.getRandomEffect(random, false);
                EffectType effectType = DimletObjectMapping.idToEffectType.get(key);
                if (!effectTypes.contains(effectType)) {
                    actualRfCost += calculateCostFactor(key);
                    effectTypes.add(effectType);
                }
            }
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifier : dimlets) {
                DimletKey key = dimletWithModifier.getLeft().getKey();
                EffectType effectType = DimletObjectMapping.idToEffectType.get(key);
                if (effectType != EffectType.EFFECT_NONE) {
                    effectTypes.add(effectType);
                }
            }
        }
    }

    private void calculateSky(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_SKY, dimlets);
        if (dimlets.isEmpty()) {
            if (random.nextFloat() < DimletConfiguration.randomSpecialSkyChance) {
                // If nothing was specified then there is random chance we get random sky stuff.
                List<DimletKey> skyIds = new ArrayList<DimletKey>(DimletObjectMapping.idToSkyDescriptor.keySet());
                for (int i = 0 ; i < 1+random.nextInt(3) ; i++) {
                    DimletKey key = skyIds.get(random.nextInt(skyIds.size()));
                    List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                    dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_SKY, key), modifiers));
                }
            }

            if (random.nextFloat() < DimletConfiguration.randomSpecialSkyChance) {
                List<DimletKey> bodyKeys = new ArrayList<DimletKey>();
                for (DimletKey key : DimletObjectMapping.idToSkyDescriptor.keySet()) {
                    if (DimletObjectMapping.celestialBodies.contains(key)) {
                        bodyKeys.add(key);
                    }
                }

                for (int i = 0 ; i < random.nextInt(3) ; i++) {
                    DimletKey key = bodyKeys.get(random.nextInt(bodyKeys.size()));
                    List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                    dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_SKY, key), modifiers));
                }
            }
        }

        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            DimletKey key = dimletWithModifiers.getKey().getKey();
            builder.combine(DimletObjectMapping.idToSkyDescriptor.get(key));
        }
        skyDescriptor = builder.build();
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

    private List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> extractType(DimletType type, List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets) {
        List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> result = new ArrayList<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>>();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            if (dimlet.getLeft().getType() == type) {
                result.add(dimlet);
            }
        }
        return result;
    }

    private void calculateTerrainType(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_TERRAIN, dimlets);
        List<DimensionDescriptor.DimletDescriptor> modifiers;
        terrainType = TerrainType.TERRAIN_VOID;
        if (dimlets.isEmpty()) {
            // Pick a random terrain type with a seed that is generated from all the
            // dimlets so we always get the same random value for these dimlets.
            List<DimletKey> idList = new ArrayList<DimletKey>(DimletObjectMapping.idToTerrainType.keySet());
            DimletKey key = idList.get(random.nextInt(idList.size()));
            actualRfCost += calculateCostFactor(key);
            terrainType = DimletObjectMapping.idToTerrainType.get(key);
            modifiers = Collections.emptyList();
        } else {
            int index = random.nextInt(dimlets.size());
            DimletKey key = dimlets.get(index).getLeft().getKey();
            terrainType = DimletObjectMapping.idToTerrainType.get(key);
            modifiers = dimlets.get(index).getRight();
        }

        List<BlockMeta> blocks = new ArrayList<BlockMeta>();
        List<Block> fluids = new ArrayList<Block>();
        getMaterialAndFluidModifiers(modifiers, blocks, fluids);

        if (!blocks.isEmpty()) {
            baseBlockForTerrain = blocks.get(random.nextInt(blocks.size()));
            if (baseBlockForTerrain == null) {
                baseBlockForTerrain = new BlockMeta(Blocks.stone, 0);     // This is the default in case None was specified.
            }
        } else {
            // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
            // Note that in this particular case we disallow randomly selecting 'expensive' blocks like glass.
            if (random.nextFloat() < DimletConfiguration.randomBaseBlockChance) {
                DimletKey key = DimletRandomizer.getRandomMaterialBlock(random, false);
                actualRfCost += calculateCostFactor(key);
                baseBlockForTerrain = DimletObjectMapping.idToBlock.get(key);
            } else {
                baseBlockForTerrain = new BlockMeta(Blocks.stone, 0);
            }
        }

        if (!fluids.isEmpty()) {
            fluidForTerrain = fluids.get(random.nextInt(fluids.size()));
            if (fluidForTerrain == null) {
                fluidForTerrain = Blocks.water;         // This is the default.
            }
        } else {
            if (random.nextFloat() < DimletConfiguration.randomOceanLiquidChance) {
                DimletKey key = DimletRandomizer.getRandomFluidBlock(random);
                actualRfCost += calculateCostFactor(key);
                fluidForTerrain = DimletObjectMapping.idToFluid.get(key);
            } else {
                fluidForTerrain = Blocks.water;
            }
        }
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

    private void getMaterialAndFluidModifiers(List<DimensionDescriptor.DimletDescriptor> modifiers, List<BlockMeta> blocks, List<Block> fluids) {
        if (modifiers != null) {
            for (DimensionDescriptor.DimletDescriptor modifier : modifiers) {
                DimletKey key = modifier.getKey();
                if (modifier.getType() == DimletType.DIMLET_MATERIAL) {
                    BlockMeta block = DimletObjectMapping.idToBlock.get(key);
                    blocks.add(block);
                } else if (modifier.getType() == DimletType.DIMLET_LIQUID) {
                    Block fluid = DimletObjectMapping.idToFluid.get(key);
                    fluids.add(fluid);
                }
            }
        }
    }

    private void calculateFeatureType(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_FEATURE, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomFeatureChance) {
                DimletKey key = DimletRandomizer.getRandomFeature(random, false);
                FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
                if (!featureTypes.contains(featureType) && featureType.isTerrainSupported(terrainType)) {
                    actualRfCost += calculateCostFactor(key);
                    featureTypes.add(featureType);
                    List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                    // @todo randomize those?
                    dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_FEATURE, key), modifiers));
                }
            }
        }

        Map<FeatureType,List<DimensionDescriptor.DimletDescriptor>> modifiersForFeature = new HashMap<FeatureType, List<DimensionDescriptor.DimletDescriptor>>();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            DimletKey key = dimlet.getLeft().getKey();
            FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
            featureTypes.add(featureType);
            modifiersForFeature.put(featureType, dimlet.getRight());
        }

        if (featureTypes.contains(FeatureType.FEATURE_LAKES)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_LAKES), blocks, fluids);

            // If no fluids are specified we will usually have default fluid generation (water+lava). Otherwise some random selection.
            if (fluids.isEmpty()) {
                while (random.nextFloat() < DimletConfiguration.randomLakeFluidChance) {
                    DimletKey key = DimletRandomizer.getRandomFluidBlock(random);
                    actualRfCost += calculateCostFactor(key);
                    fluids.add(DimletObjectMapping.idToFluid.get(key));
                }
            } else if (fluids.size() == 1 && fluids.get(0) == null) {
                fluids.clear();
            }
            fluidsForLakes = fluids.toArray(new Block[fluids.size()]);
        } else {
            fluidsForLakes = new Block[0];
        }

        if (featureTypes.contains(FeatureType.FEATURE_LAKES)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_LAKES), blocks, fluids);

            // If no fluids are specified we will usually have default fluid generation (water+lava). Otherwise some random selection.
            if (fluids.isEmpty()) {
                while (random.nextFloat() < DimletConfiguration.randomLakeFluidChance) {
                    DimletKey key = DimletRandomizer.getRandomFluidBlock(random);
                    actualRfCost += calculateCostFactor(key);
                    fluids.add(DimletObjectMapping.idToFluid.get(key));
                }
            } else if (fluids.size() == 1 && fluids.get(0) == null) {
                fluids.clear();
            }
            fluidsForLakes = fluids.toArray(new Block[fluids.size()]);
        } else {
            fluidsForLakes = new Block[0];
        }

        tendrilBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_TENDRILS);
        sphereBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_ORBS);
        liquidSphereBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_LIQUIDORBS);
        liquidSphereFluid = getFeatureLiquid(random, modifiersForFeature, FeatureType.FEATURE_LIQUIDORBS);
        canyonBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_CANYONS);
    }

    private BlockMeta getFeatureBlock(Random random, Map<FeatureType, List<DimensionDescriptor.DimletDescriptor>> modifiersForFeature, FeatureType featureType) {
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

    private Block getFeatureLiquid(Random random, Map<FeatureType, List<DimensionDescriptor.DimletDescriptor>> modifiersForFeature, FeatureType featureType) {
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
                    DimletKey key = DimletRandomizer.getRandomFluidBlock(random);
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



    private void calculateStructureType(List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_STRUCTURE, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomStructureChance) {
                DimletKey key = DimletRandomizer.getRandomStructure(random, false);
                StructureType structureType = DimletObjectMapping.idToStructureType.get(key);
                if (!structureTypes.contains(structureType)) {
                    actualRfCost += calculateCostFactor(key);
                    structureTypes.add(structureType);
                }
            }
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifier : dimlets) {
                DimletKey key = dimletWithModifier.getLeft().getKey();
                structureTypes.add(DimletObjectMapping.idToStructureType.get(key));
            }
        }
    }

    private void calculateBiomes(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        Set<DimletKey> biomeKeys = new HashSet<DimletKey>();
        List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> biomeDimlets = extractType(DimletType.DIMLET_BIOME, dimlets);
        List<Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>>> controllerDimlets = extractType(DimletType.DIMLET_CONTROLLER, dimlets);

        // First determine the controller to use.
        if (controllerDimlets.isEmpty()) {
            if (random.nextFloat() < DimletConfiguration.randomControllerChance) {
                List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToControllerType.keySet());
                DimletKey key = keys.get(random.nextInt(keys.size()));
                controllerType = DimletObjectMapping.idToControllerType.get(key);
            } else {
                if (biomeDimlets.isEmpty()) {
                    controllerType = ControllerType.CONTROLLER_DEFAULT;
                } else if (biomeDimlets.size() > 1) {
                    controllerType = ControllerType.CONTROLLER_FILTERED;
                } else {
                    controllerType = ControllerType.CONTROLLER_SINGLE;
                }
            }
        } else {
            DimletKey key = controllerDimlets.get(random.nextInt(controllerDimlets.size())).getLeft().getKey();
            controllerType = DimletObjectMapping.idToControllerType.get(key);
        }

        // Now see if we have to add or randomize biomes.
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : biomeDimlets) {
            DimletKey key = dimletWithModifiers.getKey().getKey();
            biomeKeys.add(key);
        }

        int neededBiomes = controllerType.getNeededBiomes();
        if (neededBiomes == -1) {
            // Can work with any number of biomes.
            if (biomeKeys.size() >= 2) {
                neededBiomes = biomeKeys.size();     // We already have enough biomes
            } else {
                neededBiomes = random.nextInt(10) + 3;
            }
        }

        while (biomeKeys.size() < neededBiomes) {
            DimletKey key;
            List<DimletKey> keys = new ArrayList<DimletKey>(DimletObjectMapping.idToBiome.keySet());
            key = keys.get(random.nextInt(keys.size()));
            while (biomeKeys.contains(key)) {
                key = keys.get(random.nextInt(keys.size()));
            }
            biomeKeys.add(key);
        }

        biomes.clear();
        for (DimletKey key : biomeKeys) {
            biomes.add(DimletObjectMapping.idToBiome.get(key));
        }
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

    public String getDigitString() {
        return digitString;
    }

    public BlockMeta getBaseBlockForTerrain() {
        return baseBlockForTerrain;
    }

    public BlockMeta getTendrilBlock() {
        return tendrilBlock;
    }

    public BlockMeta getCanyonBlock() {
        return canyonBlock;
    }

    public BlockMeta getSphereBlock() {
        return sphereBlock;
    }

    public BlockMeta getLiquidSphereBlock() {
        return liquidSphereBlock;
    }

    public Block getLiquidSphereFluid() {
        return liquidSphereFluid;
    }

    public BlockMeta[] getExtraOregen() {
        return extraOregen;
    }

    public Block getFluidForTerrain() {
        return fluidForTerrain;
    }

    public Block[] getFluidsForLakes() {
        return fluidsForLakes;
    }

    public SkyDescriptor getSkyDescriptor() {
        return skyDescriptor;
    }

    public WeatherDescriptor getWeatherDescriptor() {
        return weatherDescriptor;
    }

    public List<CelestialBodyDescriptor> getCelestialBodyDescriptors() {
        return celestialBodyDescriptors;
    }

    public List<MobDescriptor> getExtraMobs() {
        return extraMobs;
    }

    public boolean isPeaceful() {
        return peaceful;
    }

    public boolean isShelter() {
        return shelter;
    }

    public Float getCelestialAngle() {
        return celestialAngle;
    }

    public Float getTimeSpeed() {
        return timeSpeed;
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
