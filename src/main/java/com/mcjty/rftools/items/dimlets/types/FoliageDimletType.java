package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public class FoliageDimletType implements IDimletType {
    @Override
    public String getName() {
        return "Foliage";
    }

    @Override
    public String getOpcode() {
        return "F";
    }

    @Override
    public String getTextureName() {
        return "foliageDimlet";
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

    }

    @Override
    public String[] getInformation() {
        return new String[] { "WIP" };
    }

    @Override
    public ItemStack attemptDimletCrafting(World world, ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence) {
        return null;
    }
}
