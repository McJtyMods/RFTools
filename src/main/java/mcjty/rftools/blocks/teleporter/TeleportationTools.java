package mcjty.rftools.blocks.teleporter;

import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.SoundTools;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.ModSounds;
import mcjty.rftools.blocks.environmental.NoTeleportAreaManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.registries.ForgeRegistries;

public class TeleportationTools {
    public static final int STATUS_OK = 0;
    public static final int STATUS_WARN = 1;
    public static final int STATUS_UNKNOWN = 2;

    public static Potion confusion;
    public static Potion harm;
    public static Potion wither;

    public static void getPotions() {
        if (confusion == null) {
            confusion = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation("nausea"));
            harm = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation("instant_damage"));
            wither = ForgeRegistries.POTION_TYPES.getValue(new ResourceLocation("wither"));
        }
    }

    public static void applyEffectForSeverity(PlayerEntity player, int severity, boolean boostNeeded) {
        getPotions();
        switch (severity) {
            case 1:
                if (boostNeeded) {
                    // @todo 1.14
//                    player.addPotionEffect(new Effect(confusion, 100));
//                    player.addPotionEffect(new Effect(harm, 5));
                }
                break;
            case 2:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 100));
                break;
            case 3:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 100));
                player.attackEntityFrom(DamageSource.GENERIC, 0.5f);
                break;
            case 4:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 200));
                player.attackEntityFrom(DamageSource.GENERIC, 0.5f);
                break;
            case 5:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 200));
                player.attackEntityFrom(DamageSource.GENERIC, 1.0f);
                break;
            case 6:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 300));
                player.attackEntityFrom(DamageSource.GENERIC, 1.0f);
                break;
            case 7:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 300));
//                player.addPotionEffect(new Effect(wither, 200));
                player.attackEntityFrom(DamageSource.GENERIC, 2.0f);
                break;
            case 8:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 400));
//                player.addPotionEffect(new Effect(wither, 300));
                player.attackEntityFrom(DamageSource.GENERIC, 2.0f);
                break;
            case 9:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 400));
//                player.addPotionEffect(new Effect(wither, 400));
                player.attackEntityFrom(DamageSource.GENERIC, 3.0f);
                break;
            case 10:
                // @todo 1.14
