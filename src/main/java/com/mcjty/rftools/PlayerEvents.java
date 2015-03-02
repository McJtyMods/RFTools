package com.mcjty.rftools;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.network.DimensionSyncPacket;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerEvents {

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.worldObj.isRemote) {
            IExtendedEntityProperties properties = event.player.getExtendedProperties(PlayerExtendedProperties.ID);
            if (properties instanceof PlayerExtendedProperties) {
                PlayerExtendedProperties playerExtendedProperties = (PlayerExtendedProperties) properties;
                playerExtendedProperties.tick();
            }
        }
    }

    @SubscribeEvent
    public void onItemPickupEvent(PlayerEvent.ItemPickupEvent event) {
        if (event.pickedUp != null) {
            ItemStack stack = event.pickedUp.getEntityItem();
            if (stack != null) {
                Item item = stack.getItem();
                if (item == ModItems.unknownDimlet) {
                    Achievements.trigger(event.player, Achievements.theFirstStep);
                } else if (item == ModItems.dimensionalShard) {
                    Achievements.trigger(event.player, Achievements.specialOres);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemCraftedEvent(PlayerEvent.ItemCraftedEvent event) {
        if (event.crafting != null) {
            Item item = event.crafting.getItem();
            if (item instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) item;
                if (itemBlock.field_150939_a == ModBlocks.endergenicBlock) {
                    Achievements.trigger(event.player, Achievements.hardPower);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        RFTools.log("SMP: Player logged in: Sync diminfo to clients");
        EntityPlayer player = event.player;
        RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(player.getEntityWorld());
        manager.syncDimInfoToClients(player.getEntityWorld());
        manager.checkDimletConfig(player);
    }

    @SubscribeEvent
    public void onConnectionCreated(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        RFTools.log("SMP: Sync dimensions to client");
        DimensionSyncPacket packet = new DimensionSyncPacket();

        EntityPlayer player = ((NetHandlerPlayServer) event.handler).playerEntity;
        RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(player.getEntityWorld());
        for (Integer id : manager.getDimensions().keySet()) {
            RFTools.log("Sending over dimension " + id + " to the client");
            packet.addDimension(id);
        }

        RFTools.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DISPATCHER);
        RFTools.channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(event.manager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get());
        RFTools.channels.get(Side.SERVER).writeOutbound(packet);
    }


}
