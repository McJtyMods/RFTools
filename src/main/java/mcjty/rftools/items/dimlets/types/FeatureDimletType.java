package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.world.types.FeatureType;
import mcjty.rftools.dimension.world.types.TerrainType;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletObjectMapping;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.DimletType;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class FeatureDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_feature";

    private static int rarity = DimletRandomizer.RARITY_0;
    private static int baseCreationCost = 100;
    private static int baseMaintainCost = 1;
    private static int baseTickCost = 1;

    private static class FactorCosts {
        float materialCreationCostFactor;
        float materialMaintenanceCostFactor;
        float materialTickCostFactor;
        float liquidCreationCostFactor;
        float liquidMaintenanceCostFactor;
        float liquidTickCostFactor;

        private FactorCosts(float materialCreationCostFactor, float materialMaintenanceCostFactor, float materialTickCostFactor, float liquidCreationCostFactor, float liquidMaintenanceCostFactor, float liquidTickCostFactor) {
            this.materialCreationCostFactor = materialCreationCostFactor;
            this.materialMaintenanceCostFactor = materialMaintenanceCostFactor;
            this.materialTickCostFactor = materialTickCostFactor;
            this.liquidCreationCostFactor = liquidCreationCostFactor;
            this.liquidMaintenanceCostFactor = liquidMaintenanceCostFactor;
            this.liquidTickCostFactor = liquidTickCostFactor;
        }
    }

    // Index in the following array is the cost class from FeatureType.
    private static FactorCosts[] factors = new FactorCosts[4];
    static {
        factors[0] = new FactorCosts(1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
        factors[1] = new FactorCosts(1.4f, 1.4f, 1.3f, 1.4f, 1.4f, 1.3f);
        factors[2] = new FactorCosts(1.8f, 1.8f, 1.5f, 1.8f, 1.8f, 1.5f);
        factors[3] = new FactorCosts(2.5f, 2.5f, 1.7f, 2.5f, 2.5f, 1.7f);
    }

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
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the feature dimlet type");
        rarity = cfg.get(CATEGORY_TYPE, "rarity", rarity, "Default rarity for this dimlet type").getInt();
        baseCreationCost = cfg.get(CATEGORY_TYPE, "creation.cost", baseCreationCost, "Dimlet creation cost (how much power this dimlets adds during creation time of a dimension)").getInt();
        baseMaintainCost = cfg.get(CATEGORY_TYPE, "maintenance.cost", baseMaintainCost, "Dimlet maintenance cost (how much power this dimlet will use up to keep the dimension running)").getInt();
        baseTickCost = cfg.get(CATEGORY_TYPE, "tick.cost", baseTickCost, "Dimlet tick cost (how long it takes to make a dimension with this dimlet in it)").getInt();

        String[] desc = new String[] { "lowest class", "low class", "medium class", "high class" };
        for (int i = 0 ; i < 4 ; i++) {
            FactorCosts fc = factors[i];
            fc.materialCreationCostFactor = (float) cfg.get(CATEGORY_TYPE, "material.creation.factor." + i, fc.materialCreationCostFactor, "The cost factor for a material dimlet modifier when used in combination with a feature of " + desc[i]).getDouble();
            fc.liquidCreationCostFactor = (float) cfg.get(CATEGORY_TYPE, "liquid.creation.factor." + i, fc.liquidCreationCostFactor, "The cost factor for a liquid dimlet modifier when used in combination with a feature of " + desc[i]).getDouble();
            fc.materialMaintenanceCostFactor = (float) cfg.get(CATEGORY_TYPE, "material.maintenance.factor." + i, fc.materialMaintenanceCostFactor, "The cost factor for a material dimlet modifier when used in combination with a feature of " + desc[i]).getDouble();
            fc.liquidMaintenanceCostFactor = (float) cfg.get(CATEGORY_TYPE, "liquid.maintenance.factor." + i, fc.liquidMaintenanceCostFactor, "The cost factor for a liquid dimlet modifier when used in combination with a feature of " + desc[i]).getDouble();
            fc.materialTickCostFactor = (float) cfg.get(CATEGORY_TYPE, "material.tick.factor." + i, fc.materialTickCostFactor, "The cost factor for a material dimlet modifier when used in combination with a feature of " + desc[i]).getDouble();
            fc.liquidTickCostFactor = (float) cfg.get(CATEGORY_TYPE, "liquid.tick.factor." + i, fc.liquidTickCostFactor, "The cost factor for a liquid dimlet modifier when used in combination with a feature of " + desc[i]).getDouble();
        }
    }

    @Override
    public int getRarity() {
        return rarity;
    }

    @Override
    public int getCreationCost() {
        return baseCreationCost;
    }

    @Override
    public int getMaintenanceCost() {
        return baseMaintainCost;
    }

    @Override
    public int getTickCost() {
        return baseTickCost;
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
    public float getModifierCreateCostFactor(DimletType modifierType, DimletKey key) {
        FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
        if (modifierType == DimletType.DIMLET_MATERIAL) {
            return factors[featureType.getMaterialClass()].materialCreationCostFactor;
        } else if (modifierType == DimletType.DIMLET_LIQUID) {
            return factors[featureType.getLiquidClass()].liquidCreationCostFactor;
        } else {
            return 1.0f;
        }
    }

    @Override
    public float getModifierMaintainCostFactor(DimletType modifierType, DimletKey key) {
        FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
        if (modifierType == DimletType.DIMLET_MATERIAL) {
            return factors[featureType.getMaterialClass()].materialMaintenanceCostFactor;
        } else if (modifierType == DimletType.DIMLET_LIQUID) {
            return factors[featureType.getLiquidClass()].liquidMaintenanceCostFactor;
        } else {
            return 1.0f;
        }
    }

    @Override
    public float getModifierTickCostFactor(DimletType modifierType, DimletKey key) {
        FeatureType featureType = DimletObjectMapping.idToFeatureType.get(key);
        if (modifierType == DimletType.DIMLET_MATERIAL) {
            return factors[featureType.getMaterialClass()].materialTickCostFactor;
        } else if (modifierType == DimletType.DIMLET_LIQUID) {
            return factors[featureType.getLiquidClass()].liquidTickCostFactor;
        } else {
            return 1.0f;
        }
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

        dimensionInformation.setFluidsForLakes(getRandomFluidArray(random, dimensionInformation, featureTypes, modifiersForFeature, FeatureType.FEATURE_LAKES, true));
        dimensionInformation.setExtraOregen(getRandomBlockArray(random, featureTypes, modifiersForFeature, FeatureType.FEATURE_OREGEN, true));
        dimensionInformation.setTendrilBlock(dimensionInformation.getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_TENDRILS));
        dimensionInformation.setSphereBlocks(getRandomBlockArray(random, featureTypes, modifiersForFeature, FeatureType.FEATURE_ORBS, false));
        dimensionInformation.setHugeSphereBlocks(getRandomBlockArray(random, featureTypes, modifiersForFeature, FeatureType.FEATURE_HUGEORBS, false));
        dimensionInformation.setLiquidSphereBlocks(getRandomBlockArray(random, featureTypes, modifiersForFeature, FeatureType.FEATURE_LIQUIDORBS, false));
        dimensionInformation.setLiquidSphereFluids(getRandomFluidArray(random, dimensionInformation, featureTypes, modifiersForFeature, FeatureType.FEATURE_LIQUIDORBS, false));
        dimensionInformation.setHugeLiquidSphereBlocks(getRandomBlockArray(random, featureTypes, modifiersForFeature, FeatureType.FEATURE_HUGELIQUIDORBS, false));
        dimensionInformation.setHugeLiquidSphereFluids(getRandomFluidArray(random, dimensionInformation, featureTypes, modifiersForFeature, FeatureType.FEATURE_HUGELIQUIDORBS, false));
        dimensionInformation.setCanyonBlock(dimensionInformation.getFeatureBlock(random, modifiersForFeature, FeatureType.FEATURE_CANYONS));
    }

    private Block[] getRandomFluidArray(Random random, DimensionInformation dimensionInformation, Set<FeatureType> featureTypes, Map<FeatureType, List<DimletKey>> modifiersForFeature, FeatureType t, boolean allowEmpty) {
        Block[] fluidsForLakes;
        if (featureTypes.contains(t)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            DimensionInformation.getMaterialAndFluidModifiers(modifiersForFeature.get(t), blocks, fluids);

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
            for (int i = 0 ; i < fluidsForLakes.length ; i++) {
                if (fluidsForLakes[i] == null) {
                    fluidsForLakes[i] = Blocks.water;
                }
            }
        } else {
            fluidsForLakes = new Block[0];
        }
        if (allowEmpty || fluidsForLakes.length > 0) {
            return fluidsForLakes;
        }

        return new Block[] { Blocks.water };
    }

    private BlockMeta[] getRandomBlockArray(Random random, Set<FeatureType> featureTypes, Map<FeatureType, List<DimletKey>> modifiersForFeature, FeatureType t, boolean allowEmpty) {
        BlockMeta[] blockArray;
        if (featureTypes.contains(t)) {
            List<BlockMeta> blocks = new ArrayList<BlockMeta>();
            List<Block> fluids = new ArrayList<Block>();
            DimensionInformation.getMaterialAndFluidModifiers(modifiersForFeature.get(t), blocks, fluids);

            // If no blocks are specified we will generate a few random ores.
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

            blockArray = blocks.toArray(new BlockMeta[blocks.size()]);
            for (int i = 0 ; i < blockArray.length ; i++) {
                if (blockArray[i] == null) {
                    blockArray[i] = BlockMeta.STONE;
                }
            }
        } else {
            blockArray = new BlockMeta[0];
        }
        if (allowEmpty || blockArray.length > 0) {
            return blockArray;
        }
        return new BlockMeta[] { BlockMeta.STONE };
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
