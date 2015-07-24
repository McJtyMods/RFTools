package mcjty.rftools;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.blockprotector.BlockProtectors;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.environmental.PeacefulAreaManager;
import mcjty.rftools.dimension.DimensionInformation;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import mcjty.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForgeEventHandlers {


//    @SubscribeEvent
//    public void onGrassColorRequest(BiomeEvent.GetGrassColor event) {
//        event.
//        event.newColor = BiomeMutator.PINK;
//    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        BlockProtectors blockProtectors = BlockProtectors.getProtectors(event.world);
        if (blockProtectors == null) {
            return;
        }

        int id = event.world.provider.dimensionId;
        Collection<GlobalCoordinate> protectors = blockProtectors.findProtectors(event.x, event.y, event.z, id, 2);

        for (GlobalCoordinate protector : protectors) {
            int cx = protector.getCoordinate().getX();
            int cy = protector.getCoordinate().getY();
            int cz = protector.getCoordinate().getZ();
            TileEntity te = event.world.getTileEntity(cx, cy, cz);
            if (te instanceof BlockProtectorTileEntity) {
                BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                Coordinate relative = blockProtectorTileEntity.absoluteToRelative(event.x, event.y, event.z);
                boolean b = blockProtectorTileEntity.isProtected(relative);
                if (b) {
                    if (blockProtectorTileEntity.attemptHarvestProtection()) {
                        event.setCanceled(true);
                    } else {
                        blockProtectorTileEntity.removeProtection(relative);
                    }
                    return;
                }
            }
        }
    }


    @SubscribeEvent
    public void onDetonate(ExplosionEvent.Detonate event) {
        BlockProtectors blockProtectors = BlockProtectors.getProtectors(event.world);
        if (blockProtectors == null) {
            return;
        }

        int id = event.world.provider.dimensionId;
        Explosion explosion = event.explosion;
        Collection<GlobalCoordinate> protectors = blockProtectors.findProtectors((int) explosion.explosionX, (int) explosion.explosionY, (int) explosion.explosionZ, id, (int) explosion.explosionSize);

        if (protectors.isEmpty()) {
            return;
        }

        List<ChunkPosition> affectedBlocks = event.getAffectedBlocks();
        List<ChunkPosition> toremove = new ArrayList<ChunkPosition>();

        Vec3 explosionVector = Vec3.createVectorHelper(explosion.explosionX, explosion.explosionY, explosion.explosionZ);

        int rf = 0;
        for (GlobalCoordinate protector : protectors) {
            int cx = protector.getCoordinate().getX();
            int cy = protector.getCoordinate().getY();
            int cz = protector.getCoordinate().getZ();
            TileEntity te = event.world.getTileEntity(cx, cy, cz);
            if (te instanceof BlockProtectorTileEntity) {
                BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                for (ChunkPosition block : affectedBlocks) {
                    Coordinate relative = blockProtectorTileEntity.absoluteToRelative(block.chunkPosX, block.chunkPosY, block.chunkPosZ);
                    boolean b = blockProtectorTileEntity.isProtected(relative);
                    if (b) {
                        Vec3 blockVector = Vec3.createVectorHelper(block.chunkPosX, block.chunkPosY, block.chunkPosZ);
                        double distanceTo = explosionVector.distanceTo(blockVector);
                        int rfneeded = blockProtectorTileEntity.attemptExplosionProtection((float) (distanceTo / explosion.explosionSize), explosion.explosionSize);
                        if (rfneeded > 0) {
                            toremove.add(block);
                            rf += rfneeded;
                        } else {
                            blockProtectorTileEntity.removeProtection(relative);
                        }
                    }
                }
            }
        }

        for (ChunkPosition block : toremove) {
            affectedBlocks.remove(block);
        }

        Logging.logDebug("RF Needed for one explosion:" + rf);
    }

    @SubscribeEvent
    public void onAttackEntityEvent(AttackEntityEvent event) {
        World world = event.entityPlayer.getEntityWorld();
        int id = world.provider.dimensionId;
        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
        if (dimensionManager.getDimensionInformation(id) != null) {
            // RFTools dimension.
            DimensionStorage storage = DimensionStorage.getDimensionStorage(world);
            int energy = storage.getEnergyLevel(id);
            if (energy <= 0) {
                event.setCanceled(true);
            }
        }
    }

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
                                Logging.message(event.entityPlayer, "You cannot sleep in this dimension!");
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
        World world = event.world;
        int id = world.provider.dimensionId;
        DimensionInformation dimensionInformation = null;

        if (DimletConfiguration.preventSpawnUnpowered) {
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
            dimensionInformation = dimensionManager.getDimensionInformation(id);
            if (dimensionInformation != null) {
                // RFTools dimension.
                DimensionStorage storage = DimensionStorage.getDimensionStorage(world);
                int energy = storage.getEnergyLevel(id);
                if (energy <= 0) {
                    event.setResult(Event.Result.DENY);
                    Logging.logDebug("Dimension power low: Prevented a spawn of " + event.entity.getClass().getName());
               }
            }
        }

        if (event.entity instanceof IMob) {
            Coordinate coordinate = new Coordinate((int) event.entity.posX, (int) event.entity.posY, (int) event.entity.posZ);
            if (PeacefulAreaManager.isPeaceful(new GlobalCoordinate(coordinate, id))) {
                event.setResult(Event.Result.DENY);
                Logging.logDebug("Peaceful manager: Prevented a spawn of " + event.entity.getClass().getName());
            } else if (dimensionInformation != null && dimensionInformation.isPeaceful()) {
                // RFTools dimension.
                event.setResult(Event.Result.DENY);
                Logging.logDebug("Peaceful dimension: Prevented a spawn of " + event.entity.getClass().getName());
            }
        } else if (event.entity instanceof IAnimals) {
            if (dimensionInformation != null && dimensionInformation.isNoanimals()) {
                // RFTools dimension.
                event.setResult(Event.Result.DENY);
                Logging.logDebug("Noanimals dimension: Prevented a spawn of " + event.entity.getClass().getName());
            }
        }
    }

    @SubscribeEvent
    public void onLivingFallEvent(LivingFallEvent event) {
        if (event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entity;
            PlayerExtendedProperties playerExtendedProperties = PlayerExtendedProperties.getProperties(player);
            if (!player.worldObj.isRemote) {
                if (playerExtendedProperties.getBuffProperties().hasBuff(PlayerBuff.BUFF_FEATHERFALLING)) {
                    event.distance /= 2.0f;
                } else if (playerExtendedProperties.getBuffProperties().hasBuff(PlayerBuff.BUFF_FEATHERFALLINGPLUS)) {
                    event.distance /= 8.0f;
                }
            }
        }
    }

}
