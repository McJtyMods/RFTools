package mcjty.rftools.api.screens;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

/**
 * Implement this interface on your module item.
 */
public interface IModuleProvider {
    @CapabilityInject(IModuleProvider.class)
    public static Capability<IModuleProvider> CAPABILITY = null;

    Class<? extends IScreenModule<?>> getServerScreenModule();

    Class<? extends IClientScreenModule<?>> getClientScreenModule();

    String getName();
}
