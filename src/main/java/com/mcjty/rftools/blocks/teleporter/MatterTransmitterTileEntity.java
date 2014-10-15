package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static int MAXENERGY = 100000;
    public static int RECEIVEPERTICK = 1000;

    public static final String CMD_SETNAME = "setName";

    public static int horizontalDialerRange = 10;           // Horizontal range the dialing device can check for transmitters
    public static int verticalDialerRange = 5;              // Vertical range the dialing device can check for transmitters

    public static int rfPerDial = 1000;                     // RF Consumed by dialing device when making a new dial
    public static int rfPerCheck = 5000;                    // RF Used to do a check on a receiver.
    public static int rfDialedConnectionPerTick = 10;       // RF Consumed by transmitter when a dial is active and not doing anything else

    // The following flags are used to calculate power usage for even starting a teleport. The rfStartTeleportBaseDim (cost of
    // teleporting to another dimension) is also the cap of the local teleport which is calculated by doing
    // rfStartTelelportBaseLocal + dist * rfStartTeleportDist
    public static int rfStartTeleportBaseLocal = 5000;      // Base RF consumed by transmitter when starting a teleport in same dimension
    public static int rfStartTeleportBaseDim = 100000;      // Base RF consumed by transmitter when starting a teleport to another dimension
    public static int rfStartTeleportDist = 10;             // RF per distance unit when starting a teleport
    public static int rfTeleportPerTick = 500;              // During the time the teleport is busy this RF is used per tick on the transmitter

    public static int rfPerTeleportReceiver = 5000;         // On the receiver side we need this amount of power

    // The following flags are used to calculate the time used for doing the actual teleportation. Same principle as with
    // the power usage above with regards to local/dimensional teleport.
    public static int timeTeleportBaseLocal = 5;
    public static int timeTeleportBaseDim = 100;
    public static int timeTeleportDist = 10;                // Value in militicks (1000 == 1 tick)

    // Server side: current dialing destination
    private TeleportDestination teleportDestination = null;

    private String name = null;

    // Server side: the player we're currently teleporting.
    private EntityPlayer teleportingPlayer = null;
    private int teleportTimer = 0;
    private int cooldownTimer = 0;
    private int totalTicks;
    private int goodTicks;
    private int badTicks;

    public MatterTransmitterTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
            return rfStartTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int rf = rfStartTeleportBaseLocal + (int)(rfStartTeleportDist * dist);
            if (rf > rfStartTeleportBaseDim) {
                rf = rfStartTeleportBaseDim;
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
            return timeTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int time = timeTeleportBaseLocal + (int)(timeTeleportDist * dist / 1000);
            if (time > timeTeleportBaseDim) {
                time = timeTeleportBaseDim;
            }
            return time;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        name = tagCompound.getString("tpName");
        Coordinate c = Coordinate.readFromNBT(tagCompound, "dest");
        if (c == null) {
            teleportDestination = null;
        } else {
            int dim = tagCompound.getInteger("dim");
            teleportDestination = new TeleportDestination(c, dim);
        }
        teleportTimer = tagCompound.getInteger("tpTimer");
        cooldownTimer = tagCompound.getInteger("cooldownTimer");
        totalTicks = tagCompound.getInteger("totalTicks");
        goodTicks = tagCompound.getInteger("goodTicks");
        badTicks = tagCompound.getInteger("badTicks");
        String playerName = tagCompound.getString("tpPlayer");
        if (playerName != null && !playerName.isEmpty()) {
            teleportingPlayer = worldObj.getPlayerEntityByName(playerName);
        } else {
            teleportingPlayer = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (name != null && !name.isEmpty()) {
            tagCompound.setString("tpName", name);
        }
        if (teleportDestination != null) {
            Coordinate c = teleportDestination.getCoordinate();
            if (c != null) {
                Coordinate.writeToNBT(tagCompound, "dest", c);
                tagCompound.setInteger("dim", teleportDestination.getDimension());
            }
        }
        tagCompound.setInteger("tpTimer", teleportTimer);
        tagCompound.setInteger("cooldownTimer", cooldownTimer);
        tagCompound.setInteger("totalTicks", totalTicks);
        tagCompound.setInteger("goodTicks", goodTicks);
        tagCompound.setInteger("badTicks", badTicks);
        if (teleportingPlayer != null) {
            tagCompound.setString("tpPlayer", teleportingPlayer.getDisplayName());
        }
    }


    public TeleportDestination getTeleportDestination() {
        return teleportDestination;
    }

    public void setTeleportDestination(TeleportDestination teleportDestination) {
        this.teleportDestination = teleportDestination;
    }

    /**
     * Return a number between 0 and 10 indicating the severity of the teleportation.
     * @return
     */
    private int calculateSeverity() {
        int severity = badTicks * 10 / totalTicks;
        if (mustInterrupt()) {
            // If an interrupt was done then severity is worse.
            severity += 2;
        }
        if (severity > 10) {
            severity = 10;
        }
        return severity;
    }

    private void applyBadEffectIfNeeded(int severity) {
        severity += calculateSeverity();
        if (severity > 10) {
            severity = 10;
        }
        switch (severity) {
            case 0:
                break;
            case 2:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 100));
                break;
            case 3:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 100));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 0.5f);
                break;
            case 4:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 200));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 0.5f);
                break;
            case 5:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 200));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 1.0f);
                break;
            case 6:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 300));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 1.0f);
                break;
            case 7:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 300));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 200));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 2.0f);
                break;
            case 8:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 400));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 300));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 2.0f);
                break;
            case 9:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 400));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 400));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 3.0f);
                break;
            case 10:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 500));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 500));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 3.0f);
                break;
        }
    }

    private void interruptWithBadEffect() {
        RFTools.warn(teleportingPlayer, "Power failure during transit!");
        applyBadEffectIfNeeded(0);
        cooldownTimer = 200;
        teleportingPlayer = null;
    }


    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        cooldownTimer--;
        if (cooldownTimer < 0) {
            cooldownTimer = 0;
        }

        if (teleportingPlayer != null) {
            if (teleportDestination == null) {
                RFTools.warn(teleportingPlayer, "The destination vanished! Aborting.");
                cooldownTimer = 80;
                teleportingPlayer = null;
                return;
            }

            AxisAlignedBB playerBB = teleportingPlayer.boundingBox;
            AxisAlignedBB beamBB = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+2, zCoord+1);
            if (!playerBB.intersectsWith(beamBB)) {
                RFTools.message(teleportingPlayer, "Teleportation was interrupted!");
                applyBadEffectIfNeeded(0);
                cooldownTimer = 80;
                teleportingPlayer = null;
                return;
            }

            if (getEnergyStored(ForgeDirection.DOWN) < rfTeleportPerTick) {
                // Not enough energy. This is a bad tick.
                badTicks++;
                if (mustInterrupt()) {
                    // Too many bad ticks. Total failure!
                    interruptWithBadEffect();
                }
                return;
            }

            // We have enough energy so this is a good tick.
            extractEnergy(ForgeDirection.DOWN, rfTeleportPerTick, false);
            goodTicks++;

            teleportTimer--;
            if (teleportTimer <= 0) {
                int currentId = teleportingPlayer.worldObj.provider.dimensionId;
                if (currentId != teleportDestination.getDimension()) {
                    MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) teleportingPlayer, teleportDestination.getDimension());
                }

                Coordinate c = teleportDestination.getCoordinate();
                RFTools.message(teleportingPlayer, "Whoosh!");
                teleportingPlayer.setPositionAndUpdate(c.getX(), c.getY()-2, c.getZ());
                int severity = consumeReceiverEnergy(c, teleportDestination.getDimension());
                applyBadEffectIfNeeded(severity);
                teleportingPlayer = null;
            }
        }
    }

    /**
     * Consume energy on the receiving side and return a number indicating how good this went.
     *
     * @param c
     * @param dimension
     * @return 0 in case of success. 10 in case of severe failure
     */
    private int consumeReceiverEnergy(Coordinate c, int dimension) {
        World world = DimensionManager.getWorld(dimension);
        TileEntity te = world.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(te instanceof MatterReceiverTileEntity)) {
            RFTools.warn(teleportingPlayer, "Something went wrong with the destination!");
            return 0;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
        int extracted = matterReceiverTileEntity.extractEnergy(ForgeDirection.DOWN, rfPerTeleportReceiver, false);
        return 10 - (extracted * 10 / rfPerTeleportReceiver);
    }

    private boolean mustInterrupt() {
        return badTicks > (totalTicks / 2);
    }

    public void startTeleportation(Entity entity) {
        if (cooldownTimer > 0) {
            // In cooldown. We can't do teleport right now.
            return;
        }
        if (teleportingPlayer != null) {
            // Already teleporting
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;

        if (teleportDestination != null) {
            Coordinate cthis = new Coordinate(xCoord, yCoord, zCoord);
            int cost = calculateRFCost(worldObj, cthis, teleportDestination);
            if (getEnergyStored(ForgeDirection.DOWN) < cost) {
                RFTools.warn(player, "Not enough power to start the teleport!");
                cooldownTimer = 80;
                return;
            }
            extractEnergy(ForgeDirection.DOWN, cost, false);

            RFTools.message(player, "Start teleportation...");
            teleportingPlayer = player;
            teleportTimer = calculateTime(worldObj, cthis, teleportDestination);
            totalTicks = teleportTimer;
            goodTicks = 0;
            badTicks = 0;
        } else {
            RFTools.warn(player, "Something is wrong with the destination!");
        }
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        }
        return false;
    }
}
