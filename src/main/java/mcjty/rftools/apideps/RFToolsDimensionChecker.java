package mcjty.rftools.apideps;

import mcjty.rftoolsdim.api.dimension.IRFToolsWorldProvider;
import net.minecraft.world.World;

/**
 * Check if a given dimension is an RFTools dimension
 */
public class RFToolsDimensionChecker {

    public static boolean isRFToolsDimension(World world) {
        return world.provider instanceof IRFToolsWorldProvider;
    }
}
