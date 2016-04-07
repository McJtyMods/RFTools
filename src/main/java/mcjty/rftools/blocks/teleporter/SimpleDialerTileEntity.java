package mcjty.rftools.blocks.teleporter;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.logic.LogicTileEntity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class SimpleDialerTileEntity extends LogicTileEntity {

    GlobalCoordinate transmitter;
    Integer receiver;
    private boolean onceMode = false;

    private boolean prevIn = false;
    private boolean powered = false;

    public SimpleDialerTileEntity() {
    }

    @Override
    public void setPowered(int powered) {
        this.powered = powered > 0;
        markDirty();
    }

    public void update() {
        if (transmitter == null) {
            return;
        }

        if (powered == prevIn) {
            return;
        }

        prevIn = powered;
        markDirty();

        if (powered) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
            BlockPos coordinate = null;
            int dim = 0;
            if (receiver != null) {
                GlobalCoordinate gc = destinations.getCoordinateForId(receiver);
                if (gc != null) {
                    coordinate = gc.getCoordinate();
                    dim = gc.getDimension();
                }
            }

            int dial = TeleportationTools.dial(worldObj, null, null, transmitter.getCoordinate(), transmitter.getDimension(), coordinate, dim, onceMode);
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

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("powered");
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
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("powered", powered);
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
