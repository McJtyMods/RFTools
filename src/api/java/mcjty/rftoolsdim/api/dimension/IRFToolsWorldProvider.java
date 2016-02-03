package mcjty.rftoolsdim.api.dimension;

/**
 * RFTools dimension providers will implement this interface. You can querry for this to ask information
 * about this dimension:
 *     (RFToolsWorldProvider)(world.provider)
 */
public interface IRFToolsWorldProvider {
    /**
     * Get the amount of RF left in this dimension (only works server-side).
     */
    int getCurrentRF();
}
