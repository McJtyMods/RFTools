package mcjty.rftools.blocks.teleporter;

import mcjty.entity.GenericTileEntity;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.blocks.logic.RedstoneChannels;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;

public class SimpleDialerTileEntity extends GenericTileEntity {

    GlobalCoordinate transmitter;
    Integer receiver;
    private int prevValue = -1;

    public SimpleDialerTileEntity() {
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    protected void checkStateServer() {
        // Update is called manuall which is why canUpdate() returns false.
        super.checkStateServer();

        if (transmitter == null) {
            return;
        }

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newvalue = BlockTools.getRedstoneSignalIn(meta) ? 1 : 0;
        if (newvalue != prevValue) {
            prevValue = newvalue;
            if (newvalue == 1) {
                TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
                Coordinate coordinate = null;
                int dim = 0;
                if (receiver != null) {
                    GlobalCoordinate gc = destinations.getCoordinateForId(receiver);
                    if (gc != null) {
                        coordinate = gc.getCoordinate();
                        dim = gc.getDimension();
                    }
                }

                TeleportationTools.dial(worldObj, null, null, transmitter.getCoordinate(), transmitter.getDimension(), coordinate, dim, false);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("transX")) {
            transmitter = new GlobalCoordinate(new Coordinate(tagCompound.getInteger("transX"), tagCompound.getInteger("transY"), tagCompound.getInteger("transZ")), tagCompound.getInteger("transDim"));
        } else {
            transmitter = null;
        }
        if (tagCompound.hasKey("receiver")) {
            receiver = tagCompound.getInteger("receiver");
        } else {
            receiver = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
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
    }
}
