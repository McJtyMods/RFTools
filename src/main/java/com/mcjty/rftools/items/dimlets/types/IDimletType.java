package com.mcjty.rftools.items.dimlets.types;

import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Random;

public interface IDimletType {
    String getName();
    String getOpcode();
    String getTextureName();

    // Return true if this dimlet is a modifier.
    boolean isModifier();

    /**
     * Return true if this type can be modified by the given type.
     * @param type
     */
    public boolean isModifiedBy(DimletType type);

    // Return true if this dimlet can be injected into an existing dimension.
    boolean isInjectable();

    // Inject this dimlet into a dimension.
    void inject(DimletKey key, DimensionInformation dimensionInformation);

    /**
     * Given a list of dimlets and associated modifiers, try to parse the dimlets for this
     * given type and construct the dimension.
     * @param dimlets
     * @param random
     * @param dimensionInformation the dimension information
     */
    void constructDimension(List<Pair<DimletKey,List<DimletKey>>> dimlets, Random random, DimensionInformation dimensionInformation);


    public String[] getInformation();

    /**
     * Attempt to craft a dimlet of this type given a controller, a memory part, an energy part and an essence item.
     *
     * @param stackController
     * @param stackMemory
     * @param stackEnergy
     * @param stackEssence
     * @return an itemstack if succesful
     */
    DimletKey attemptDimletCrafting(ItemStack stackController, ItemStack stackMemory, ItemStack stackEnergy, ItemStack stackEssence);

}
