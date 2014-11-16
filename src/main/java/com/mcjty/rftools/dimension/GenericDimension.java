package com.mcjty.rftools.dimension;

import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import net.minecraftforge.common.DimensionManager;

public class GenericDimension {

    private static final int GENERIC_DIMENSION_ID = 15;

    public GenericDimension() {
        DimensionManager.registerProviderType(getDimensionID(), GenericWorldProvider.class, true);
        DimensionManager.registerDimension(getDimensionID(), getDimensionID());

    }

    public static int getDimensionID() {
        return GENERIC_DIMENSION_ID;
    }
}
