package powercrystals.minefactoryreloaded.api;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Defines a target for the Laser Drill Precharger
 *
 * @author skyboy
 */
public interface IFactoryLaserTarget {

	/**
	 * Used to be determined if a laser can be formed on <tt>from</tt>
	 *
	 * @param from
	 *            The direction the laser is coming from
	 *
	 * @return True if the precharger can form a beam from this side
	 */
	public boolean canFormBeamWith(ForgeDirection from);

	/**
	 * Used to add energy to the tile.
	 *
	 * @param from
	 *            The direction the energy is coming from
	 * @param energy
	 *            The amount of energy being transferred
	 * @param simulate
	 *            true if this transaction will only be simulated
	 *
	 * @return The amount of energy not consumed
	 */
	public int addEnergy(ForgeDirection from, int energy, boolean simulate);

}
