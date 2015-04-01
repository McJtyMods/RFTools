package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.world.types.TerrainType;
import mcjty.rftools.items.dimlets.*;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TerrainDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_terrain";

    private static int rarity = DimletRandomizer.RARITY_0;
    private static int baseCreationCost = 100;
    private static int baseMaintainCost = 1;
    private static int baseTickCost = 1;

    private static float materialCreationCostFactor = 5.0f;
    private static float liquidCreationCostFactor = 5.0f;
    private static float materialMaintenanceCostFactor = 5.0f;
    private static float liquidMaintenanceCostFactor = 5.0f;
    private static float materialTickCostFactor = 2.0f;
    private static float liquidTickCostFactor = 2.0f;


    @Override
    public String getName() {
        return "Terrain";
    }

    @Override
    public String getOpcode() {
        return "T";
    }

    @Override
    public String getTextureName() {
        return "terrainDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the terrain dimlet type");
        rarity = cfg.get(CATEGORY_TYPE, "rarity", rarity, "Default rarity for this dimlet type").getInt();
        baseCreationCost = cfg.get(CATEGORY_TYPE, "creation.cost", baseCreationCost, "Dimlet creation cost (how much power this dimlets adds during creation time of a dimension)").getInt();
        baseMaintainCost = cfg.get(CATEGORY_TYPE, "maintenance.cost", baseMaintainCost, "Dimlet maintenance cost (how much power this dimlet will use up to keep the dimension running)").getInt();
        baseTickCost = cfg.get(CATEGORY_TYPE, "tick.cost", baseTickCost, "Dimlet tick cost (how long it takes to make a dimension with this dimlet in it)").getInt();
        materialCreationCostFactor = (float) cfg.get(CATEGORY_TYPE, "material.creation.factor", materialCreationCostFactor, "The cost factor for a material dimlet modifier when used in combination with this terrain").getDouble();
        liquidCreationCostFactor = (float) cfg.get(CATEGORY_TYPE, "liquid.creation.factor", liquidCreationCostFactor, "The cost factor for a liquid dimlet modifier when used in combination with this terrain").getDouble();
        materialMaintenanceCostFactor = (float) cfg.get(CATEGORY_TYPE, "material.maintenance.factor", materialMaintenanceCostFactor, "The cost factor for a material dimlet modifier when used in combination with this terrain").getDouble();
        liquidMaintenanceCostFactor = (float) cfg.get(CATEGORY_TYPE, "liquid.maintenance.factor", liquidMaintenanceCostFactor, "The cost factor for a liquid dimlet modifier when used in combination with this terrain").getDouble();
        materialTickCostFactor = (float) cfg.get(CATEGORY_TYPE, "material.tick.factor", materialTickCostFactor, "The cost factor for a material dimlet modifier when used in combination with this terrain").getDouble();
        liquidTickCostFactor = (float) cfg.get(CATEGORY_TYPE, "liquid.tick.factor", liquidTickCostFactor, "The cost factor for a liquid dimlet modifier when used in combination with this terrain").getDouble();
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
        if (modifierType == DimletType.DIMLET_MATERIAL) {
            return materialCreationCostFactor;
        } else if (modifierType == DimletType.DIMLET_LIQUID) {
            return liquidCreationCostFactor;
        } else {
            return 1.0f;
        }
    }

    @Override
    public float getModifierMaintainCostFactor(DimletType modifierType, DimletKey key) {
        if (modifierType == DimletType.DIMLET_MATERIAL) {
            return materialMaintenanceCostFactor;
        } else if (modifierType == DimletType.DIMLET_LIQUID) {
            return liquidMaintenanceCostFactor;
        } else {
            return 1.0f;
        }
    }

    @Override
    public float getModifierTickCostFactor(DimletType modifierType, DimletKey key) {
        if (modifierType == DimletType.DIMLET_MATERIAL) {
            return materialTickCostFactor;
        } else if (modifierType == DimletType.DIMLET_LIQUID) {
            return liquidTickCostFactor;
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
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_TERRAIN, dimlets);
        List<DimletKey> modifiers;
        TerrainType terrainType = TerrainType.TERRAIN_VOID;
        if (dimlets.isEmpty()) {
            // Pick a random terrain type with a seed that is generated from all the
            // dimlets so we always get the same random value for these dimlets.
            List<DimletKey> idList = new ArrayList<DimletKey>(DimletObjectMapping.idToTerrainType.keySet());
            DimletKey key = idList.get(random.nextInt(idList.size()));
            dimensionInformation.updateCostFactor(key);
            terrainType = DimletObjectMapping.idToTerrainType.get(key);
            modifiers = Collections.emptyList();
        } else {
            int index = random.nextInt(dimlets.size());
            DimletKey key = dimlets.get(index).getLeft();
            terrainType = DimletObjectMapping.idToTerrainType.get(key);
            modifiers = dimlets.get(index).getRight();
        }

        List<BlockMeta> blocks = new ArrayList<BlockMeta>();
        List<Block> fluids = new ArrayList<Block>();
        DimensionInformation.getMaterialAndFluidModifiers(modifiers, blocks, fluids);
        dimensionInformation.setTerrainType(terrainType);

        BlockMeta baseBlockForTerrain;
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
                dimensionInformation.updateCostFactor(key);
                baseBlockForTerrain = DimletObjectMapping.idToBlock.get(key);
            } else {
                baseBlockForTerrain = new BlockMeta(Blocks.stone, 0);
            }
        }
        dimensionInformation.setBaseBlockForTerrain(baseBlockForTerrain);

        Block fluidForTerrain;
        if (!fluids.isEmpty()) {
            fluidForTerrain = fluids.get(random.nextInt(fluids.size()));
            if (fluidForTerrain == null) {
                fluidForTerrain = Blocks.water;         // This is the default.
            }
        } else {
            if (random.nextFloat() < DimletConfiguration.randomOceanLiquidChance) {
                DimletKey key = DimletRandomizer.getRandomFluidBlock(random, false);
                dimensionInformation.updateCostFactor(key);
                fluidForTerrain = DimletObjectMapping.idToFluid.get(key);
            } else {
                fluidForTerrain = Blocks.water;
            }
        }
        dimensionInformation.setFluidForTerrain(fluidForTerrain);
    }

    @Override
    public String[] getInformation() {
        return new String[] { "This affects the type of terrain", "that you will get in a dimension", "This dimlet can receive liquid and material", "modifiers which have to come in front of the terrain." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
