package com.mcjty.rftools.dimension;

import cpw.mods.fml.common.registry.GameRegistry;

public class ModDimensions {
    public static void init() {
        GameRegistry.registerWorldGenerator(new GenericWorldGenerator(), 1000);
    }
}
