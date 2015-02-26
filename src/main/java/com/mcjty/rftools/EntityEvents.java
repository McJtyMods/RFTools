package com.mcjty.rftools;

import com.mcjty.rftools.items.teleportprobe.TimedTeleportationProperties;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;

public class EntityEvents {

    @SubscribeEvent
    public void onEntityConstructingEvent(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            TimedTeleportationProperties properties = new TimedTeleportationProperties();
            event.entity.registerExtendedProperties(TimedTeleportationProperties.ID, properties);
        }
    }


}
