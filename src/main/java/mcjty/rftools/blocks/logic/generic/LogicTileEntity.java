package mcjty.rftools.blocks.logic.generic;

import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;

public class LogicTileEntity extends GenericTileEntity {

    private LogicFacing facing = LogicFacing.DOWN_TONORTH;

    public LogicFacing getFacing() {
        Integer meta = worldObj.getBlockState(pos).getValue(LogicSlabBlock.META_INTERMEDIATE);
        return LogicFacing.getFacingWithMeta(facing, meta);
    }

    public void setFacing(LogicFacing facing) {
        this.facing = facing;
        markDirty();
    }

    protected void setRedstoneState(boolean newout) {
        markDirty();
        IBlockState state = worldObj.getBlockState(getPos());
        worldObj.setBlockState(getPos(), state.withProperty(LogicSlabBlock.OUTPUTPOWER, newout), 2);
        worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
        worldObj.notifyBlockUpdate(this.pos, state, state, 3);
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
