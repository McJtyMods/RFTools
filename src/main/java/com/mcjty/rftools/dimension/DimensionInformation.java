package com.mcjty.rftools.dimension;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.varia.Coordinate;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
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
    private Block baseFeatureBlock = null;      // Block used for some of the features like tendrils or spheres

    private Set<FeatureType> featureTypes = new HashSet<FeatureType>();
    private Block[] extraOregen = new Block[] {};
    private Block[] fluidsForLakes = new Block[] {};

    private Set<StructureType> structureTypes = new HashSet<StructureType>();
    private List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
    private String digitString = "";

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
        logDebug(player, "        Base feature block: " + new ItemStack(baseFeatureBlock).getDisplayName());
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
        buf.writeInt(Block.blockRegistry.getIDForObject(baseFeatureBlock));
        buf.writeInt(Block.blockRegistry.getIDForObject(fluidForTerrain));

        buf.writeInt(extraOregen.length);
        for (Block block : extraOregen) {
            buf.writeInt(Block.blockRegistry.getIDForObject(block));
        }
        buf.writeInt(fluidsForLakes.length);
        for (Block block : fluidsForLakes) {
            buf.writeInt(Block.blockRegistry.getIDForObject(block));
        }

        skyDescriptor.toBytes(buf);
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
        baseFeatureBlock = (Block) Block.blockRegistry.getObjectById(buf.readInt());
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

        skyDescriptor = new SkyDescriptor.Builder().fromBytes(buf).build();
    }

    public Coordinate getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Coordinate spawnPoint) {
        this.spawnPoint = spawnPoint;
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

        FeatureType featureType = null;
        if (featureTypes.contains(FeatureType.FEATURE_TENDRILS)) {
            featureType = FeatureType.FEATURE_TENDRILS;
        } else if (featureTypes.contains(FeatureType.FEATURE_CANYONS)) {
            featureType = FeatureType.FEATURE_CANYONS;
        } else if (featureTypes.contains(FeatureType.FEATURE_SPHERES)) {
            featureType = FeatureType.FEATURE_SPHERES;
        }
        if (featureType != null) {
            List<Block> blocks = new ArrayList<Block>();
            List<Block> fluids = new ArrayList<Block>();
            getMaterialAndFluidModifiers(modifiersForFeature.get(featureType), blocks, fluids);

            if (!blocks.isEmpty()) {
                baseFeatureBlock = blocks.get(random.nextInt(blocks.size()));
                if (baseFeatureBlock == null) {
                    baseFeatureBlock = Blocks.stone;     // This is the default in case None was specified.
                }
            } else {
                // Nothing was specified. With a relatively big chance we use stone. But there is also a chance that the material will be something else.
                if (random.nextFloat() < 0.6f) {
                    baseFeatureBlock = Blocks.stone;
                } else {
                    baseFeatureBlock = KnownDimletConfiguration.getRandomMaterialBlock(random, true);
                }
            }
        } else {
             baseFeatureBlock = Blocks.stone;
        }

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

    public Block getBaseFeatureBlock() {
        return baseFeatureBlock;
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
}
