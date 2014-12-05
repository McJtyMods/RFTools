package com.mcjty.rftools.apideps;

import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.Loader;
import net.minecraft.item.Item;

public class BuildCraftChecker {
    private static int buildcraftPresent = 0;

    public static boolean isBuildcraftPresent() {
        if (buildcraftPresent == 0) {
            if (Loader.isModLoaded("BuildCraft|Core")) {
                buildcraftPresent = 1;
            } else {
                buildcraftPresent = -11;
            }
        }
        return buildcraftPresent > 0;
    }

    public static boolean isBuildcraftWrench(Item item) {
        if (!isBuildcraftPresent()) {
            return false;
        }
        return item instanceof IToolWrench;
    }
}
