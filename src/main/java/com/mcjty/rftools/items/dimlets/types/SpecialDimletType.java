package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.SpecialType;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class SpecialDimletType implements IDimletType {
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
    public boolean isModifier() {
        return false;
    }

    @Override
    public boolean isModifiedBy(DimletType type) {
        return false;
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
