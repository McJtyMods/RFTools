package mcjty.rftoolsdim.api.dimension;

import net.minecraft.world.World;

/**
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("rftoolsdim", "getDimensionManager", "<whatever>.YourClass$GetDimensionManager");
 */
public interface IDimensionManager {

    /**
     * Return true if this dimension represents an RFTools dimension.
     * Only call this server-side
     * @param world is a reference to any world (overworld is fine)
     * @param id
     * @return
     */
    boolean isRFToolsDimension(World world, int id);

    /**
     * Return the power left in the current dimension. Returns -1 if it is not
     * an RFTools dimension
     * Only call this server-side
     * @param world is a reference to any world (overworld is fine)
     * @param id
     * @return
     */
    int getCurrentRF(World world, int id);

    /**
     * Get the dimension information. If the id doesn't represent an RFTools
     * dimension this just returns null.
     * Only call this server-side
     * @param world is a reference to any world (overworld is fine)
     * @param id
     * @return
     */
    IDimensionInformation getDimensionInformation(World world, int id);
}
