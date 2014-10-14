package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Map;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 1000000;
    public static final int RECEIVEPERTICK = 1000;

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
     * @param transmitterInfo
     * @param teleportDestination
     * @return
     */
    public static int calculateRFCost(World world, TransmitterInfo transmitterInfo, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return rfStartTeleportBaseDim;
        } else {
            Coordinate c1 = transmitterInfo.getCoordinate();
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int rf = rfStartTeleportBaseLocal + (int)(rfStartTeleportDist * dist);
            if (rf > rfStartTeleportBaseDim) {
                rf = rfStartTeleportBaseDim;
            }
            return rf;
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

    @Override
    protected void checkStateServer() {
        super.checkStateServer();
        if (teleportingPlayer != null) {
            if (teleportDestination == null) {
                teleportingPlayer.addChatComponentMessage(new ChatComponentText("The destination vanished! Aborting."));
                teleportingPlayer = null;
                return;
            }

            AxisAlignedBB playerBB = teleportingPlayer.boundingBox;
            AxisAlignedBB beamBB = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+2, zCoord+1);
            if (!playerBB.intersectsWith(beamBB)) {
                teleportingPlayer.addChatComponentMessage(new ChatComponentText("Teleportation was interrupted!"));
                teleportingPlayer = null;
                return;
            }

            teleportTimer--;
            if (teleportTimer <= 0) {
                int currentId = teleportingPlayer.worldObj.provider.dimensionId;
                if (currentId != teleportDestination.getDimension()) {
                    MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) teleportingPlayer, teleportDestination.getDimension());
                }
                teleportingPlayer.addChatComponentMessage(new ChatComponentText("Whoosh!"));
                Coordinate c = teleportDestination.getCoordinate();
                teleportingPlayer.setPositionAndUpdate(c.getX(), c.getY()-2, c.getZ());
                teleportingPlayer = null;
            }
        }
    }

    public void startTeleportation(Entity entity) {
        if (teleportingPlayer != null) {
            // Already teleporting
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;

        if (teleportDestination != null) {
            player.addChatComponentMessage(new ChatComponentText("Start teleportation..."));
            teleportingPlayer = player;
            teleportTimer = 40;
        } else {
            player.addChatComponentMessage(new ChatComponentText("Something is wrong with the destination!"));
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
