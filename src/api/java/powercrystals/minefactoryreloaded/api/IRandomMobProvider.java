package powercrystals.minefactoryreloaded.api;

import java.util.List;

import net.minecraft.world.World;

public interface IRandomMobProvider {

	/**
	 * Called to provide random entities to be spawned by mystery SafariNets
	 *
	 * @param world
	 *            The world object the entities will be spawned in.
	 * @return A list of RandomMob instances of entities that are all ready to
	 *         be spawned in the world with no additional method calls.
	 */
	public List<RandomMob> getRandomMobs(World world);

}
