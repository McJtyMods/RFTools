package mcjty.rftools.blocks.shield;

import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.rftools.blocks.shield.filters.ShieldFilter;

import java.util.List;

public class PacketGetFilters extends PacketRequestListFromServer<ShieldFilter, PacketGetFilters, PacketFiltersReady> {

    public PacketGetFilters() {
    }

    public PacketGetFilters(int x, int y, int z) {
        super(x, y, z, ShieldTEBase.CMD_GETFILTERS);
    }

    @Override
    protected PacketFiltersReady createMessageToClient(int x, int y, int z, List<ShieldFilter> result) {
        return new PacketFiltersReady(x, y, z, ShieldTEBase.CLIENTCMD_GETFILTERS, result);
    }
}
