package mcjty.xnet.api;

import mcjty.xnet.api.channels.IChannelType;

/**
 * Main interface for XNet.
 * Get a reference to an implementation of this interface by calling:
 *         FMLInterModComms.sendFunctionMessage("xnet", "getXNet", "<whatever>.YourClass$GetXNet");
 */
public interface IXNet {

    void registerChannelType(IChannelType type);
}
