package mcjty.rftools.apideps;

import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class BuildCraftChecker {
    private static int buildcraftPresent = 0;

    public static boolean isBuildcraftPresent() {
        if (buildcraftPresent == 0) {
            if (Loader.isModLoaded("BuildCraft|Core")) {
                buildcraftPresent = 1;
            } else {
                buildcraftPresent = -1;
            }
        }
        return buildcraftPresent > 0;
    }

    public static boolean isBuildcraftWrench(Item item) {
        return item instanceof IToolWrench;
    }


    public static void useBuildcraftWrench(Item item, EntityPlayer player, int x, int y, int z) {
        IToolWrench wrench = (IToolWrench) item;
        wrench.wrenchUsed(player, x, y, z);
    }
}
