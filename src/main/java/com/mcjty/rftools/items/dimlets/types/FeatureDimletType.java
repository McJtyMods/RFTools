package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.FeatureType;
import com.mcjty.rftools.dimension.world.types.TerrainType;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class FeatureDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Feature";
    }

    @Override
    public String getOpcode() {
        return "f";
    }

    @Override
    public String getTextureName() {
        return "featureDimlet";
    }

    @Override
    public boolean isModifier() {
        return false;
    }

    @Override
    public boolean isModifiedBy(DimletType type) {
        return type == DimletType.DIMLET_MATERIAL || type == DimletType.DIMLET_LIQUID;
    }

    @Override
    public boolean isInjectable() {
        return false;
    }

    @Override
    public void inject(DimletKey key, DimensionInformation dimensionInformation) {

    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        TerrainType terrainType = dimensionInformation.getTerrainType();
        Set<FeatureType> featureTypes = dimensionInformation.getFeatureTypes();
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_FEATURE, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomFeatureChance) {
                DimletKey key = DimletRandomizer.getRandomFeature(random, false);
                FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
                if (!featureTypes.contains(featureType) && featureType.isTerrainSupported(terrainType)) {
                    dimensionInformation.updateCostFactor(key);
                    featureTypes.add(featureType);
                    List<DimletKey> modifiers = Collections.emptyList();
                    // @todo randomize those?
                    dimlets.add(Pair.of(key, modifiers));
                }
            }
        }

        Map<FeatureType,List<DimletKey>> modifiersForFeature = new HashMap<FeatureType, List<DimletKey>>();
        for (Pair<DimletKey, List<DimletKey>> dimlet : dimlets) {
            DimletKey key = dimlet.getLeft();
            FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
            featureTypes.add(featureType);
            modifiersForFeature.put(featureType, dimlet.getRight());
        }

        Block[] fluidsForLakes;
        if (featureTypes.contains(FeatureType.FEATURE_LAKES)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            DimensionInformation.getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_LAKES), blocks, fluids);

            // If no fluids are specified we will usually have default fluid generation (water+lava). Otherwise some random selection.
            if (fluids.isEmpty()) {
                while (random.nextFloat() < DimletConfiguration.randomLakeFluidChance) {
                    DimletKey key = DimletRandomizer.getRandomFluidBlock(random, true);
                    dimensionInformation.updateCostFactor(key);
                    fluids.add(DimletObjectMapping.idToFluid.get(key));
                }
            } else if (fluids.size() == 1 && fluids.get(0) == null) {
                fluids.clear();
            }
            fluidsForLakes = fluids.toArray(new Block[fluids.size()]);
        } else {
            fluidsForLakes = new Block[0];
        }
        dimensionInformation.setFluidsForLakes(fluidsForLakes);

        BlockMeta[] extraOregen;
        if (featureTypes.contains(FeatureType.FEATURE_OREGEN)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            DimensionInformation.getMaterialAndFluidModifiers(modifiersForFeature.get(FeatureType.FEATURE_OREGEN), blocks, fluids);

            // If no ores are specified we will generate a few random ores.
            if (blocks.isEmpty()) {
                float chance = 1.1f;
                while (random.nextFloat() < chance) {
                    DimletKey key = DimletRandomizer.getRandomMaterialBlock(random, true);
                    BlockMeta bm = DimletObjectMapping.idToBlock.get(key);
                    if (bm != null) {
                        blocks.add(bm);
                        chance = chance * 0.80f;
                    }
                }
            }

            extraOregen = blocks.toArray(new BlockMeta[blocks.size()]);
        } else {
            extraOregen = new BlockMeta[0];
        }
        dimensionInformation.setExtraOregen(extraOregen);

        dimensionInformation.setTendrilBlock(dimensionInformation.getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_TENDRILS));
        dimensionInformation.setSphereBlock(dimensionInformation.getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_ORBS));
        dimensionInformation.setLiquidSphereBlock(dimensionInformation.getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_LIQUIDORBS));
        dimensionInformation.setLiquidSphereFluid(dimensionInformation.getFeatureLiquid(random, modifiersForFeature, FeatureType.FEATURE_LIQUIDORBS));
        dimensionInformation.setCanyonBlock(dimensionInformation.getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_CANYONS));
    }

    @Override
    public String[] getInformation() {
        return new String[] { "This affects various features of the dimension.", "Some of these features need material or liquid modifiers", "which you have to put in front of this feature." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
