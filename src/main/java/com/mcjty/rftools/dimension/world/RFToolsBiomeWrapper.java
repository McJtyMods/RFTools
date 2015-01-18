package com.mcjty.rftools.dimension.world;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.List;
import java.util.Random;

public class RFToolsBiomeWrapper extends BiomeGenBase {
    private final BiomeGenBase parent;

    public RFToolsBiomeWrapper(int id, BiomeGenBase parent) {
        this(id, parent, false);
    }

    public RFToolsBiomeWrapper(int id, BiomeGenBase parent, boolean register) {
        super(id, false);
        this.parent = parent;
    }

    @Override
    public BiomeDecorator createBiomeDecorator() {
        return super.createBiomeDecorator();
    }

    @Override
    public BiomeGenBase setTemperatureRainfall(float p_76732_1_, float p_76732_2_) {
        return super.setTemperatureRainfall(p_76732_1_, p_76732_2_);
    }

    @Override
    public BiomeGenBase setDisableRain() {
        return super.setDisableRain();
    }

    @Override
    public WorldGenAbstractTree func_150567_a(Random p_150567_1_) {
        return super.func_150567_a(p_150567_1_);
    }

    @Override
    public WorldGenerator getRandomWorldGenForGrass(Random p_76730_1_) {
        return super.getRandomWorldGenForGrass(p_76730_1_);
    }

    @Override
    public String func_150572_a(Random p_150572_1_, int p_150572_2_, int p_150572_3_, int p_150572_4_) {
        return super.func_150572_a(p_150572_1_, p_150572_2_, p_150572_3_, p_150572_4_);
    }

    @Override
    public BiomeGenBase setEnableSnow() {
        return super.setEnableSnow();
    }

    @Override
    public BiomeGenBase setBiomeName(String p_76735_1_) {
        return super.setBiomeName(p_76735_1_);
    }

    @Override
    public BiomeGenBase func_76733_a(int p_76733_1_) {
        return super.func_76733_a(p_76733_1_);
    }

    @Override
    public BiomeGenBase setColor(int p_76739_1_) {
        return super.setColor(p_76739_1_);
    }

    @Override
    public BiomeGenBase func_150563_c(int p_150563_1_) {
        return super.func_150563_c(p_150563_1_);
    }

    @Override
    public BiomeGenBase func_150557_a(int p_150557_1_, boolean p_150557_2_) {
        return super.func_150557_a(p_150557_1_, p_150557_2_);
    }

    @Override
    public int getSkyColorByTemp(float p_76731_1_) {
        return super.getSkyColorByTemp(p_76731_1_);
    }

    @Override
    public List getSpawnableList(EnumCreatureType p_76747_1_) {
        return super.getSpawnableList(p_76747_1_);
    }

    @Override
    public boolean getEnableSnow() {
        return super.getEnableSnow();
    }

    @Override
    public boolean canSpawnLightningBolt() {
        return super.canSpawnLightningBolt();
    }

    @Override
    public boolean isHighHumidity() {
        return super.isHighHumidity();
    }

    @Override
    public float getSpawningChance() {
        return super.getSpawningChance();
    }

    @Override
    public void decorate(World p_76728_1_, Random p_76728_2_, int p_76728_3_, int p_76728_4_) {
        super.decorate(p_76728_1_, p_76728_2_, p_76728_3_, p_76728_4_);
    }

    @Override
    public int getBiomeGrassColor(int p_150558_1_, int p_150558_2_, int p_150558_3_) {
//        return super.getBiomeGrassColor(p_150558_1_, p_150558_2_, p_150558_3_);
        return 0x000000;//getModdedBiomeGrassColor(ColorizerGrass.getGrassColor(d0, d1));
    }

    @Override
    public int getBiomeFoliageColor(int p_150571_1_, int p_150571_2_, int p_150571_3_) {
        return super.getBiomeFoliageColor(p_150571_1_, p_150571_2_, p_150571_3_);
    }

    @Override
    public boolean func_150559_j() {
        return super.func_150559_j();
    }

    @Override
    public void genTerrainBlocks(World p_150573_1_, Random p_150573_2_, Block[] p_150573_3_, byte[] p_150573_4_, int p_150573_5_, int p_150573_6_, double p_150573_7_) {
        super.genTerrainBlocks(p_150573_1_, p_150573_2_, p_150573_3_, p_150573_4_, p_150573_5_, p_150573_6_, p_150573_7_);
    }

    @Override
    public BiomeGenBase createMutation() {
        return super.createMutation();
    }

    @Override
    public Class getBiomeClass() {
        return super.getBiomeClass();
    }

    @Override
    public boolean isEqualTo(BiomeGenBase p_150569_1_) {
        return super.isEqualTo(p_150569_1_);
    }

    @Override
    public TempCategory getTempCategory() {
        return super.getTempCategory();
    }

    @Override
    public BiomeDecorator getModdedBiomeDecorator(BiomeDecorator original) {
        return super.getModdedBiomeDecorator(original);
    }

    @Override
    public int getWaterColorMultiplier() {
        return super.getWaterColorMultiplier();
    }

    @Override
    public int getModdedBiomeGrassColor(int original) {
        return super.getModdedBiomeGrassColor(original);
    }

    @Override
    public int getModdedBiomeFoliageColor(int original) {
        return super.getModdedBiomeFoliageColor(original);
    }

    @Override
    public void addDefaultFlowers() {
        super.addDefaultFlowers();
    }

    @Override
    public void addFlower(Block block, int metadata, int weight) {
        super.addFlower(block, metadata, weight);
    }

    @Override
    public void plantFlower(World world, Random rand, int x, int y, int z) {
        super.plantFlower(world, rand, x, y, z);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.equals(parent)) {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return parent.hashCode();
    }
}
