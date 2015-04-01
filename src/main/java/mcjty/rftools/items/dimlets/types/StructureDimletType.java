package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.world.types.StructureType;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletObjectMapping;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class StructureDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_structure";

    private static int rarity = DimletRandomizer.RARITY_3;
    private static int baseCreationCost = 600;
    private static int baseMaintainCost = 100;
    private static int baseTickCost = 900;

    @Override
    public String getName() {
        return "Structure";
    }

    @Override
    public String getOpcode() {
        return "S";
    }

    @Override
    public String getTextureName() {
        return "structuresDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the structure dimlet type");
        rarity = cfg.get(CATEGORY_TYPE, "rarity", rarity, "Default rarity for this dimlet type").getInt();
        baseCreationCost = cfg.get(CATEGORY_TYPE, "creation.cost", baseCreationCost, "Dimlet creation cost (how much power this dimlets adds during creation time of a dimension)").getInt();
        baseMaintainCost = cfg.get(CATEGORY_TYPE, "maintenance.cost", baseMaintainCost, "Dimlet maintenance cost (how much power this dimlet will use up to keep the dimension running)").getInt();
        baseTickCost = cfg.get(CATEGORY_TYPE, "tick.cost", baseTickCost, "Dimlet tick cost (how long it takes to make a dimension with this dimlet in it)").getInt();
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
        return false;
    }

    @Override
    public float getModifierCreateCostFactor(DimletType modifierType, DimletKey key) {
        return 1.0f;
    }

    @Override
    public float getModifierMaintainCostFactor(DimletType modifierType, DimletKey key) {
        return 1.0f;
    }

    @Override
    public float getModifierTickCostFactor(DimletType modifierType, DimletKey key) {
        return 1.0f;
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
        Set<StructureType> structureTypes = dimensionInformation.getStructureTypes();
        Set<String> dimensionTypes = new HashSet<String>();
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_STRUCTURE, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomStructureChance) {
                DimletKey key = DimletRandomizer.getRandomStructure(random, false);
                StructureType structureType = DimletObjectMapping.idToStructureType.get(key);
                if (!structureTypes.contains(structureType) || (structureType == StructureType.STRUCTURE_RECURRENTCOMPLEX)) {
                    dimensionInformation.updateCostFactor(key);
                    structureTypes.add(structureType);
                    if (structureType == StructureType.STRUCTURE_RECURRENTCOMPLEX) {
                        dimensionTypes.add(DimletObjectMapping.idToRecurrentComplexType.get(key));
                    }
                }
            }
        } else {
            for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
                DimletKey key = dimletWithModifier.getLeft();
                StructureType structureType = DimletObjectMapping.idToStructureType.get(key);
                structureTypes.add(structureType);
                if (structureType == StructureType.STRUCTURE_RECURRENTCOMPLEX) {
                    dimensionTypes.add(DimletObjectMapping.idToRecurrentComplexType.get(key));
                }
            }
        }
        dimensionInformation.setDimensionTypes(dimensionTypes.toArray(new String[dimensionTypes.size()]));
    }

    @Override
    public String[] getInformation() {
        return new String[] { "Control generation of various structures", "in the world." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
