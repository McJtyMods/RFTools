package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletObjectMapping;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class DigitDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Digit";
    }

    @Override
    public String getOpcode() {
        return "d";
    }

    @Override
    public String getTextureName() {
        return "digitDimlet";
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
        dimlets = DimensionInformation.extractType(DimletType.DIMLET_DIGIT, dimlets);
        String digitString = "";
        for (Pair<DimletKey, List<DimletKey>> dimletWithModifiers : dimlets) {
            DimletKey key = dimletWithModifiers.getKey();
            digitString += DimletObjectMapping.idToDigit.get(key);
        }
        dimensionInformation.setDigitString(digitString);
    }

    @Override
    public String[] getInformation() {
        return new String[] { "This dimlet has no effect on the dimension", "but can be used to get new unique dimensions", "with exactly the same dimlets." };
    }

    @Override
    public DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
