package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DialingDeviceTileEntity extends GenericEnergyHandlerTileEntity {

    public static int MAXENERGY = 50000;
    public static int RECEIVEPERTICK = 100;

    public static final String CMD_TELEPORT = "tp";
    public static final String CMD_GETRECEIVERS = "getReceivers";
    public static final String CLIENTCMD_GETRECEIVERS = "getReceivers";
    public static final String CMD_DIAL = "dial";
    public static final String CLIENTCMD_DIAL = "dialResult";
    public static final String CMD_GETTRANSMITTERS = "getTransmitters";
    public static final String CLIENTCMD_GETTRANSMITTERS = "getTransmitters";
    public static final String CMD_CHECKSTATUS = "checkStatus";
    public static final String CLIENTCMD_STATUS = "status";
    public static final int DIAL_RECEIVER_BLOCKED_MASK = 0x1;       // One value for blocked or not on receiver side
    public static final int DIAL_TRANSMITTER_BLOCKED_MASK = 0x2;    // One value for blocked or not on transmitter side
    public static final int DIAL_INVALID_DESTINATION_MASK = 0x4;    // The destination is somehow invalid
    public static final int DIAL_DIALER_POWER_LOW_MASK = 0x8;       // The dialer itself is low on power
    public static final int DIAL_RECEIVER_POWER_LOW_MASK = 0x10;    // The receiver is low on power
    public static final int DIAL_TRANSMITTER_NOACCESS = 0x20;       // No access to transmitter
    public static final int DIAL_RECEIVER_NOACCESS = 0x40;          // No access to receiver
    public static final int DIAL_INTERRUPTED = 0x80;                // The dial was interrupted
    public static final int DIAL_OK = 0;                            // All is ok

    // For client.
    private List<TeleportDestination> receivers = null;
    private List<TransmitterInfo> transmitters = null;

    public DialingDeviceTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    /**
     * Calculate the distance (in string form) between a transmitter and receiver.
     * @param world
     * @param transmitterInfo
     * @param teleportDestination
     * @return the distance or else 'dimension warp' in case it is another dimension.
     */
    public static String calculateDistance(World world, TransmitterInfo transmitterInfo, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return "dimension warp";
        } else {
            Coordinate c1 = transmitterInfo.getCoordinate();
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            return Integer.toString((int) dist);
        }
    }

    public static boolean isDestinationAnalyzerAvailable(World world, int x, int y, int z) {
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x + 1, y, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x - 1, y, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y + 1, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y - 1, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y, z + 1))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y, z - 1))) {
            return true;
        }
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    public List<TeleportDestination> searchReceivers() {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        return new ArrayList<TeleportDestination>(destinations.getValidDestinations());
    }

    public void storeReceiversForClient(List<TeleportDestination> receivers) {
        this.receivers = new ArrayList<TeleportDestination>(receivers);
    }

    public List<TeleportDestination> getReceivers() {
        return receivers;
    }

    public List<TransmitterInfo> searchTransmitters() {
        int x = xCoord;
        int y = yCoord;
        int z = zCoord;

        int hrange = MatterTransmitterTileEntity.horizontalDialerRange;
        int vrange = MatterTransmitterTileEntity.verticalDialerRange;

        List<TransmitterInfo> transmitters = new ArrayList<TransmitterInfo>();
        for (int dy = -vrange ; dy <= vrange ; dy++) {
            int yy = y + dy;
            if (yy >= 0 && yy < worldObj.getActualHeight()) {
                for (int dz = -hrange ; dz <= hrange; dz++) {
                    int zz = z + dz;
                    for (int dx = -hrange ; dx <= hrange ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            Coordinate c = new Coordinate(xx, yy, zz);
                            TileEntity tileEntity = worldObj.getTileEntity(xx, yy, zz);
                            if (tileEntity != null) {
                                if (tileEntity instanceof MatterTransmitterTileEntity) {
                                    MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) tileEntity;
                                    transmitters.add(new TransmitterInfo(c, matterTransmitterTileEntity.getName(), matterTransmitterTileEntity.getTeleportDestination()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return transmitters;
    }

    public void storeTransmittersForClient(List<TransmitterInfo> transmitters) {
        this.transmitters = new ArrayList<TransmitterInfo>(transmitters);
    }

    public List<TransmitterInfo> getTransmitters() {
        return transmitters;
    }

    private void clearBeam(Coordinate c, World world) {
        Block b = world.getBlock(c.getX(), c.getY()+1, c.getZ());
        if (ModBlocks.teleportBeamBlock.equals(b)) {
            world.setBlockToAir(c.getX(), c.getY()+1, c.getZ());
        }
    }


    private boolean makeBeam(Coordinate c, World world, int dy1, int dy2, int errory) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = world.getBlock(c.getX(), c.getY()+dy, c.getZ());
            if ((!b.isAir(world, xCoord, yCoord+dy, zCoord)) && !ModBlocks.teleportBeamBlock.equals(b)) {
                if (dy <= errory) {
                    // Everything below errory must be free.
                    return false;
                } else {
                    // Everything higher then errory doesn't have to be free.
                    break;
                }
            }
        }
        Block b = world.getBlock(c.getX(), c.getY()+1, c.getZ());
        if (b.isAir(world, c.getX(), c.getY()+1, c.getZ()) || ModBlocks.teleportBeamBlock.equals(b)) {
            world.setBlock(c.getX(), c.getY()+1, c.getZ(), ModBlocks.teleportBeamBlock, 0, 2);
        }
        return true;
    }

    private int dial(String player, Coordinate transmitter, int transDim, Coordinate coordinate, int dimension) {
        World transWorld = DimensionManager.getProvider(transDim).worldObj;
        MatterTransmitterTileEntity transmitterTileEntity = (MatterTransmitterTileEntity) transWorld.getTileEntity(transmitter.getX(), transmitter.getY(), transmitter.getZ());

        if (!transmitterTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_NOACCESS;
        }

        if (coordinate == null) {
            clearBeam(transmitter, transWorld);
            transmitterTileEntity.setTeleportDestination(null);
            return DialingDeviceTileEntity.DIAL_INTERRUPTED;
        }

        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        TeleportDestination teleportDestination = destinations.getDestination(coordinate, dimension);
        if (teleportDestination == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        Coordinate c = teleportDestination.getCoordinate();
        World recWorld = DimensionManager.getProvider(teleportDestination.getDimension()).worldObj;
        TileEntity tileEntity = recWorld.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }
        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;
        if (!matterReceiverTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_NOACCESS;
        }

        int cost = MatterTransmitterTileEntity.rfPerDial;
        if (getEnergyStored(ForgeDirection.DOWN) < cost) {
            return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
        }

        if (!makeBeam(transmitter, transWorld, 1, 4, 2)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_BLOCKED_MASK;
        }

        extractEnergy(ForgeDirection.DOWN, cost, false);
        transmitterTileEntity.setTeleportDestination(teleportDestination);

        return DialingDeviceTileEntity.DIAL_OK;
    }

    private int checkStatus(Coordinate c, int dim) {
        World w = DimensionManager.getProvider(dim).worldObj;
        TileEntity tileEntity = w.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;

        int cost = MatterTransmitterTileEntity.rfPerCheck;
        if (getEnergyStored(ForgeDirection.DOWN) < cost) {
            return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
        }
        extractEnergy(ForgeDirection.DOWN, cost, false);

        return matterReceiverTileEntity.checkStatus();
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETRECEIVERS.equals(command)) {
            return searchReceivers();
        } else if (CMD_GETTRANSMITTERS.equals(command)) {
            return searchTransmitters();
        }
        return null;
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_CHECKSTATUS.equals(command)) {
            Coordinate c = args.get("c").getCoordinate();
            int dim = args.get("dim").getInteger();
            return checkStatus(c, dim);
        } else if (CMD_DIAL.equals(command)) {
            String player = args.get("player").getString();
            Coordinate transmitter = args.get("trans").getCoordinate();
            int transDim = args.get("transDim").getInteger();
            Coordinate c = args.get("c").getCoordinate();
            int dim = args.get("dim").getInteger();

            return dial(player, transmitter, transDim, c, dim);
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETRECEIVERS.equals(command)) {
            storeReceiversForClient((List<TeleportDestination>) list);
            return true;
        } else if (CLIENTCMD_GETTRANSMITTERS.equals(command)) {
            storeTransmittersForClient((List<TransmitterInfo>) list);
            return true;
        }
        return false;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_STATUS.equals(command)) {
            GuiDialingDevice.fromServer_receiverStatus = result;
            return true;
        } else if (CLIENTCMD_DIAL.equals(command)) {
            GuiDialingDevice.fromServer_dialResult = result;
            return true;
        }
        return false;
    }
}
