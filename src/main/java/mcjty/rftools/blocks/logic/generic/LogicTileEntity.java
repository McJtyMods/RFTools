package mcjty.rftools.blocks.logic.generic;

import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class LogicTileEntity extends GenericTileEntity {

    private LogicFacing facing = LogicFacing.DOWN_TONORTH;

    protected boolean powered = false;

    public LogicFacing getFacing(IBlockState state) {
        Integer meta = state.getValue(LogicSlabBlock.META_INTERMEDIATE);
        return LogicFacing.getFacingWithMeta(facing, meta);
    }

    public void setFacing(LogicFacing facing) {
        this.facing = facing;
        markDirty();
    }

    public boolean isPowered() {
        return powered;
    }

    protected void setRedstoneState(boolean newout) {
        if (powered == newout) {
            return;
        }
        powered = newout;
        markDirty();
        EnumFacing outputSide = getFacing(worldObj.getBlockState(this.pos)).getInputSide().getOpposite();
        worldObj.notifyBlockOfStateChange(this.pos.offset(outputSide), this.getBlockType());
//        worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        facing = LogicFacing.values()[tagCompound.getInteger("lf")];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("lf", facing.ordinal());
        return tagCompound;
    }
}
