package mcjty.rftools.blocks.teleporter;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SimpleDialerTileEntity extends LogicTileEntity {

    private GlobalCoordinate transmitter;
    private Integer receiver;
    private boolean onceMode = false;

    private boolean prevIn = false;

    public SimpleDialerTileEntity() {
    }

    public void update() {
        if (transmitter == null) {
            return;
        }

        if ((powerLevel > 0) == prevIn) {
            return;
        }

        prevIn = powerLevel > 0;
        markDirty();

        if (powerLevel > 0) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());
            BlockPos coordinate = null;
            int dim = 0;
            if (receiver != null) {
                GlobalCoordinate gc = destinations.getCoordinateForId(receiver);
                if (gc != null) {
                    coordinate = gc.getCoordinate();
                    dim = gc.getDimension();
                }
            }

            int dial = TeleportationTools.dial(getWorld(), null, null, transmitter.getCoordinate(), transmitter.getDimension(), coordinate, dim, onceMode);
            if (dial != DialingDeviceTileEntity.DIAL_OK) {
                // @todo some way to report error
            }
        }
    }

    public boolean isOnceMode() {
        return onceMode;
    }

    public void setOnceMode(boolean onceMode) {
        this.onceMode = onceMode;
        markDirtyClient();
    }

    public GlobalCoordinate getTransmitter() {
        return transmitter;
    }

    public Integer getReceiver() {
        return receiver;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("transX")) {
            transmitter = new GlobalCoordinate(new BlockPos(tagCompound.getInteger("transX"), tagCompound.getInteger("transY"), tagCompound.getInteger("transZ")), tagCompound.getInteger("transDim"));
        } else {
            transmitter = null;
        }
        if (tagCompound.hasKey("receiver")) {
            receiver = tagCompound.getInteger("receiver");
        } else {
            receiver = null;
        }
        onceMode = tagCompound.getBoolean("once");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        if (transmitter != null) {
            tagCompound.setInteger("transX", transmitter.getCoordinate().getX());
            tagCompound.setInteger("transY", transmitter.getCoordinate().getY());
            tagCompound.setInteger("transZ", transmitter.getCoordinate().getZ());
            tagCompound.setInteger("transDim", transmitter.getDimension());
        }
        if (receiver != null) {
            tagCompound.setInteger("receiver", receiver);
        }
        tagCompound.setBoolean("once", onceMode);
    }
}
