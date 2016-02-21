package mcjty.rftools.api.screens.data;

/**
 * A data module for representing contents and how that contents evolves
 * (example, RF, RF/tick and so on)
 */
public interface IModuleDataContents extends IModuleData {

    long getContents();

    long getMaxContents();

    long getLastPerTick();
}
