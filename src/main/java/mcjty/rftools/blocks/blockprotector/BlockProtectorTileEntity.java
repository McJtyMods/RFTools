package mcjty.rftools.blocks.blockprotector;

import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.entity.SyncedValueSet;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Set;

public class BlockProtectorTileEntity extends GenericEnergyReceiverTileEntity {

    // Relative coordinates (relative to this tile entity)
    private SyncedValueSet<Coordinate> protectedBlocks = new SyncedValueSet<Coordinate>() {
        @Override
        public Coordinate readElementFromNBT(NBTTagCompound tagCompound) {
            return Coordinate.readFromNBT(tagCompound, "c");
        }

        @Override
        public NBTTagCompound writeElementToNBT(Coordinate element) {
            return Coordinate.writeToNBT(element);
        }
    };


    public BlockProtectorTileEntity() {
        super(BlockProtectorConfiguration.MAXENERGY, BlockProtectorConfiguration.RECEIVEPERTICK);
        registerSyncedObject(protectedBlocks);
    }

    public Set<Coordinate> getProtectedBlocks() {
        return protectedBlocks;
    }

    // Toggle a coordinate to be protected or not. The coordinate given here is absolute.
    public void toggleCoordinate(GlobalCoordinate c) {
        if (c.getDimension() != worldObj.provider.dimensionId) {
            // Wrong dimension. Don't do anything.
            return;
        }
        Coordinate relative = new Coordinate(c.getCoordinate().getX() - xCoord, c.getCoordinate().getY() - yCoord, c.getCoordinate().getZ() - zCoord);
        if (protectedBlocks.contains(relative)) {
            protectedBlocks.remove(relative);
        } else {
            protectedBlocks.add(relative);
        }
        markDirty();
        notifyBlockUpdate();
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        protectedBlocks.readFromNBT(tagCompound, "coordinates");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        protectedBlocks.writeToNBT(tagCompound, "coordinates");
    }
}
