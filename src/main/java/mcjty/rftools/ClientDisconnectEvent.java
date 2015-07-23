package mcjty.rftools;

import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import mcjty.varia.Logging;

public class ClientDisconnectEvent {

    @SubscribeEvent
    public void onDisconnectedFromServerEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Logging.log("Disconnect from server: Unregistering RFTools dimensions");
        RfToolsDimensionManager.unregisterDimensions();
        KnownDimletConfiguration.clean();
    }

}