//                player.addPotionEffect(new Effect(harm, 500));
//                player.addPotionEffect(new Effect(wither, 500));
                player.attackEntityFrom(DamageSource.GENERIC, 3.0f);
                break;
        }
    }

    /**
     * Calculate the cost of doing a dial between a transmitter and a destination.
     * @param world
     * @param c1 the start coordinate
     * @param teleportDestination
     * @return
     */
    public static int calculateRFCost(World world, BlockPos c1, TeleportDestination teleportDestination) {
        if (world.getDimension().getType().getId() != teleportDestination.getDimension()) {
            return TeleportConfiguration.rfStartTeleportBaseDim.get();
        } else {
            BlockPos c2 = teleportDestination.getCoordinate();
            double dist = new Vec3d(c1.getX(), c1.getY(), c1.getZ()).distanceTo(new Vec3d(c2.getX(), c2.getY(), c2.getZ()));
            int rf = TeleportConfiguration.rfStartTeleportBaseLocal.get() + (int)(TeleportConfiguration.rfStartTeleportDist.get() * dist);
            if (rf > TeleportConfiguration.rfStartTeleportBaseDim.get()) {
                rf = TeleportConfiguration.rfStartTeleportBaseDim.get();
            }
            return rf;
        }
    }

    /**
     * Calculate the time in ticks of doing a dial between a transmitter and a destination.
     * @param world
     * @param c1 the start coordinate
     * @param teleportDestination
     * @return
     */
    public static int calculateTime(World world, BlockPos c1, TeleportDestination teleportDestination) {
        if (world.getDimension().getType().getId() != teleportDestination.getDimension()) {
            return TeleportConfiguration.timeTeleportBaseDim.get();
        } else {
            BlockPos c2 = teleportDestination.getCoordinate();
            double dist = new Vec3d(c1.getX(), c1.getY(), c1.getZ()).distanceTo(new Vec3d(c2.getX(), c2.getY(), c2.getZ()));
            int time = TeleportConfiguration.timeTeleportBaseLocal.get() + (int)(TeleportConfiguration.timeTeleportDist.get() * dist / 1000);
            if (time > TeleportConfiguration.timeTeleportBaseDim.get()) {
                time = TeleportConfiguration.timeTeleportBaseDim.get();
            }
            return time;
        }
    }

    // Return true if we needed a boost.
    public static boolean performTeleport(PlayerEntity player, TeleportDestination dest, int bad, int good, boolean boosted) {
        BlockPos c = dest.getCoordinate();

        BlockPos old = new BlockPos((int)player.posX, (int)player.posY, (int)player.posZ);
        int oldId = player.getEntityWorld().getDimension().getType().getId();

        if (!TeleportationTools.allowTeleport(player, oldId, old, dest.getDimension(), dest.getCoordinate())) {
            return false;
        }

        if (oldId != dest.getDimension()) {
            mcjty.lib.varia.TeleportationTools.teleportToDimension(player, dest.getDimension(), c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5);
        } else {
            player.setPositionAndUpdate(c.getX()+0.5, c.getY()+1, c.getZ()+0.5);
        }

        if (TeleportConfiguration.whooshMessage.get()) {
            Logging.message(player, "Whoosh!");
        }
        // @todo achievements
//        Achievements.trigger(player, Achievements.firstTeleport);

        boolean boostNeeded = false;
        int severity = consumeReceiverEnergy(player, dest.getCoordinate(), dest.getDimension());
        if (severity > 0 && boosted) {
            boostNeeded = true;
            severity = 1;
        }

        severity = applyBadEffectIfNeeded(player, severity, bad, good, boostNeeded);
        if (severity <= 0) {
            if (TeleportConfiguration.teleportVolume.get() >= 0.01) {
                SoundTools.playSound(player.getEntityWorld(), ModSounds.whoosh, player.posX, player.posY, player.posZ, TeleportConfiguration.teleportVolume.get(), 1.0f);
            }
        }
        if (TeleportConfiguration.logTeleportUsages.get()) {
            Logging.log("Teleport: Player " + player.getName() + " from " + old + " (dim " + oldId + ") to " + dest.getCoordinate() + " (dim " + dest.getDimension() + ") with severity " + severity);
        }
        return boostNeeded;
    }

    // Server side only
    public static int dial(World worldObj, DialingDeviceTileEntity dialingDeviceTileEntity, String player, BlockPos transmitter, int transDim, BlockPos coordinate, int dimension, boolean once) {
        World transWorld = mcjty.lib.varia.TeleportationTools.getWorldForDimension(transDim);
        if (transWorld == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_SOURCE_MASK;
        }
        MatterTransmitterTileEntity transmitterTileEntity = (MatterTransmitterTileEntity) transWorld.getTileEntity(transmitter);
        if (transmitterTileEntity == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_TRANSMITTER;
        }

        if (player != null && !transmitterTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_NOACCESS;
        }

        if (coordinate == null) {
            transmitterTileEntity.setTeleportDestination(null, false);
            return DialingDeviceTileEntity.DIAL_INTERRUPTED;
        }

        TeleportDestination teleportDestination = findDestination(worldObj, coordinate, dimension);
        if (teleportDestination == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        BlockPos c = teleportDestination.getCoordinate();
        World recWorld = mcjty.lib.varia.TeleportationTools.getWorldForDimension(teleportDestination.getDimension());
        if (recWorld == null) {
            recWorld = WorldTools.getWorld(teleportDestination.getDimension());
            if (recWorld == null) {
                return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
            }
        }

        // Only do this if not an rftools dimension.
        TileEntity tileEntity = recWorld.getTileEntity(c);
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }
        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;
        matterReceiverTileEntity.updateDestination();       // Make sure destination is ok.
        if (player != null && !matterReceiverTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_NOACCESS;
        }

        if (!checkBeam(transmitter, transWorld, 1, 4, 2)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_BLOCKED_MASK;
        }

        if (dialingDeviceTileEntity != null) {
            if (!dialingDeviceTileEntity.getCapability(CapabilityEnergy.ENERGY).map(h -> {
                int cost = TeleportConfiguration.rfPerDial.get();
                cost = (int) (cost * (2.0f - dialingDeviceTileEntity.getInfusedFactor()) / 2.0f);

                if (h.getEnergyStored() < cost) {
                    return false;
                }
                ((GenericEnergyStorage)h).consumeEnergy(cost);
                return true;
            }).orElse(false)) {
                return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
            }
        }

        transmitterTileEntity.setTeleportDestination(teleportDestination, once);

        return DialingDeviceTileEntity.DIAL_OK;
    }


    /**
     * Consume energy on the receiving side and return a number indicating how good this went.
     *
     * @param c
     * @param dimension
     * @return 0 in case of success. 10 in case of severe failure
     */
    private static int consumeReceiverEnergy(PlayerEntity player, BlockPos c, int dimension) {
        World world = WorldTools.getWorld(dimension);
        TileEntity te = world.getTileEntity(c);
        if (!(te instanceof MatterReceiverTileEntity)) {
            Logging.warn(player, "Something went wrong with the destination!");
            return 0;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;

        return matterReceiverTileEntity.getCapability(CapabilityEnergy.ENERGY).map(h -> {
            int rf = TeleportConfiguration.rfPerTeleportReceiver.get();
            rf = (int) (rf * (2.0f - matterReceiverTileEntity.getInfusedFactor()) / 2.0f);

            if (rf <= 0) {
                return 0;
            }
            int extracted = Math.min(rf, h.getEnergyStored());
            ((GenericEnergyStorage)h).consumeEnergy(rf);

            long remainingRf = ((GenericEnergyStorage) h).getEnergy();
            if (remainingRf <= 1) {
                Logging.warn(player, "The matter receiver has run out of power!");
            } else if (remainingRf < (TeleportConfiguration.RECEIVER_MAXENERGY.get() / 10)) {
                Logging.warn(player, "The matter receiver is getting very low on power!");
            } else if (remainingRf < (TeleportConfiguration.RECEIVER_MAXENERGY.get() / 5)) {
                Logging.warn(player, "The matter receiver is getting low on power!");
            }

            return 10 - (extracted * 10 / rf);
        }).orElse(0);
    }

    /**
     * Return a number between 0 and 10 indicating the severity of the teleportation.
     * @return
     */
    public static int calculateSeverity(int bad, int total) {
        if (total == 0) {
            total = 1;
        }
        int severity = bad * 10 / total;
        if (mustInterrupt(bad, total)) {
            // If an interrupt was done then severity is worse.
            severity += 2;
        }
        if (severity > 10) {
            severity = 10;
        }
        return severity;
    }

    public static int applyBadEffectIfNeeded(PlayerEntity player, int severity, int bad, int total, boolean boostNeeded) {
        if (player == null) {
            return 0;
        }
        severity += calculateSeverity(bad, total);
        if (severity > 10) {
            severity = 10;
        }
        if (severity <= 0) {
            return 0;
        }

        if (TeleportConfiguration.teleportErrorVolume.get() >= 0.01) {
            SoundTools.playSound(player.getEntityWorld(), ModSounds.error, player.posX, player.posY, player.posZ, TeleportConfiguration.teleportErrorVolume.get(), 1.0f);
        }

        applyEffectForSeverity(player, severity, boostNeeded);
        return severity;
    }

    public static boolean mustInterrupt(int bad, int total) {
        return bad > (total / 2);
    }

    public static boolean allowTeleport(Entity entity, int sourceDim, BlockPos source, int destDim, BlockPos dest) {
        if (NoTeleportAreaManager.isTeleportPrevented(entity, new GlobalCoordinate(source, sourceDim))) {
            return false;
        }
        if (NoTeleportAreaManager.isTeleportPrevented(entity, new GlobalCoordinate(dest, destDim))) {
            return false;
        }
        return true;
    }

    public static TeleportDestination findDestination(World worldObj, BlockPos coordinate, int dimension) {
        TeleportDestinations destinations = TeleportDestinations.get();
        return destinations.getDestination(coordinate, dimension);
    }

    // Check if there is room for a beam.
    public static boolean checkBeam(BlockPos c, World world, int dy1, int dy2, int errory) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            BlockPos pos = new BlockPos(c.getX(), c.getY() + dy, c.getZ());
            BlockState state = world.getBlockState(pos);
            Block b = state.getBlock();
            if (!b.isAir(state, world, pos)) {
                if (dy <= errory) {
                    // Everything below errory must be free.
                    return false;
                } else {
                    // Everything higher then errory doesn't have to be free.
                    break;
                }
            }
        }
        return true;
    }

    public static boolean checkValidTeleport(PlayerEntity player, int srcId, int dstId) {
        if (TeleportConfiguration.preventInterdimensionalTeleports.get()) {
            if (srcId == dstId) {
                Logging.warn(player, "Teleportation in the same dimension is not allowed!");
                return false;
            }
        }
        if (TeleportConfiguration.getBlacklistedTeleportationDestinations().contains(dstId)) {
            Logging.warn(player, "Teleportation to that dimension is not allowed!");
            return false;
        }
        if (TeleportConfiguration.getBlacklistedTeleportationSources().contains(srcId)) {
            Logging.warn(player, "Teleportation from this dimension is not allowed!");
            return false;
        }
        return true;
    }
}
