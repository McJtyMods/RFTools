package com.mcjty.rftools;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
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
    public void unloadEvent(WorldEvent.Unload evt) {
        System.out.println("######### unloadEvent: evt.world.isRemote = " + evt.world.isRemote);
        System.out.println("evt.world.provider.dimensionId = " + evt.world.provider.dimensionId);
        if (evt.world.isRemote) {
            return;
        }
        int d = evt.world.provider.dimensionId;
        if (d == 0) {
            RfToolsDimensionManager.unregisterDimensions();
        }
    }
}
