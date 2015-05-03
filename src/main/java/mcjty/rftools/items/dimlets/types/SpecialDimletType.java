package mcjty.rftools.items.dimlets.types;

import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.world.types.SpecialType;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletObjectMapping;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class SpecialDimletType implements IDimletType {
    private static final String CATEGORY_TYPE = "type_special";

    private static int rarity = DimletRandomizer.RARITY_5;
    private static int baseCreationCost = 1000;
    private static int baseMaintainCost = 1000;
    private static int baseTickCost = 1000;

    @Override
    public String getName() {
        return "Special";
    }

    @Override
    public String getOpcode() {
        return "X";
    }

    @Override
    public String getTextureName() {
        return "specialDimlet";
    }

    @Override
    public void setupFromConfig(Configuration cfg) {
        cfg.addCustomCategoryComment(CATEGORY_TYPE, "Settings for the special dimlet type");
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
        return true;
    }

    @Override
    public void inject(DimletKey key, DimensionInformation dimensionInformation) {
        SpecialType specialType = DimletObjectMapping.idToSpecialType.get(key);
        if (specialType == SpecialType.SPECIAL_PEACEFUL) {
            dimensionInformation.setPeaceful(true);
        } else if (specialType == SpecialType.SPECIAL_NOANIMALS) {
            dimensionInformation.setNoanimals(true);
        } else if (specialType == SpecialType.SPECIAL_SPAWN) {
            dimensionInformation.setRespawnHere(true);
        }
    }

    @Override
    public void constructDimension(List<Pair<DimletKey, List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation) {
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_SPECIAL, dimlets);
        for (Pair<DimletKey, List<DimletKey>> dimlet : dimlets) {
            DimletKey key = dimlet.getLeft();
            SpecialType specialType = DimletObjectMapping.idToSpecialType.get(key);
            if (specialType == SpecialType.SPECIAL_PEACEFUL) {
                dimensionInformation.setPeaceful(true);
            } else if (specialType == SpecialType.SPECIAL_NOANIMALS) {
                dimensionInformation.setNoanimals(true);
            } else if (specialType == SpecialType.SPECIAL_SHELTER) {
                dimensionInformation.setShelter(true);
            } else if (specialType == SpecialType.SPECIAL_SPAWN) {
                dimensionInformation.setRespawnHere(true);
            }
        }
    }

    @Override
    public String[] getInformation() {
        return new String[] { "Special dimlets with various features." };
    }

    private static boolean isValidSpecialEssence(ItemStack stackEssence) {
        if (stackEssence.getItem() == ModItems.peaceEssenceItem) {
            return true;
        }
        if (stackEssence.getItem() == ModItems.efficiencyEssenceItem) {
            return true;
        }
        if (stackEssence.getItem() == ModItems.mediocreEfficiencyEssenceItem) {
            return true;
        }

        return false;
    }

    private static DimletKey findSpecialDimlet(ItemStack stackEssence) {
        if (stackEssence.getItem() == ModItems.peaceEssenceItem) {
            return new DimletKey(DimletType.DIMLET_SPECIAL, "Peaceful");
        } else if (stackEssence.getItem() == ModItems.efficiencyEssenceItem) {
            return new DimletKey(DimletType.DIMLET_SPECIAL, "Efficiency");
        } else if (stackEssence.getItem() == ModItems.mediocreEfficiencyEssenceItem) {
            return new DimletKey(DimletType.DIMLET_SPECIAL, "Mediocre Efficiency");
        }
        return null;
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        if (!isValidSpecialEssence(stackEssence)) {
            return null;
        }
        DimletKey specialDimlet = findSpecialDimlet(stackEssence);
        if (specialDimlet == null) {
            return null;
        }
        if (!DimletCraftingTools.matchDimletRecipe(specialDimlet, stackController, stackMemory, stackEnergy)) {
            return null;
        }
        return specialDimlet;
    }
}
