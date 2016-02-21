package mcjty.rftools.api.screens;

import mcjty.rftools.api.screens.data.IModuleDataFactory;

/**
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("rftools", "getScreenModuleRegistry", "<whatever>.YourClass$GetDimensionManager");
 */
public interface IScreenModuleRegistry {

    /**
     * Register a module data factory.
     * @param id a unique id in the form modid:name
     * @param dataFactory
     */
    void registerModuleDataFactory(String id, IModuleDataFactory dataFactory);

    IModuleDataFactory getModuleDataFactory(String id);
}
