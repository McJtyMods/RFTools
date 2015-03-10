package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.Achievements;
import com.mcjty.rftools.RFTools;
import com.mcjty.varia.Coordinate;
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

        int currentId = player.worldObj.provider.dimensionId;
        if (currentId != dest.getDimension()) {
            WorldServer worldServerForDimension = MinecraftServer.getServer().worldServerForDimension(dest.getDimension());
            MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) player, dest.getDimension(),
                    new RfToolsTeleporter(worldServerForDimension, c.getX()+0.5, c.getY()+1.5, c.getZ()+0.5));
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

        if (!applyBadEffectIfNeeded(player, severity, bad, good, boostNeeded)) {
            if (TeleportConfiguration.teleportVolume >= 0.01) {
                ((EntityPlayerMP) player).worldObj.playSoundAtEntity(player, RFTools.MODID + ":teleport_whoosh", TeleportConfiguration.teleportVolume, 1.0f);
            }
        }
        return boostNeeded;
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

        int extracted = matterReceiverTileEntity.extractEnergy(ForgeDirection.DOWN, rf, false);
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

    public static boolean applyBadEffectIfNeeded(EntityPlayer player, int severity, int bad, int total, boolean boostNeeded) {
        severity += calculateSeverity(bad, total);
        if (severity > 10) {
            severity = 10;
        }
        if (severity <= 0) {
            return false;
        }

        if (TeleportConfiguration.teleportErrorVolume >= 0.01) {
            player.worldObj.playSoundAtEntity(player, RFTools.MODID + ":teleport_error", TeleportConfiguration.teleportErrorVolume, 1.0f);
        }

        applyEffectForSeverity(player, severity, boostNeeded);
        return true;
    }

    public static boolean mustInterrupt(int bad, int total) {
        return bad > (total / 2);
    }
}
