package powercrystals.minefactoryreloaded.api;

import net.minecraft.entity.EntityLivingBase;

public interface ILiquidDrinkHandler {

	/**
	 * Called when an entity has consumed the fluid this manages.
	 *
	 * @param entity
	 *            The entity that has consumed the fluid this
	 *            ILiquidDrinkHandler manages
	 */
	public void onDrink(EntityLivingBase entity);

}
