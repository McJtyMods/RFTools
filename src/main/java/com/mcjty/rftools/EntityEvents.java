package com.mcjty.rftools;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;

public class EntityEvents {

    @SubscribeEvent
    public void onEntityConstructingEvent(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            PlayerExtendedProperties properties = new PlayerExtendedProperties();
            event.entity.registerExtendedProperties(PlayerExtendedProperties.ID, properties);
        }
    }

    @SubscribeEvent
    public void onLivingFallEvent(LivingFallEvent event) {
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            PlayerExtendedProperties playerExtendedProperties = PlayerExtendedProperties.getProperties(player);
            if (!player.worldObj.isRemote) {
                if (playerExtendedProperties.hasBuff(PlayerBuff.BUFF_FEATHERFALLING)) {
                    System.out.println("We have the BUFF!");
                    event.distance /= 5.0f;
                } else {
                    System.out.println("That hurts!");
                }
            }
        }
    }

}
