package com.mcjty.rftools.dimension;

import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraftforge.common.DimensionManager;

public class GenericDimension {

    public GenericDimension() {
        DimensionManager.registerProviderType(getDimensionID(), GenericWorldProvider.class, true);
        DimensionManager.registerDimension(getDimensionID(), getDimensionID());

    }

    public static int getDimensionID() {
        return KnownDimletConfiguration.firstDimensionId;
    }
}
