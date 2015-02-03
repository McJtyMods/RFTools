package com.mcjty.rftools;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.WorldEvent;

public class WorldLoadEvent {

//    @SubscribeEvent
//    public void loadEvent(WorldEvent.Load evt) {
//        System.out.println("######### loadEvent: evt.world.isRemote = " + evt.world.isRemote);
//        System.out.println("evt.world.provider.dimensionId = " + evt.world.provider.dimensionId);
//
//        if (evt.world.isRemote) {
//            return;
//        }
//        int d = evt.world.provider.dimensionId;
//        if (d == 0) {
//            TeleportDestinations.clearInstance();
//            RfToolsDimensionManager.clearInstance();
//            DimensionStorage.clearInstance();
//        }
//    }

    @SubscribeEvent
    public void loadEvent(WorldEvent.Load evt) {
        if (evt.world.isRemote) {
            if (!KnownDimletConfiguration.isInitialized()) {
                RFTools.log("Clientside World Load Event: initialize dimlets");
                KnownDimletConfiguration.init();
                KnownDimletConfiguration.initCrafting();
            }
        } else if (MinecraftServer.getServer().isDedicatedServer()) {
            if (evt.world.provider.dimensionId == 0 && !KnownDimletConfiguration.isInitialized()) {
                RFTools.log("Serverside World Load Event: initialize dimlets");
                KnownDimletConfiguration.init();
                KnownDimletConfiguration.initCrafting();
            }
        }
    }

    @SubscribeEvent
    public void unloadEvent(WorldEvent.Unload evt) {
        if (evt.world.isRemote) {
            return;
        }
        int d = evt.world.provider.dimensionId;
        if (d == 0) {
            RfToolsDimensionManager.unregisterDimensions();
        }
    }
}
