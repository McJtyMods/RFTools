package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

public class TeleportationTools {
    public static final int STATUS_OK = 0;
    public static final int STATUS_WARN = 1;
    public static final int STATUS_UNKNOWN = 2;

    public static void applyEffectForSeverity(EntityPlayer player, int severity, boolean boostNeeded) {
        switch (severity) {
            case 1:
                if (boostNeeded) {
                    player.addPotionEffect(new PotionEffect(Potion.confusion.getId(), 100));
                    player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 5));
                }
                break;
            case 2:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 100));
                break;
            case 3:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 100));
                player.attackEntityFrom(DamageSource.generic, 0.5f);
                break;
            case 4:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 200));
                player.attackEntityFrom(DamageSource.generic, 0.5f);
                break;
            case 5:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 200));
                player.attackEntityFrom(DamageSource.generic, 1.0f);
                break;
            case 6:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 300));
                player.attackEntityFrom(DamageSource.generic, 1.0f);
                break;
            case 7:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 300));
                player.addPotionEffect(new PotionEffect(Potion.wither.getId(), 200));
                player.attackEntityFrom(DamageSource.generic, 2.0f);
                break;
            case 8:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 400));
                player.addPotionEffect(new PotionEffect(Potion.wither.getId(), 300));
                player.attackEntityFrom(DamageSource.generic, 2.0f);
                break;
            case 9:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 400));
                player.addPotionEffect(new PotionEffect(Potion.wither.getId(), 400));
                player.attackEntityFrom(DamageSource.generic, 3.0f);
                break;
            case 10:
                player.addPotionEffect(new PotionEffect(Potion.harm.getId(), 500));
                player.addPotionEffect(new PotionEffect(Potion.wither.getId(), 500));
                player.attackEntityFrom(DamageSource.generic, 3.0f);
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
    public static int calculateRFCost(World world, Coordinate c1, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return TeleportConfiguration.rfStartTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int rf = TeleportConfiguration.rfStartTeleportBaseLocal + (int)(TeleportConfiguration.rfStartTeleportDist * dist);
            if (rf > TeleportConfiguration.rfStartTeleportBaseDim) {
                rf = TeleportConfiguration.rfStartTeleportBaseDim;
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
    public static int calculateTime(World world, Coordinate c1, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return TeleportConfiguration.timeTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int time = TeleportConfiguration.timeTeleportBaseLocal + (int)(TeleportConfiguration.timeTeleportDist * dist / 1000);
            if (time > TeleportConfiguration.timeTeleportBaseDim) {
                time = TeleportConfiguration.timeTeleportBaseDim;
            }
            return time;
        }
    }

    // Return true if we needed a boost.
    public static boolean performTeleport(EntityPlayer player, TeleportDestination dest, int bad, int good, boolean boosted) {
        Coordinate c = dest.getCoordinate();

        Coordinate old = new Coordinate((int)player.posX, (int)player.posY, (int)player.posZ);
        int oldId = player.worldObj.provider.dimensionId;
        if (oldId != dest.getDimension()) {
            TeleportationTools.teleportToDimension(player, dest.getDimension(), c.getX() + 0.5, c.getY() + 1.5, c.getZ() + 0.5);
        } else {
            player.setPositionAndUpdate(c.getX()+0.5, c.getY()+1, c.getZ()+0.5);
        }

        RFTools.message(player, "Whoosh!");
        Achievements.trigger(player, Achievements.firstTeleport);

        boolean boostNeeded = false;
        int severity = consumeReceiverEnergy(player, dest.getCoordinate(), dest.getDimension());
        if (severity > 0 && boosted) {
            boostNeeded = true;
            severity = 1;
        }

        severity = applyBadEffectIfNeeded(player, severity, bad, good, boostNeeded);
        if (severity > 0) {
            if (TeleportConfiguration.teleportVolume >= 0.01) {
                ((EntityPlayerMP) player).worldObj.playSoundAtEntity(player, RFTools.MODID + ":teleport_whoosh", TeleportConfiguration.teleportVolume, 1.0f);
            }
        }
        if (TeleportConfiguration.logTeleportUsages) {
            RFTools.log("Teleport: Player " + player.getDisplayName() + " from " + old + " (dim " + oldId + ") to " + dest.getCoordinate() + " (dim " + dest.getDimension() + ") with severity " + severity);
        }
        return boostNeeded;
    }

    // Server side only
    public static int dial(World worldObj, DialingDeviceTileEntity dialingDeviceTileEntity, String player, Coordinate transmitter, int transDim, Coordinate coordinate, int dimension, boolean once) {
        World transWorld = RfToolsDimensionManager.getDimensionManager(worldObj).getWorldForDimension(transDim);
        if (transWorld == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_SOURCE_MASK;
        }
        MatterTransmitterTileEntity transmitterTileEntity = (MatterTransmitterTileEntity) transWorld.getTileEntity(transmitter.getX(), transmitter.getY(), transmitter.getZ());

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

        Coordinate c = teleportDestination.getCoordinate();
        World recWorld = RfToolsDimensionManager.getWorldForDimension(teleportDestination.getDimension());
        if (recWorld == null) {
            recWorld = MinecraftServer.getServer().worldServerForDimension(teleportDestination.getDimension());
            if (recWorld == null) {
                return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
            }
        }

        // Only do this if not an rftools dimension.
        TileEntity tileEntity = recWorld.getTileEntity(c.getX(), c.getY(), c.getZ());
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
            int cost = TeleportConfiguration.rfPerDial;
            cost = (int) (cost * (2.0f - dialingDeviceTileEntity.getInfusedFactor()) / 2.0f);

            if (dialingDeviceTileEntity.getEnergyStored(ForgeDirection.DOWN) < cost) {
                return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
            }

            dialingDeviceTileEntity.consumeEnergy(cost);
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
    private static int consumeReceiverEnergy(EntityPlayer player, Coordinate c, int dimension) {
        World world = DimensionManager.getWorld(dimension);
        TileEntity te = world.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(te instanceof MatterReceiverTileEntity)) {
            RFTools.warn(player, "Something went wrong with the destination!");
            return 0;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
        int rf = TeleportConfiguration.rfPerTeleportReceiver;
        rf = (int) (rf * (2.0f - matterReceiverTileEntity.getInfusedFactor()) / 2.0f);

        int extracted = rf;
        if (rf > matterReceiverTileEntity.getEnergyStored(ForgeDirection.DOWN)) {
            extracted = matterReceiverTileEntity.getEnergyStored(ForgeDirection.DOWN);
        }
        matterReceiverTileEntity.consumeEnergy(rf);

        int remainingRf = matterReceiverTileEntity.getEnergyStored(ForgeDirection.DOWN);
        if (remainingRf <= 1) {
            RFTools.warn(player, "The matter receiver has run out of power!");
        } else if (remainingRf < (TeleportConfiguration.RECEIVER_MAXENERGY / 10)) {
            RFTools.warn(player, "The matter receiver is getting very low on power!");
        } else if (remainingRf < (TeleportConfiguration.RECEIVER_MAXENERGY / 5)) {
            RFTools.warn(player, "The matter receiver is getting low on power!");
        }

        return 10 - (extracted * 10 / rf);
    }

    /**
     * Return a number between 0 and 10 indicating the severity of the teleportation.
     * @return
     */
    public static int calculateSeverity(int bad, int total) {
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

    public static int applyBadEffectIfNeeded(EntityPlayer player, int severity, int bad, int total, boolean boostNeeded) {
        severity += calculateSeverity(bad, total);
        if (severity > 10) {
            severity = 10;
        }
        if (severity <= 0) {
            return 0;
        }

        if (TeleportConfiguration.teleportErrorVolume >= 0.01) {
            player.worldObj.playSoundAtEntity(player, RFTools.MODID + ":teleport_error", TeleportConfiguration.teleportErrorVolume, 1.0f);
        }

        applyEffectForSeverity(player, severity, boostNeeded);
        return severity;
    }

    public static boolean mustInterrupt(int bad, int total) {
        return bad > (total / 2);
    }

    public static void teleportToDimension(EntityPlayer player, int dimension, double x, double y, double z) {
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP) player;
        WorldServer worldServer = MinecraftServer.getServer().worldServerForDimension(dimension);
        player.addExperienceLevel(0);
        MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension(entityPlayerMP, dimension,
                new RfToolsTeleporter(worldServer, x, y, z));
    }

    public static TeleportDestination findDestination(World worldObj, Coordinate coordinate, int dimension) {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        return destinations.getDestination(coordinate, dimension);
    }

    // Check if there is room for a beam.
    public static boolean checkBeam(Coordinate c, World world, int dy1, int dy2, int errory) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = world.getBlock(c.getX(), c.getY()+dy, c.getZ());
            if (!b.isAir(world, c.getX(), c.getY()+dy, c.getZ())) {
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
}
