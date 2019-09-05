package mcjty.rftools.api.screens;

import net.minecraftforge.common.capabilities.Capability;

/**
 * Implement this interface on your module item.
 */
public interface IModuleProvider {
    // @todo 1.14
//    @CapabilityInject(IModuleProvider.class)
    public static Capability<IModuleProvider> CAPABILITY = null;

    Class<? extends IScreenModule<?>> getServerScreenModule();

    Class<? extends IClientScreenModule<?>> getClientScreenModule();

    String getModuleName();

    void createGui(IModuleGuiBuilder guiBuilder);
}
