package mcjty.rftools;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.blockprotector.BlockProtectors;
import mcjty.rftools.blocks.environmental.NoTeleportAreaManager;
import mcjty.rftools.blocks.environmental.PeacefulAreaManager;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.playerprops.*;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ForgeEventHandlers {

    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.worldObj.isRemote) {
            PorterProperties porterProperties = PlayerExtendedProperties.getPorterProperties(event.player);
            if (porterProperties != null) {
                porterProperties.tickTeleport(event.player);
            }

            BuffProperties buffProperties = PlayerExtendedProperties.getBuffProperties(event.player);
            if (buffProperties != null) {
                buffProperties.tickBuffs((EntityPlayerMP) event.player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(AttachCapabilitiesEvent.Entity event){
        if (event.getEntity() instanceof EntityPlayer) {
            if (!event.getEntity().hasCapability(PlayerExtendedProperties.PORTER_CAPABILITY, null)) {
                event.addCapability(new ResourceLocation(RFTools.MODID, "Properties"), new PropertiesDispatcher());
            }
        }
    }

    private Collection<GlobalCoordinate> getProtectors(World world, int x, int y, int z) {
        Collection<GlobalCoordinate> protectors;
        BlockProtectors blockProtectors = BlockProtectors.getProtectors(world);
        if (blockProtectors == null) {
            protectors = Collections.emptyList();
        } else {
            int id = world.provider.getDimension();
            protectors = blockProtectors.findProtectors(x, y, z, id, 2);
        }
        return protectors;
    }


    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event instanceof PlayerInteractEvent.LeftClickBlock) {
            checkCreativeClick(event);
        }

        ItemStack heldItem = event.getEntityPlayer().getHeldItem(event.getHand());
        if (heldItem == null || heldItem.getItem() == null) {
            return;
        }
        if (event.getEntityPlayer().isSneaking() && WrenchChecker.isAWrench(heldItem.getItem())) {
            // If the block is protected we prevent sneak-wrenching it.
            World world = event.getWorld();
            int x = event.getPos().getX();
            int y = event.getPos().getY();
            int z = event.getPos().getZ();
            Collection<GlobalCoordinate> protectors = getProtectors(world, x, y, z);
            checkHarvestProtection(event, x, y, z, world, protectors);
        }

    }

    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        int x = event.getPos().getX();
        int y = event.getPos().getY();
        int z = event.getPos().getZ();
        World world = event.getWorld();

        Collection<GlobalCoordinate> protectors = getProtectors(world, x, y, z);
        checkHarvestProtection(event, x, y, z, world, protectors);
    }

    private void checkCreativeClick(PlayerInteractEvent event) {
        if (event.getEntityPlayer().isCreative()) {
            // In creative we don't want our screens to be destroyed by left click unless he/she is sneaking
            Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            if (block == ScreenSetup.screenBlock || block == ScreenSetup.screenHitBlock) {
                if (!event.getEntityPlayer().isSneaking()) {
                    // If not sneaking while we hit a screen we cancel the destroy. Otherwise we go through.

                    if (event.getWorld().isRemote) {
                        // simulate click because it isn't called in creativemode or when we cancel the event
                        block.onBlockClicked(event.getWorld(), event.getPos(), event.getEntityPlayer());
                    }

                    event.setCanceled(true);
                }
            }
        }
    }

    private void checkHarvestProtection(Event event, int x, int y, int z, World world, Collection<GlobalCoordinate> protectors) {
        for (GlobalCoordinate protector : protectors) {
            TileEntity te = world.getTileEntity(protector.getCoordinate());
            if (te instanceof BlockProtectorTileEntity) {
                BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                BlockPos relative = blockProtectorTileEntity.absoluteToRelative(x, y, z);
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
        Explosion explosion = event.getExplosion();
        Vec3d explosionVector = explosion.getPosition();
        Collection<GlobalCoordinate> protectors = getProtectors(event.getWorld(), (int) explosionVector.xCoord, (int) explosionVector.yCoord, (int) explosionVector.zCoord);

        if (protectors.isEmpty()) {
            return;
        }

        List<BlockPos> affectedBlocks = event.getAffectedBlocks();
        List<BlockPos> toremove = new ArrayList<>();

        int rf = 0;
        for (GlobalCoordinate protector : protectors) {
            BlockPos pos = protector.getCoordinate();
            TileEntity te = event.getWorld().getTileEntity(pos);
            if (te instanceof BlockProtectorTileEntity) {
                BlockProtectorTileEntity blockProtectorTileEntity = (BlockProtectorTileEntity) te;
                for (BlockPos block : affectedBlocks) {
                    BlockPos relative = blockProtectorTileEntity.absoluteToRelative(block);
                    boolean b = blockProtectorTileEntity.isProtected(relative);
                    if (b) {
                        Vec3d blockVector = new Vec3d(block);
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

        affectedBlocks.removeAll(toremove);

        Logging.logDebug("RF Needed for one explosion:" + rf);
    }

    @SubscribeEvent
    public void onEntityTeleport(EnderTeleportEvent event) {
        World world = event.getEntity().getEntityWorld();
        int id = world.provider.getDimension();

        Entity entity = event.getEntity();
        BlockPos coordinate = new BlockPos((int) entity.posX, (int) entity.posY, (int) entity.posZ);
        if (NoTeleportAreaManager.isTeleportPrevented(entity, new GlobalCoordinate(coordinate, id))) {
            event.setCanceled(true);
            Logging.logDebug("No Teleport manager: Prevented teleport of " + entity.getClass().getName());
        } else {
            coordinate = new BlockPos((int) event.getTargetX(), (int) event.getTargetY(), (int) event.getTargetZ());
            if (NoTeleportAreaManager.isTeleportPrevented(entity, new GlobalCoordinate(coordinate, id))) {
                event.setCanceled(true);
                Logging.logDebug("No Teleport manager: Prevented teleport of " + entity.getClass().getName());
            }
        }
    }


    @SubscribeEvent
    public void onEntitySpawnEvent(LivingSpawnEvent.CheckSpawn event) {
        World world = event.getWorld();
        int id = world.provider.getDimension();

        Entity entity = event.getEntity();
        if (entity instanceof IMob) {
            BlockPos coordinate = new BlockPos((int) entity.posX, (int) entity.posY, (int) entity.posZ);
            if (PeacefulAreaManager.isPeaceful(new GlobalCoordinate(coordinate, id))) {
                event.setResult(Event.Result.DENY);
                Logging.logDebug("Peaceful manager: Prevented a spawn of " + entity.getClass().getName());
            }
        }
    }

    @SubscribeEvent
    public void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            // We need to copyFrom the capabilities
            if (event.getOriginal().hasCapability(PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY, null)) {
                FavoriteDestinationsProperties oldFavorites = event.getOriginal().getCapability(PlayerExtendedProperties.FAVORITE_DESTINATIONS_CAPABILITY, null);
                FavoriteDestinationsProperties newFavorites = PlayerExtendedProperties.getFavoriteDestinations(event.getEntityPlayer());
                newFavorites.copyFrom(oldFavorites);
            }
        }
    }

}
