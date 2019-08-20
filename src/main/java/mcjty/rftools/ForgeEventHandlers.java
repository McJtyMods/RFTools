package mcjty.rftools;

import mcjty.lib.api.smartwrench.SmartWrench;
import mcjty.lib.api.smartwrench.SmartWrenchMode;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.WrenchChecker;
import mcjty.rftools.blocks.blockprotector.BlockProtectorConfiguration;
import mcjty.rftools.blocks.blockprotector.BlockProtectors;
import mcjty.rftools.blocks.environmental.NoTeleportAreaManager;
import mcjty.rftools.blocks.environmental.PeacefulAreaManager;
import mcjty.rftools.blocks.screens.ScreenBlock;
import mcjty.rftools.blocks.screens.ScreenHitBlock;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.items.smartwrench.SmartWrenchItem;
import mcjty.rftools.playerprops.BuffProperties;
import mcjty.rftools.playerprops.FavoriteDestinationsProperties;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import mcjty.rftools.playerprops.PropertiesDispatcher;
import mcjty.rftools.shapes.ShapeDataManagerServer;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForgeEventHandlers {

    // Workaround for the charged porter so that the teleport can be done outside
    // of the entity tick loop
    private static List<Pair<TeleportDestination,PlayerEntity>> playersToTeleportHere = new ArrayList<>();

    public static void addPlayerToTeleportHere(TeleportDestination destination, PlayerEntity player) {
        playersToTeleportHere.add(Pair.of(destination, player));
    }

    private static void performDelayedTeleports() {
        if (!playersToTeleportHere.isEmpty()) {
            // Teleport players here
            for (Pair<TeleportDestination, PlayerEntity> pair : playersToTeleportHere) {
                TeleportationTools.performTeleport(pair.getRight(), pair.getLeft(), 0, 10, false);
            }
            playersToTeleportHere.clear();
        }
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.world.provider.getDimension() == 0) {
            performDelayedTeleports();
            ShapeDataManagerServer.handleWork();
        }
    }


    @SubscribeEvent
    public void onPlayerTickEvent(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START && !event.player.getEntityWorld().isRemote) {
            BuffProperties buffProperties = PlayerExtendedProperties.getBuffProperties(event.player);
            if (buffProperties != null) {
                buffProperties.tickBuffs((ServerPlayerEntity) event.player);
            }
        }
    }

    @SubscribeEvent
    public void onEntityConstructing(AttachCapabilitiesEvent<Entity> event){
        if (event.getObject() instanceof PlayerEntity) {
            if (!event.getObject().hasCapability(PlayerExtendedProperties.BUFF_CAPABILITY, null)) {
                event.addCapability(new ResourceLocation(RFTools.MODID, "Properties"), new PropertiesDispatcher());
            }
        }
    }


    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getWorld().isRemote) {
            return;
        }
        PlayerEntity player = event.getEntityPlayer();
        ItemStack heldItem = player.getHeldItemMainhand();
        if (heldItem.isEmpty() || !(heldItem.getItem() instanceof SmartWrench)) {
            double blockReachDistance = ((ServerPlayerEntity) player).interactionManager.getBlockReachDistance();
            RayTraceResult rayTrace = ForgeHooks.rayTraceEyes(player, blockReachDistance + 1);
            if (rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK) {
                Block block = event.getWorld().getBlockState(rayTrace.getBlockPos()).getBlock();
                if (block instanceof ScreenBlock) {
                    event.setCanceled(true);
                    return;
                } else if (block instanceof ScreenHitBlock) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        PlayerEntity player = event.getEntityPlayer();

        if (event instanceof PlayerInteractEvent.LeftClickBlock) {
            checkCreativeClick(event);
        } else if (event instanceof PlayerInteractEvent.RightClickBlock) {
            if (player.isSneaking()) {
                ItemStack heldItem = player.getHeldItemMainhand();
                if (heldItem.isEmpty() || !(heldItem.getItem() instanceof SmartWrench)) {
                    World world = event.getWorld();
                    BlockState state = world.getBlockState(event.getPos());
                    Block block = state.getBlock();
                    if (block instanceof ScreenBlock) {
                        Vec3d vec = ((PlayerInteractEvent.RightClickBlock) event).getHitVec();
                        ((ScreenBlock) block).activate(world, event.getPos(), state, player, event.getHand(), event.getFace(), (float) vec.x, (float) vec.y, (float) vec.z);
                        ((PlayerInteractEvent.RightClickBlock) event).setUseItem(Event.Result.DENY);
                        return;
                    } else if (block instanceof ScreenHitBlock) {
                        Vec3d vec = ((PlayerInteractEvent.RightClickBlock) event).getHitVec();
                        ((ScreenHitBlock) block).activate(world, event.getPos(), state, player, event.getHand(), event.getFace(), (float) vec.x, (float) vec.y, (float) vec.z);
                        ((PlayerInteractEvent.RightClickBlock) event).setUseItem(Event.Result.DENY);
                        return;
                    }
                }
            }
        }

        ItemStack heldItem = player.getHeldItem(event.getHand());
        if (heldItem.isEmpty() || heldItem.getItem() == null) {
            return;
        }
        if (BlockProtectorConfiguration.enabled.get() && player.isSneaking() && WrenchChecker.isAWrench(heldItem.getItem())) {
            // If the block is protected we prevent sneak-wrenching it.
            if (heldItem.getItem() instanceof SmartWrenchItem) {
                // But if it is a smart wrench in select mode we allow it
                if (SmartWrenchItem.getCurrentMode(heldItem) == SmartWrenchMode.MODE_SELECT) {
                    return;
                }
            }
            World world = event.getWorld();
            int x = event.getPos().getX();
            int y = event.getPos().getY();
            int z = event.getPos().getZ();
            Collection<GlobalCoordinate> protectors = BlockProtectors.getProtectors(world, x, y, z);
            if (BlockProtectors.checkHarvestProtection(x, y, z, world, protectors)) {
                event.setCanceled(true);
            }
        }

    }

    private void checkCreativeClick(PlayerInteractEvent event) {
        if (event.getEntityPlayer().isCreative()) {
            // In creative we don't want our screens to be destroyed by left click unless he/she is sneaking
            Block block = event.getWorld().getBlockState(event.getPos()).getBlock();
            if (block == ScreenSetup.screenBlock || block == ScreenSetup.creativeScreenBlock || block == ScreenSetup.screenHitBlock) {
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

    @SubscribeEvent
    public void onLivingFall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            BuffProperties buffProperties = PlayerExtendedProperties.getBuffProperties(player);
            if (buffProperties != null) {
                if (buffProperties.hasBuff(PlayerBuff.BUFF_FEATHERFALLING)) {
                    event.setDamageMultiplier(event.getDamageMultiplier() / 2);
                }
                if (buffProperties.hasBuff(PlayerBuff.BUFF_FEATHERFALLINGPLUS)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityTeleport(EnderTeleportEvent event) {
        World world = event.getEntity().getEntityWorld();
        int id = world.provider.getDimension();

        Entity entity = event.getEntity();
        BlockPos coordinate = new BlockPos((int) entity.posX, (int) entity.posY, (int) entity.posZ);
        if (NoTeleportAreaManager.isTeleportPrevented(entity, new GlobalCoordinate(coordinate, id))) {
            event.setCanceled(true);
        } else {
            coordinate = new BlockPos((int) event.getTargetX(), (int) event.getTargetY(), (int) event.getTargetZ());
            if (NoTeleportAreaManager.isTeleportPrevented(entity, new GlobalCoordinate(coordinate, id))) {
                event.setCanceled(true);
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

    @SubscribeEvent
    public void onPostWorldTick(TickEvent.WorldTickEvent event) {
        if (!event.world.isRemote) {
            TickOrderHandler.postWorldTick(event.world.provider.getDimension());
        }
    }
}
