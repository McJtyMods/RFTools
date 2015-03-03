package com.mcjty.rftools;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.WorldEvent;

public class WorldLoadEvent {

    @SubscribeEvent
    public void loadEvent(WorldEvent.Load evt) {
        if (evt.world.isRemote) {
            // Do nothing on client.
            return;
        }

        if (MinecraftServer.getServer().isDedicatedServer()) {
            // If we are on a server then we initialize here.
            if (evt.world.provider.dimensionId == 0 && !KnownDimletConfiguration.isInitialized()) {
                RFTools.log("Serverside World Load Event: initialize dimlets");
                KnownDimletConfiguration.initServer(evt.world);
                KnownDimletConfiguration.initCrafting(evt.world);
            }
        } else {
            // If we are on a single player client then we connect here.
            if (!KnownDimletConfiguration.isInitialized()) {
                RFTools.log("Single player World Load Event: initialize dimlets");
                KnownDimletConfiguration.initClient(evt.world);
                KnownDimletConfiguration.initCrafting(evt.world);
            }
        }
    }

    @SubscribeEvent
    public void unloadEvent(WorldEvent.Unload evt) {
        int d = evt.world.provider.dimensionId;
        if (d != 0) {
            return;
        }

        if (!evt.world.isRemote) {
            if (MinecraftServer.getServer().isDedicatedServer()) {
                RfToolsDimensionManager.unregisterDimensions();
                KnownDimletConfiguration.clean();
            }
        }
    }
}
