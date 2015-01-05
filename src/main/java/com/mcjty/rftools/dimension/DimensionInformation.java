package com.mcjty.rftools.dimension;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.world.types.*;
import com.mcjty.rftools.items.dimlets.*;
import com.mcjty.rftools.network.ByteBufTools;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DimensionInformation {
    private final DimensionDescriptor descriptor;
    private final String name;

    private Coordinate spawnPoint = null;

    private int probeCounter = 0;

    private TerrainType terrainType = TerrainType.TERRAIN_VOID;
    private Block baseBlockForTerrain = null;
    private Block fluidForTerrain = null;
    private Block tendrilBlock = null;
    private Block canyonBlock = null;
    private Block sphereBlock = null;

    private List<MobDescriptor> extraMobs = new ArrayList<MobDescriptor>();
    private boolean peaceful = false;

    private Set<FeatureType> featureTypes = new HashSet<FeatureType>();
    private Block[] extraOregen = new Block[] {};
    private Block[] fluidsForLakes = new Block[] {};

    private Set<StructureType> structureTypes = new HashSet<StructureType>();
    private Set<EffectType> effectTypes = new HashSet<EffectType>();
    private List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
    private String digitString = "";

    private Float celestialAngle = null;
    private Float timeSpeed = null;

    private SkyDescriptor skyDescriptor;

    // The actual RF cost after taking into account the features we got in our world.
    private int actualRfCost;

    public DimensionInformation(String name, DimensionDescriptor descriptor, long seed) {
        this.name = name;
        this.descriptor = descriptor;

        setupFromDescriptor(seed);
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
        calculateSpecial(dimlets, random);
        calculateTime(dimlets, random);
        calculateEffects(dimlets, random);

        actualRfCost += descriptor.getRfMaintainCost();
    }

    public void injectDimlet(int id) {
        DimletType type = KnownDimletConfiguration.idToDimlet.get(id).getKey().getType();
        switch (type) {
            case DIMLET_BIOME:
            case DIMLET_FOLIAGE:
            case DIMLET_LIQUID:
            case DIMLET_MATERIAL:
            case DIMLET_STRUCTURE:
            case DIMLET_TERRAIN:
            case DIMLET_DIGIT:
            case DIMLET_FEATURE:
                // Not supported
                return;
            case DIMLET_MOBS:
                injectMobDimlet(id);
                break;
            case DIMLET_SKY:
                injectSkyDimlet(id);
                break;
            case DIMLET_TIME:
                injectTimeDimlet(id);
                break;
            case DIMLET_SPECIAL:
                injectSpecialDimlet(id);
                break;
            case DIMLET_EFFECT:
                injectEffectDimlet(id);
                break;
        }
    }

    private void injectMobDimlet(int id) {
        addToCost(id);
        extraMobs.add(DimletMapping.idtoMob.get(id));
    }

    private void injectSkyDimlet(int id) {
        addToCost(id);
        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
//        builder.combine(skyDescriptor);   @todo: should we allow combining?
        builder.combine(DimletMapping.idToSkyDescriptor.get(id));
        skyDescriptor = builder.build();
    }

    private void injectTimeDimlet(int id) {
        addToCost(id);
        celestialAngle = DimletMapping.idToCelestialAngle.get(id);
        timeSpeed = DimletMapping.idToSpeed.get(id);
    }

    private void injectEffectDimlet(int id) {
        addToCost(id);
        effectTypes.add(DimletMapping.idToEffectType.get(id));
    }

    private void injectSpecialDimlet(int id) {
        addToCost(id);
        if (DimletMapping.idToSpecialType.get(id) == SpecialType.SPECIAL_PEACEFUL) {
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
            readFromNBT(tagCompound);
        } else {
            // This is an older version. Here we have to calculate the random information again.
            setupFromDescriptor(1);
        }
    }

    private void readFromNBT(NBTTagCompound tagCompound) {
        terrainType = TerrainType.values()[tagCompound.getInteger("terrain")];
        featureTypes = toEnumSet(tagCompound.getIntArray("features"), FeatureType.values());
        structureTypes = toEnumSet(tagCompound.getIntArray("structures"), StructureType.values());
        effectTypes = toEnumSet(tagCompound.getIntArray("effects"), EffectType.values());

        biomes.clear();
        for (int a : tagCompound.getIntArray("biomes")) {
            biomes.add(BiomeGenBase.getBiome(a));
        }

        digitString = tagCompound.getString("digits");

        baseBlockForTerrain = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("baseBlock"));
        tendrilBlock = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("tendrilBlock"));
        sphereBlock = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("sphereBlock"));
        canyonBlock = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("canyonBlock"));
        fluidForTerrain = (Block) Block.blockRegistry.getObjectById(tagCompound.getInteger("fluidBlock"));

        List<Block> ores = new ArrayList<Block>();
        for (int a : tagCompound.getIntArray("extraOregen")) {
            ores.add((Block) Block.blockRegistry.getObjectById(a));
        }
        extraOregen = ores.toArray(new Block[ores.size()]);

        ores.clear();
        for (int a : tagCompound.getIntArray("lakeFluids")) {
            ores.add((Block) Block.blockRegistry.getObjectById(a));
        }
        fluidsForLakes = ores.toArray(new Block[ores.size()]);

        peaceful = tagCompound.getBoolean("peaceful");
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
        tagCompound.setString("digits", digitString);

        tagCompound.setInteger("baseBlock", Block.blockRegistry.getIDForObject(baseBlockForTerrain));
        tagCompound.setInteger("tendrilBlock", Block.blockRegistry.getIDForObject(tendrilBlock));
        tagCompound.setInteger("sphereBlock", Block.blockRegistry.getIDForObject(sphereBlock));
        tagCompound.setInteger("canyonBlock", Block.blockRegistry.getIDForObject(canyonBlock));
        tagCompound.setInteger("fluidBlock", Block.blockRegistry.getIDForObject(fluidForTerrain));

        c = new ArrayList<Integer>(extraOregen.length);
        for (Block t : extraOregen) {
            c.add(Block.blockRegistry.getIDForObject(t));
        }
        tagCompound.setIntArray("extraOregen", ArrayUtils.toPrimitive(c.toArray(new Integer[c.size()])));

        c = new ArrayList<Integer>(fluidsForLakes.length);
        for (Block t : fluidsForLakes) {
            c.add(Block.blockRegistry.getIDForObject(t));
        }
        tagCompound.setIntArray("lakeFluids", ArrayUtils.toPrimitive(c.toArray(new Integer[c.size()])));

        tagCompound.setBoolean("peaceful", peaceful);
        if (celestialAngle != null) {
            tagCompound.setFloat("celestialAngle", celestialAngle);
        }
        if (timeSpeed != null) {
            tagCompound.setFloat("timeSpeed", timeSpeed);
        }
        tagCompound.setInteger("probes", probeCounter);
        tagCompound.setInteger("actualCost", actualRfCost);

        skyDescriptor.writeToNBT(tagCompound);

        NBTTagList list = new NBTTagList();
        for (MobDescriptor mob : extraMobs) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setString("class", mob.getEntityClass().getName());
            tc.setInteger("chance", mob.getSpawnChance());
            tc.setInteger("minGroup", mob.getMinGroup());
            tc.setInteger("maxGroup", mob.getMaxGroup());
            tc.setInteger("maxLoaded", mob.getMaxLoaded());
            list.appendTag(tc);
        }

        tagCompound.setTag("mobs", list);
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
        RFTools.message(player, EnumChatFormatting.YELLOW + message);
    }

    public void dump(EntityPlayer player) {
        String digits = getDigitString();
        if (!digits.isEmpty()) {
            logDebug(player, "    Digits: " + digits);
        }
        TerrainType terrainType = getTerrainType();
        logDebug(player, "    Terrain: " + terrainType.toString());
        logDebug(player, "        Base block: " + new ItemStack(baseBlockForTerrain).getDisplayName());
        if (featureTypes.contains(FeatureType.FEATURE_TENDRILS)) {
            logDebug(player, "        Tendril block: " + new ItemStack(tendrilBlock).getDisplayName());
        }
        if (featureTypes.contains(FeatureType.FEATURE_SPHERES)) {
            logDebug(player, "        Sphere block: " + new ItemStack(sphereBlock).getDisplayName());
        }
        if (featureTypes.contains(FeatureType.FEATURE_CANYONS)) {
            logDebug(player, "        Canyon block: " + new ItemStack(canyonBlock).getDisplayName());
        }
        logDebug(player, "        Base fluid: " + new ItemStack(fluidForTerrain).getDisplayName());
        for (BiomeGenBase biome : getBiomes()) {
            logDebug(player, "    Biome: " + biome.biomeName);
        }
        for (FeatureType featureType : getFeatureTypes()) {
            logDebug(player, "    Feature: " + featureType.toString());
        }
        for (Block block : extraOregen) {
            logDebug(player, "        Extra ore: " + new ItemStack(block).getDisplayName());
        }
        for (Block block : fluidsForLakes) {
            logDebug(player, "        Lake fluid: " + new ItemStack(block).getDisplayName());
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

        for (MobDescriptor mob : extraMobs) {
            logDebug(player, "    Mob: " + mob.getEntityClass().getName());
        }
        if (peaceful) {
            logDebug(player, "    Peaceful mode");
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

        ByteBufTools.writeString(buf, digitString);

        buf.writeInt(Block.blockRegistry.getIDForObject(baseBlockForTerrain));
        buf.writeInt(Block.blockRegistry.getIDForObject(tendrilBlock));
        buf.writeInt(Block.blockRegistry.getIDForObject(sphereBlock));
        buf.writeInt(Block.blockRegistry.getIDForObject(canyonBlock));
        buf.writeInt(Block.blockRegistry.getIDForObject(fluidForTerrain));

        buf.writeInt(extraOregen.length);
        for (Block block : extraOregen) {
            buf.writeInt(Block.blockRegistry.getIDForObject(block));
        }
        buf.writeInt(fluidsForLakes.length);
        for (Block block : fluidsForLakes) {
            buf.writeInt(Block.blockRegistry.getIDForObject(block));
        }

        buf.writeBoolean(peaceful);
        ByteBufTools.writeFloat(buf, celestialAngle);
        ByteBufTools.writeFloat(buf, timeSpeed);

        buf.writeInt(probeCounter);
        buf.writeInt(actualRfCost);

        skyDescriptor.toBytes(buf);

        buf.writeInt(extraMobs.size());
        for (MobDescriptor mob : extraMobs) {
            ByteBufTools.writeString(buf, mob.getEntityClass().getName());
            buf.writeInt(mob.getSpawnChance());
            buf.writeInt(mob.getMinGroup());
            buf.writeInt(mob.getMaxGroup());
            buf.writeInt(mob.getMaxLoaded());
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
        digitString = ByteBufTools.readString(buf);

        baseBlockForTerrain = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        tendrilBlock = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        sphereBlock = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        canyonBlock = (Block) Block.blockRegistry.getObjectById(buf.readInt());
        fluidForTerrain = (Block) Block.blockRegistry.getObjectById(buf.readInt());

        size = buf.readInt();
        List<Block> blocks = new ArrayList<Block>();
        for (int i = 0 ; i < size ; i++) {
            blocks.add((Block) Block.blockRegistry.getObjectById(buf.readInt()));
        }
        extraOregen = blocks.toArray(new Block[blocks.size()]);

        size = buf.readInt();
        blocks = new ArrayList<Block>();
        for (int i = 0 ; i < size ; i++) {
            blocks.add((Block) Block.blockRegistry.getObjectById(buf.readInt()));
        }
        fluidsForLakes = blocks.toArray(new Block[blocks.size()]);

        peaceful = buf.readBoolean();

        celestialAngle = ByteBufTools.readFloat(buf);
        timeSpeed = ByteBufTools.readFloat(buf);

        probeCounter = buf.readInt();
        actualRfCost = buf.readInt();

        skyDescriptor = new SkyDescriptor.Builder().fromBytes(buf).build();

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
                throw new RuntimeException(e);
            }
        }
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
                List<Integer> keys = new ArrayList<Integer>(DimletMapping.idToCelestialAngle.keySet());
                int id = keys.get(random.nextInt(keys.size()));
                celestialAngle = DimletMapping.idToCelestialAngle.get(id);
                timeSpeed = DimletMapping.idToSpeed.get(id);
            }
        } else {
            int id = dimlets.get(random.nextInt(dimlets.size())).getKey().getId();
            celestialAngle = DimletMapping.idToCelestialAngle.get(id);
            timeSpeed = DimletMapping.idToSpeed.get(id);
        }
    }

    private void calculateSpecial(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_SPECIAL, dimlets);
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            if (DimletMapping.idToSpecialType.get(dimlet.getLeft().getId()) == SpecialType.SPECIAL_PEACEFUL) {
                peaceful = true;
            }
        }
    }

    private void calculateMobs(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_MOBS, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomExtraMobsChance) {
                int id = DimletRandomizer.getRandomMob(random);
                actualRfCost += calculateCostFactor(id);
                extraMobs.add(DimletMapping.idtoMob.get(id));
            }
        } else if (dimlets.size() == 1 && DimletMapping.idtoMob.get(dimlets.get(0).getLeft().getId()) == null) {
            // Just default.
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
                extraMobs.add(DimletMapping.idtoMob.get(dimletWithModifiers.getLeft().getId()));
            }
        }
    }

    private void calculateDigitString(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets) {
        dimlets = extractType(DimletType.DIMLET_DIGIT, dimlets);
        digitString = "";
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getKey().getId();
            digitString += DimletMapping.idToDigit.get(id);
        }
    }

    private void calculateEffects(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_EFFECT, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomEffectChance) {
                int id = DimletRandomizer.getRandomEffect(random, false);
                EffectType effectType = DimletMapping.idToEffectType.get(id);
                if (!effectTypes.contains(effectType)) {
                    actualRfCost += calculateCostFactor(id);
                    effectTypes.add(effectType);
                }
            }
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifier : dimlets) {
                EffectType effectType = DimletMapping.idToEffectType.get(dimletWithModifier.getLeft().getId());
                if (effectType != EffectType.EFFECT_NONE) {
                    effectTypes.add(effectType);
                }
            }
        }
    }

    private void calculateSky(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_SKY, dimlets);
        if (random.nextFloat() < DimletConfiguration.randomSpecialSkyChance && dimlets.isEmpty()) {
            // If nothing was specified then there is random chance we get random sky stuff.
            List<Integer> skyIds = new ArrayList<Integer>(DimletMapping.idToSkyDescriptor.keySet());
            for (int i = 0 ; i < 1+random.nextInt(3) ; i++) {
                int id = skyIds.get(random.nextInt(skyIds.size()));
                List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_SKY,id), modifiers));
            }
        }

        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getKey().getId();
            builder.combine(DimletMapping.idToSkyDescriptor.get(id));
        }
        skyDescriptor = builder.build();
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

    private void calculateTerrainType(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_TERRAIN, dimlets);
        List<DimensionDescriptor.DimletDescriptor> modifiers;
        terrainType = TerrainType.TERRAIN_VOID;
        if (dimlets.isEmpty()) {
            // Pick a random terrain type with a seed that is generated from all the
            // dimlets so we always get the same random value for these dimlets.
            ArrayList<Integer> idList = new ArrayList<Integer>(DimletMapping.idToTerrainType.keySet());
            int id = idList.get(random.nextInt(idList.size()));
            actualRfCost += calculateCostFactor(id);
            terrainType = DimletMapping.idToTerrainType.get(id);
            modifiers = Collections.emptyList();
        } else {
            int index = random.nextInt(dimlets.size());
            terrainType = DimletMapping.idToTerrainType.get(dimlets.get(index).getLeft().getId());
            modifiers = dimlets.get(index).getRight();
        }

        List<Block> blocks = new ArrayList<Block>();
        List<Block> fluids = new ArrayList<Block>();
        getMaterialAndFluidModifiers(modifiers, blocks, fluids);

        if (!blocks.isEmpty()) {
            baseBlockForTerrain = blocks.get(random.nextInt(blocks.size()));
            if (baseBlockForTerrain == null) {
                baseBlockForTerrain = Blocks.stone;     // This is the default in case None was specified.
            }
        } else {
            // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
            // Note that in this particular case we disallow randomly selecting 'expensive' blocks like glass.
            if (random.nextFloat() < DimletConfiguration.randomBaseBlockChance) {
                int id = DimletRandomizer.getRandomMaterialBlock(random, false);
                actualRfCost += calculateCostFactor(id);
                baseBlockForTerrain = DimletMapping.idToBlock.get(id);
            } else {
                baseBlockForTerrain = Blocks.stone;
            }
        }

        if (!fluids.isEmpty()) {
            fluidForTerrain = fluids.get(random.nextInt(fluids.size()));
            if (fluidForTerrain == null) {
                fluidForTerrain = Blocks.water;         // This is the default.
            }
        } else {
            if (random.nextFloat() < DimletConfiguration.randomOceanLiquidChance) {
                int id = DimletRandomizer.getRandomFluidBlock(random);
                actualRfCost += calculateCostFactor(id);
                fluidForTerrain = DimletMapping.idToFluid.get(id);
            } else {
                fluidForTerrain = Blocks.water;
            }
        }
    }

    private int calculateCostFactor(int id) {
        DimletEntry dimletEntry = KnownDimletConfiguration.idToDimlet.get(id);
        if (dimletEntry == null) {
            RFTools.logError("Something went wrong for id: "+id);
            return 0;
        }
        return (int) (dimletEntry.getRfMaintainCost() * DimletConfiguration.afterCreationCostFactor);
    }

    private void addToCost(int id) {
        DimletEntry dimletEntry = KnownDimletConfiguration.idToDimlet.get(id);
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

    private void getMaterialAndFluidModifiers(List<DimensionDescriptor.DimletDescriptor> modifiers, List<Block> blocks, List<Block> fluids) {
        if (modifiers != null) {
            for (DimensionDescriptor.DimletDescriptor modifier : modifiers) {
                if (modifier.getType() == DimletType.DIMLET_MATERIAL) {
                    Block block = DimletMapping.idToBlock.get(modifier.getId());
                    blocks.add(block);
                } else if (modifier.getType() == DimletType.DIMLET_LIQUID) {
                    Block fluid = DimletMapping.idToFluid.get(modifier.getId());
                    fluids.add(fluid);
                }
            }
        }
    }

    private void calculateFeatureType(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_FEATURE, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomFeatureChance) {
                int id = DimletRandomizer.getRandomFeature(random, false);
                FeatureType featureType = DimletMapping.idToFeatureType.get(id);
                if (!featureTypes.contains(featureType)) {
                    actualRfCost += calculateCostFactor(id);
                    featureTypes.add(featureType);
                    List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                    // @todo randomize those?
                    dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_FEATURE, id), modifiers));
                }
            }
        }

        Map<FeatureType,List<DimensionDescriptor.DimletDescriptor>> modifiersForFeature = new HashMap<FeatureType, List<DimensionDescriptor.DimletDescriptor>>();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            FeatureType featureType = DimletMapping.idToFeatureType.get(dimlet.getLeft().getId());
            featureTypes.add(featureType);
            modifiersForFeature.put(featureType, dimlet.getRight());
        }

        if (featureTypes.contains(FeatureType.FEATURE_LAKES)) {
            List<Block> blocks = new ArrayList<Block>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_LAKES), blocks, fluids);

            // If no fluids are specified we will usually have default fluid generation (water+lava). Otherwise some random selection.
            if (fluids.isEmpty()) {
                while (random.nextFloat() < DimletConfiguration.randomLakeFluidChance) {
                    int id = DimletRandomizer.getRandomFluidBlock(random);
                    actualRfCost += calculateCostFactor(id);
                    fluids.add(DimletMapping.idToFluid.get(id));
                }
            } else if (fluids.size() == 1 && fluids.get(0) == null) {
                fluids.clear();
            }
            fluidsForLakes = fluids.toArray(new Block[fluids.size()]);
        } else {
            fluidsForLakes = new Block[0];
        }

        if (featureTypes.contains(FeatureType.FEATURE_OREGEN)) {
            List<Block> blocks = new ArrayList<Block>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_OREGEN), blocks, fluids);

            // If no blocks for oregen are specified we have a small chance that some extra oregen is generated anyway.
            if (blocks.isEmpty()) {
                while (random.nextFloat() < DimletConfiguration.randomOregenMaterialChance) {
                    int id = DimletRandomizer.getRandomMaterialBlock(random, true);
                    actualRfCost += calculateCostFactor(id);
                    blocks.add(DimletMapping.idToBlock.get(id));
                }
            } else if (blocks.size() == 1 && blocks.get(0) == null) {
                blocks.clear();
            }
            extraOregen = blocks.toArray(new Block[blocks.size()]);
        } else {
            extraOregen = new Block[0];
        }

        tendrilBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_TENDRILS);
        sphereBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_SPHERES);
        canyonBlock = getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_CANYONS);
    }

    private Block getFeatureBlock(Random random, Map<FeatureType, List<DimensionDescriptor.DimletDescriptor>> modifiersForFeature, FeatureType featureType) {
        Block block;
        if (featureTypes.contains(featureType)) {
            List<Block> blocks = new ArrayList<Block>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(featureType), blocks, fluids);

            if (!blocks.isEmpty()) {
                block = blocks.get(random.nextInt(blocks.size()));
                if (block == null) {
                    block = Blocks.stone;     // This is the default in case None was specified.
                }
            } else {
                // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
                if (random.nextFloat() < DimletConfiguration.randomFeatureMaterialChance) {
                    int id = DimletRandomizer.getRandomMaterialBlock(random, true);
                    actualRfCost += calculateCostFactor(id);
                    block = DimletMapping.idToBlock.get(id);
                } else {
                    block = Blocks.stone;
                }
            }
        } else {
            block = Blocks.stone;
        }
        return block;
    }


    private void calculateStructureType(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_STRUCTURE, dimlets);
        if (dimlets.isEmpty()) {
            for (Integer id : DimletMapping.idToStructureType.keySet()) {
                if (random.nextFloat() < DimletConfiguration.randomStructureChance) {
                    actualRfCost += calculateCostFactor(id);
                    structureTypes.add(DimletMapping.idToStructureType.get(id));
                }
            }
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifier : dimlets) {
                structureTypes.add(DimletMapping.idToStructureType.get(dimletWithModifier.getLeft().getId()));
            }
        }
    }

    private void calculateBiomes(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_BIOME, dimlets);
        // @@@ TODO: distinguish between random overworld biome, random nether biome, random biome and specific biomes
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getKey().getId();
            biomes.add(DimletMapping.idToBiome.get(id));
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

    public String getDigitString() {
        return digitString;
    }

    public Block getBaseBlockForTerrain() {
        return baseBlockForTerrain;
    }

    public Block getTendrilBlock() {
        return tendrilBlock;
    }

    public Block getCanyonBlock() {
        return canyonBlock;
    }

    public Block getSphereBlock() {
        return sphereBlock;
    }

    public Block[] getExtraOregen() {
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

    public List<MobDescriptor> getExtraMobs() {
        return extraMobs;
    }

    public boolean isPeaceful() {
        return peaceful;
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
}
