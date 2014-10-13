package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DialingDeviceTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 50000;
    public static final int RECEIVEPERTICK = 100;

    public static final String CMD_TELEPORT = "tp";
    public static final String CMD_GETRECEIVERS = "getReceivers";
    public static final String CLIENTCMD_GETRECEIVERS = "getReceivers";
    public static final String CMD_GETTRANSMITTERS = "getTransmitters";
    public static final String CLIENTCMD_GETTRANSMITTERS = "getTransmitters";
    public static final String CMD_CHECKSTATUS = "checkStatus";
    public static final String CLIENTCMD_STATUS = "status";
    public static final int DIAL_RECEIVER_BLOCKED_MASK = 0x1;       // One value for blocked or not on receiver side
    public static final int DIAL_TRANSMITTER_BLOCKED_MASK = 0x2;    // One value for blocked or not on transmitter side
    public static final int DIAL_INVALID_DESTINATION_MASK = 0x4;    // The destination is somehow invalid
    public static final int DIAL_POWER_LOW_MASK = 0x8;              // The transmitter itself is low on power
    public static final int DIAL_ENERGY_MASK = 0x0f0;               // Sixteen energy levels for receiver (15 = sufficient, 0 = completely empty)
    public static final int DIAL_MATTER_MASK = 0xf00;               // Sixteen matter levels for receiver (16 = sufficient, 0 = completely empty)
    public static final int DIAL_OK = 0;                            // All is ok

    // For client.
    private List<TeleportDestination> receivers = null;
    private List<TransmitterInfo> transmitters = null;
    private int receiverStatus = -1;

    public DialingDeviceTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
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
        return destinations.getValidDestinations("");
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

    public int getReceiverStatus() {
        return receiverStatus;
    }

    public void setReceiverStatus(int receiverStatus) {
        this.receiverStatus = receiverStatus;
    }

    private int checkStatus(Coordinate c, int dim) {
        MatterReceiverTileEntity matterReceiverTileEntity;
        try {
            World w = DimensionManager.getProvider(dim).worldObj;
            matterReceiverTileEntity = (MatterReceiverTileEntity) w.getTileEntity(c.getX(), c.getY(), c.getZ());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

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
            setReceiverStatus(result);
            return true;
        }
        return false;
    }
}
