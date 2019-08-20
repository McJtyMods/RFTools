package mcjty.rftools.blocks.screens;

import mcjty.lib.tileentity.GenericTileEntity;
import net.minecraft.nbt.CompoundNBT;

public class ScreenHitTileEntity extends GenericTileEntity {

    private int dx;
    private int dy;
    private int dz;

    public void setRelativeLocation(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        markDirty();
        BlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
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
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("dx", dx);
        tagCompound.setInteger("dy", dy);
        tagCompound.setInteger("dz", dz);
        return tagCompound;
    }

//    @Override
//    public CompoundNBT getUpdateTag() {
//        CompoundNBT updateTag = super.getUpdateTag();
//        writeToNBT(updateTag);
//        return updateTag;
//    }
//
//    @Nullable
//    @Override
//    public SPacketUpdateTileEntity getUpdatePacket() {
//        CompoundNBT nbtTag = new CompoundNBT();
//        this.writeToNBT(nbtTag);
//        return new SPacketUpdateTileEntity(getPos(), 1, nbtTag);
//    }
//
//    @Override
//    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
//        readFromNBT(packet.getNbtCompound());
//    }
}
