package mcjty.rftoolsdim.api.dimension;

import java.util.UUID;

/**
 * Get information of an RFTools dimension
 */
public interface IDimensionInformation {

    /**
     * Get the name of this dimension.
     * @return
     */
    String getName();

    /**
     * Get the owner of this dimension.
     * @return
     */
    UUID getOwner();
}
