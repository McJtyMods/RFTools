package com.mcjty.rftools;

import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;

public class EntityEvents {

    @SubscribeEvent
    public void onEntityConstructingEvent(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            PlayerExtendedProperties properties = new PlayerExtendedProperties();
            event.entity.registerExtendedProperties(PlayerExtendedProperties.ID, properties);
        }
    }

    @SubscribeEvent
    public void onPlayerInterractEvent(PlayerInteractEvent event) {
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            World world = event.world;
            if (!world.isRemote) {
                Block block = world.getBlock(event.x, event.y, event.z);
                if (block instanceof BlockBed) {
                    RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
                    if (dimensionManager.getDimensionInformation(world.provider.dimensionId) != null) {
                        // We are in an RFTools dimension.
                        switch (DimletConfiguration.bedBehaviour) {
                            case 0:
                                event.setCanceled(true);
                                RFTools.message(event.entityPlayer, "You cannot sleep in this dimension!");
                                break;
                            case 1:
                                // Just do the usual thing (this typically mean explosion).
                                break;
                            case 2:
                                event.setCanceled(true);
                                int meta = BedControl.getBedMeta(world, event.x, event.y, event.z);
                                if (meta != -1) {
                                    BedControl.trySleep(world, event.entityPlayer, event.x, event.y, event.z, meta);
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingFallEvent(LivingFallEvent event) {
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            PlayerExtendedProperties playerExtendedProperties = PlayerExtendedProperties.getProperties(player);
            if (!player.worldObj.isRemote) {
                if (playerExtendedProperties.hasBuff(PlayerBuff.BUFF_FEATHERFALLING)) {
                    event.distance /= 2.0f;
                } else if (playerExtendedProperties.hasBuff(PlayerBuff.BUFF_FEATHERFALLINGPLUS)) {
                    event.distance /= 8.0f;
                }
            }
        }
    }

}
