package mcjty.rftools;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.environmental.PeacefulAreaManager;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

public class ForgeEventHandlers {

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
    public void onEntitySpawnEvent(LivingSpawnEvent.CheckSpawn event) {
        if (event.entity instanceof IMob) {
            Coordinate coordinate = new Coordinate((int) event.entity.posX, (int) event.entity.posY, (int) event.entity.posZ);
            if (PeacefulAreaManager.isPeaceful(new GlobalCoordinate(coordinate, event.world.provider.dimensionId))) {
                event.setResult(Event.Result.DENY);
                RFTools.logDebug("Prevented a spawn of " + event.entity.getClass().getName());
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
