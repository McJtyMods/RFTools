package mcjty.rftools;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import mcjty.lib.varia.Logging;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;

public class ClientDisconnectEvent {

    @SubscribeEvent
    public void onDisconnectedFromServerEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Logging.log("Disconnect from server: Unregistering RFTools dimensions");
        RfToolsDimensionManager.unregisterDimensions();
        KnownDimletConfiguration.clean();
    }

}
