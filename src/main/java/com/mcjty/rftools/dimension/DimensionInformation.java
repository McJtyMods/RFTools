package com.mcjty.rftools.dimension;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.SpecialType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.biome.BiomeGenBase;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DimensionInformation {
    private final DimensionDescriptor descriptor;
    private final String name;

    private Coordinate spawnPoint = null;

    private TerrainType terrainType = TerrainType.TERRAIN_VOID;
    private Block baseBlockForTerrain = null;
    private Block fluidForTerrain = null;
    private Block tendrilBlock = null;
    private Block canyonBlock = null;
    private Block sphereBlock = null;

    private List<Class<? extends EntityLiving>> extraMobs = new ArrayList<Class<? extends EntityLiving>>();
    private boolean peaceful = false;

    private Set<FeatureType> featureTypes = new HashSet<FeatureType>();
    private Block[] extraOregen = new Block[] {};
    private Block[] fluidsForLakes = new Block[] {};

    private Set<StructureType> structureTypes = new HashSet<StructureType>();
    private List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
    private String digitString = "";

    private Float celestialAngle = null;
    private Float timeSpeed = null;

    private SkyDescriptor skyDescriptor;

    public DimensionInformation(String name, DimensionDescriptor descriptor) {
        this.name = name;
        this.descriptor = descriptor;

        List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets = descriptor.getDimletsWithModifiers();

        Random random = new Random(descriptor.calculateSeed());

        calculateTerrainType(dimlets, random);

        calculateFeatureType(dimlets, random);

        calculateStructureType(dimlets, random);
        calculateBiomes(dimlets, random);
        calculateDigitString(dimlets);

        calculateSky(dimlets, random);

        calculateMobs(dimlets, random);
        calculateSpecial(dimlets, random);
        calculateTime(dimlets, random);
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
        logDebug(player, "    Sun brightness: " + skyDescriptor.getSunBrightnessFactor());
        logDebug(player, "    Star brightness: " + skyDescriptor.getStarBrightnessFactor());
        for (Class<? extends EntityLiving> entityClass : extraMobs) {
            logDebug(player, "    Mob: " + entityClass.getName());
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
    }

    public void toBytes(ByteBuf buf) {
        if (terrainType == null) {
            buf.writeInt(TerrainType.TERRAIN_VOID.ordinal());
        } else {
            buf.writeInt(terrainType.ordinal());
        }
        buf.writeInt(featureTypes.size());
        for (FeatureType type : featureTypes) {
            buf.writeInt(type.ordinal());
        }
        buf.writeInt(structureTypes.size());
        for (StructureType type : structureTypes) {
            buf.writeInt(type.ordinal());
        }
        buf.writeInt(biomes.size());
        for (BiomeGenBase entry : biomes) {
            buf.writeInt(entry.biomeID);
        }
        buf.writeInt(digitString.length());
        buf.writeBytes(digitString.getBytes());

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
        if (celestialAngle != null) {
            buf.writeBoolean(true);
            buf.writeFloat(celestialAngle);
        } else {
            buf.writeBoolean(false);
        }
        if (timeSpeed != null) {
            buf.writeBoolean(true);
            buf.writeFloat(timeSpeed);
        } else {
            buf.writeBoolean(false);
        }

        skyDescriptor.toBytes(buf);

        // @todo do mobs
    }

    public void fromBytes(ByteBuf buf) {
        terrainType = TerrainType.values()[buf.readInt()];

        int size = buf.readInt();
        featureTypes.clear();
        for (int i = 0 ; i < size ; i++) {
            featureTypes.add(FeatureType.values()[buf.readInt()]);
        }

        structureTypes.clear();
        size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            structureTypes.add(StructureType.values()[buf.readInt()]);
        }

        biomes.clear();
        size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            biomes.add(BiomeGenBase.getBiome(buf.readInt()));
        }
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        digitString = new String(dst);

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

        if (buf.readBoolean()) {
            celestialAngle = buf.readFloat();
        } else {
            celestialAngle = null;
        }
        if (buf.readBoolean()) {
            timeSpeed = buf.readFloat();
        } else {
            timeSpeed = null;
        }

        skyDescriptor = new SkyDescriptor.Builder().fromBytes(buf).build();

        // @todo do mobs
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
            if (random.nextFloat() < 0.5f) {
                celestialAngle = null;      // Default
                timeSpeed = null;
            } else {
                List<Integer> keys = new ArrayList<Integer>(KnownDimletConfiguration.idToCelestialAngle.keySet());
                int id = keys.get(random.nextInt(keys.size()));
                celestialAngle = KnownDimletConfiguration.idToCelestialAngle.get(id);
                timeSpeed = KnownDimletConfiguration.idToSpeed.get(id);
            }
        } else {
            int id = dimlets.get(random.nextInt(dimlets.size())).getKey().getId();
            celestialAngle = KnownDimletConfiguration.idToCelestialAngle.get(id);
            timeSpeed = KnownDimletConfiguration.idToSpeed.get(id);
        }
    }

    private void calculateSpecial(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_SPECIAL, dimlets);
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            if (KnownDimletConfiguration.idToSpecialType.get(dimlet.getLeft().getId()) == SpecialType.SPECIAL_PEACEFUL) {
                peaceful = true;
            }
        }
    }

    private void calculateMobs(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_MOBS, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < .4f) {
                extraMobs.add(KnownDimletConfiguration.getRandomMob(random));
            }
        } else if (dimlets.size() == 1 && KnownDimletConfiguration.idtoMob.get(dimlets.get(0).getLeft().getId()) == null) {
            // Just default.
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
                extraMobs.add(KnownDimletConfiguration.idtoMob.get(dimletWithModifiers.getLeft().getId()));
            }
        }
    }

    private void calculateDigitString(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets) {
        dimlets = extractType(DimletType.DIMLET_DIGIT, dimlets);
        digitString = "";
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getKey().getId();
            digitString += KnownDimletConfiguration.idToDigit.get(id);
        }
    }

    private void calculateSky(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_SKY, dimlets);
        if (random.nextFloat() < 0.5f && dimlets.isEmpty()) {
            // If nothing was specified then there is random chance we get random sky stuff.
            List<Integer> skyIds = new ArrayList<Integer>(KnownDimletConfiguration.idToSkyDescriptor.keySet());
            for (int i = 0 ; i < 1+random.nextInt(3) ; i++) {
                int id = skyIds.get(random.nextInt(skyIds.size()));
                List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_SKY,id), modifiers));
            }
        }

        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getKey().getId();
            builder.combine(KnownDimletConfiguration.idToSkyDescriptor.get(id));
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
            terrainType = TerrainType.values()[random.nextInt(TerrainType.values().length)];
            modifiers = Collections.emptyList();
        } else {
            int index = random.nextInt(dimlets.size());
            terrainType = KnownDimletConfiguration.idToTerrainType.get(dimlets.get(index).getLeft().getId());
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
            if (random.nextFloat() < 0.6f) {
                baseBlockForTerrain = Blocks.stone;
            } else {
                baseBlockForTerrain = KnownDimletConfiguration.getRandomMaterialBlock(random, false);
            }
        }

        if (!fluids.isEmpty()) {
            fluidForTerrain = fluids.get(random.nextInt(fluids.size()));
            if (fluidForTerrain == null) {
                fluidForTerrain = Blocks.water;         // This is the default.
            }
        } else {
            if (random.nextFloat() < 0.6f) {
                fluidForTerrain = Blocks.water;
            } else {
                fluidForTerrain = KnownDimletConfiguration.getRandomFluidBlock(random);
            }
        }
    }

    private void getMaterialAndFluidModifiers(List<DimensionDescriptor.DimletDescriptor> modifiers, List<Block> blocks, List<Block> fluids) {
        if (modifiers != null) {
            for (DimensionDescriptor.DimletDescriptor modifier : modifiers) {
                if (modifier.getType() == DimletType.DIMLET_MATERIAL) {
                    Block block = KnownDimletConfiguration.idToBlock.get(modifier.getId());
                    blocks.add(block);
                } else if (modifier.getType() == DimletType.DIMLET_LIQUID) {
                    Block fluid = KnownDimletConfiguration.idToFluid.get(modifier.getId());
                    fluids.add(fluid);
                }
            }
        }
    }

    private void calculateFeatureType(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_FEATURE, dimlets);
        if (dimlets.isEmpty()) {
            for (Map.Entry<Integer, FeatureType> entry : KnownDimletConfiguration.idToFeatureType.entrySet()) {
                // @Todo make this chance configurable?
                if (random.nextFloat() < .4f) {
                    featureTypes.add(entry.getValue());
                    List<DimensionDescriptor.DimletDescriptor> modifiers = Collections.emptyList();
                    // @todo randomize those?
                    dimlets.add(Pair.of(new DimensionDescriptor.DimletDescriptor(DimletType.DIMLET_FEATURE, entry.getKey()), modifiers));
                }
            }
        }

        Map<FeatureType,List<DimensionDescriptor.DimletDescriptor>> modifiersForFeature = new HashMap<FeatureType, List<DimensionDescriptor.DimletDescriptor>>();
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimlet : dimlets) {
            FeatureType featureType = KnownDimletConfiguration.idToFeatureType.get(dimlet.getLeft().getId());
            featureTypes.add(featureType);
            modifiersForFeature.put(featureType, dimlet.getRight());
        }

        if (featureTypes.contains(FeatureType.FEATURE_LAKES)) {
            List<Block> blocks = new ArrayList<Block>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_LAKES), blocks, fluids);

            // If no fluids are specified we will usually have default fluid generation (water+lava). Otherwise some random selection.
            if (fluids.isEmpty()) {
                while (random.nextFloat() < 0.2f) {
                    fluids.add(KnownDimletConfiguration.getRandomFluidBlock(random));
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
                while (random.nextFloat() < 0.2f) {
                    blocks.add(KnownDimletConfiguration.getRandomMaterialBlock(random, true));
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
        Block block = Blocks.stone;
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
                if (random.nextFloat() < 0.6f) {
                    block = Blocks.stone;
                } else {
                    block = KnownDimletConfiguration.getRandomMaterialBlock(random, true);
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
            for (StructureType type : StructureType.values()) {
                // @Todo make this chance configurable?
                if (random.nextFloat() < .2f) {
                    structureTypes.add(type);
                }
            }
        } else {
            for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifier : dimlets) {
                structureTypes.add(KnownDimletConfiguration.idToStructureType.get(dimletWithModifier.getLeft().getId()));
            }
        }
    }

    private void calculateBiomes(List<Pair<DimensionDescriptor.DimletDescriptor,List<DimensionDescriptor.DimletDescriptor>>> dimlets, Random random) {
        dimlets = extractType(DimletType.DIMLET_BIOME, dimlets);
        // @@@ TODO: distinguish between random overworld biome, random nether biome, random biome and specific biomes
        for (Pair<DimensionDescriptor.DimletDescriptor, List<DimensionDescriptor.DimletDescriptor>> dimletWithModifiers : dimlets) {
            int id = dimletWithModifiers.getKey().getId();
            biomes.add(KnownDimletConfiguration.idToBiome.get(id));
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

    public List<Class<? extends EntityLiving>> getExtraMobs() {
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
}
