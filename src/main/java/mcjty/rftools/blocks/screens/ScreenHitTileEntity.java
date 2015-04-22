package mcjty.rftools.blocks.screens;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class ScreenHitTileEntity extends TileEntity {

    private int dx;
    private int dy;
    private int dz;

    @Override
    public boolean canUpdate() {
        return false;
    }

    public void setRelativeLocation(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        dx = tagCompound.getInteger("dx");
        dy = tagCompound.getInteger("dy");
        dz = tagCompound.getInteger("dz");
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("dx", dx);
        tagCompound.setInteger("dy", dy);
        tagCompound.setInteger("dz", dz);
    }
}
