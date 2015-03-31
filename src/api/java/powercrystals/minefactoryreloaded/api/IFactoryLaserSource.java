package powercrystals.minefactoryreloaded.api;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Defines a target for the laser blocks. TileEntities that implement this
 * interface will sustain the beam.
 *
 * @author skyboy
 */
public interface IFactoryLaserSource {

	/**
	 * Used to determine if laser blocks can remain in the world when emitted
	 * from <tt>from</tt>
	 *
	 * @param from
	 *            The direction the laser is oriented
	 *
	 * @return True if the beam should be sustained from this side
	 */
	public boolean canFormBeamFrom(ForgeDirection from);

}
