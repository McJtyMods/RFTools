package com.mcjty.rftools;

import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRegisterDimensions;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PlayerLoginEvent {
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event){
        RFTools.log("Player logged in!");
        EntityPlayer player = event.player;
        RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(player.getEntityWorld());
        for (Integer id : manager.getDimensions().keySet()) {
            RFTools.log("Sending over dimension " + id + " to the client");
            PacketHandler.INSTANCE.sendTo(new PacketRegisterDimensions(id), (EntityPlayerMP) player);
        }
    }

}
