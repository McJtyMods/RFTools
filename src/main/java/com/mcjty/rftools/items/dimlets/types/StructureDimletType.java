package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.world.types.StructureType;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class StructureDimletType implements IDimletType {
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
    public boolean isModifier() {
        return false;
    }

    @Override
    public boolean isModifiedBy(DimletType type) {
        return false;
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
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_STRUCTURE, dimlets);
        if (dimlets.isEmpty()) {
            while (random.nextFloat() < DimletConfiguration.randomStructureChance) {
                DimletKey key = DimletRandomizer.getRandomStructure(random, false);
                StructureType structureType = DimletObjectMapping.idToStructureType.get(key);
                if (!structureTypes.contains(structureType)) {
                    dimensionInformation.updateCostFactor(key);
                    structureTypes.add(structureType);
                }
            }
        } else {
            for (Pair<DimletKey, List<DimletKey>> dimletWithModifier : dimlets) {
                DimletKey key = dimletWithModifier.getLeft();
                structureTypes.add(DimletObjectMapping.idToStructureType.get(key));
            }
        }
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
