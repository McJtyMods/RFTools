package com.mcjty.rftools.dimension;

import com.google.common.collect.ImmutableList;
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
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.fluids.FluidStack;

import java.util.*;

public class DimensionInformation {
    private final DimensionDescriptor descriptor;
    private final String name;

    private Coordinate spawnPoint = null;

    private TerrainType terrainType = TerrainType.TERRAIN_VOID;
    private Block baseBlockForTerrain = null;
    private Block fluidForTerrain = null;

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

        Map<DimletType,List<Integer>> dimlets = descriptor.getDimlets();
        Random random = getRandom(dimlets);

        List<DimensionDescriptor.DimletDescriptor> modifiersForTerrain = descriptor.getModifierDimlets(DimletType.DIMLET_TERRAIN);
        calculateTerrainType(dimlets, modifiersForTerrain, random);

        List<DimensionDescriptor.DimletDescriptor> modifiersForFeature = descriptor.getModifierDimlets(DimletType.DIMLET_FEATURE);
        calculateFeatureType(dimlets, modifiersForFeature, random);

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

        //@todo @@@@@@@@@@@@@@@@@@@@@@@@@@@@#################################
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
    }

    public Coordinate getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(Coordinate spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    private Random getRandom(Map<DimletType, List<Integer>> dimlets) {
        int seed = 1;
        for (DimletType type : DimletType.values()) {
            for (Integer id : dimlets.get(type)) {
                seed = 31 * seed + id;
            }
        }
        return new Random(seed);
    }

    private void calculateDigitString(Map<DimletType,List<Integer>> dimlets) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_DIGIT);
        digitString = "";
        for (Integer id : list) {
            digitString += KnownDimletConfiguration.idToDigit.get(id);
        }
    }

    private void calculateSky(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_SKY);
        if (random.nextFloat() < 0.5f && list.isEmpty()) {
            // If nothing was specified then there is random chance we get random sky stuff.
            list = new ArrayList<Integer>();        // Make a new list to not override the one from the map.
            List<Integer> skyIds = new ArrayList<Integer>(KnownDimletConfiguration.idToSkyDescriptor.keySet());
            for (int i = 0 ; i < 1+random.nextInt(3) ; i++) {
                int id = skyIds.get(random.nextInt(skyIds.size()));
                list.add(id);
            }
        }

        SkyDescriptor.Builder builder = new SkyDescriptor.Builder();
        for (int id : list) {
            builder.combine(KnownDimletConfiguration.idToSkyDescriptor.get(id));
        }
        skyDescriptor = builder.build();
    }

    private void calculateTerrainType(Map<DimletType,List<Integer>> dimlets, List<DimensionDescriptor.DimletDescriptor> modifiers, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_TERRAIN);
        terrainType = TerrainType.TERRAIN_VOID;
        if (list.isEmpty()) {
            // Pick a random terrain type with a seed that is generated from all the
            // dimlets so we always get the same random value for these dimlets.
            terrainType = TerrainType.values()[random.nextInt(TerrainType.values().length)];
        } else {
            terrainType = KnownDimletConfiguration.idToTerrainType.get(list.get(random.nextInt(list.size())));
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
            if (random.nextFloat() < 0.6f) {
                baseBlockForTerrain = Blocks.stone;
            } else {
                baseBlockForTerrain = KnownDimletConfiguration.getRandomMaterialBlock();
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
                fluidForTerrain = KnownDimletConfiguration.getRandomFluidBlock();
            }
        }
    }

    private void getMaterialAndFluidModifiers(List<DimensionDescriptor.DimletDescriptor> modifiers, List<Block> blocks, List<Block> fluids) {
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

    private void calculateFeatureType(Map<DimletType,List<Integer>> dimlets, List<DimensionDescriptor.DimletDescriptor> modifiers, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_FEATURE);
        if (list.isEmpty()) {
            for (FeatureType type : FeatureType.values()) {
                // @Todo make this chance configurable?
                if (random.nextFloat() < .4f) {
                    featureTypes.add(type);
                }
            }
        } else {
            for (Integer id : list) {
                featureTypes.add(KnownDimletConfiguration.idToFeatureType.get(id));
            }
        }

        List<Block> blocks = new ArrayList<Block>();
        List<Block> fluids = new ArrayList<Block>();
        getMaterialAndFluidModifiers(modifiers, blocks, fluids);

        // If no blocks for oregen are specified we have a small chance that some extra oregen is generated anyway.
        if (blocks.isEmpty()) {
            while (random.nextFloat() < 0.2f) {
                blocks.add(KnownDimletConfiguration.getRandomMaterialBlock());
            }
        } else if (blocks.size() == 1 && blocks.get(0) == null) {
            blocks.clear();
        }
        extraOregen = blocks.toArray(new Block[blocks.size()]);

        // If no fluids are specified we will usually have default fluid generation (water+lava). Otherwise some random selection.
        if (fluids.isEmpty()) {
            while (random.nextFloat() < 0.2f) {
                fluids.add(KnownDimletConfiguration.getRandomFluidBlock());
            }
        } else if (fluids.size() == 1 && fluids.get(0) == null) {
            fluids.clear();
        }
        fluidsForLakes = fluids.toArray(new Block[fluids.size()]);
    }


    private void calculateStructureType(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_STRUCTURE);
        if (list.isEmpty()) {
            for (StructureType type : StructureType.values()) {
                // @Todo make this chance configurable?
                if (random.nextFloat() < .2f) {
                    structureTypes.add(type);
                }
            }
        } else {
            for (Integer id : list) {
                structureTypes.add(KnownDimletConfiguration.idToStructureType.get(id));
            }
        }
    }

    private void calculateBiomes(Map<DimletType,List<Integer>> dimlets, Random random) {
        List<Integer> list = dimlets.get(DimletType.DIMLET_BIOME);
        // @@@ TODO: distinguish between random overworld biome, random nether biome, random biome and specific biomes
        for (Integer id : list) {
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
