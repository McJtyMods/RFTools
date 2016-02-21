package mcjty.rftools.api.screens;

import mcjty.rftools.api.screens.data.IModuleDataFactory;

/**
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("rftools", "getScreenModuleRegistry", "<whatever>.YourClass$GetDimensionManager");
 */
public interface IScreenModuleRegistry {

    /**
     * Register a module data factory. This is needed so that RFTools know how to deserialize your module data
     * for your screen module. You don't have to do this for any of the builtin module data implementations
     * (like IModuleDataBoolean, IModuleDataInteger and so on). Note that you only need the screen module registry
     * if you have custom data.
     *
     * @param id a unique id in the form modid:name
     * @param dataFactory
     */
    void registerModuleDataFactory(String id, IModuleDataFactory dataFactory);

    IModuleDataFactory getModuleDataFactory(String id);
}
