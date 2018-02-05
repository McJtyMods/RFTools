package mcjty.rftools.blocks.logic.generic;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.Logging;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class LogicTileEntity extends GenericTileEntity {

    private LogicFacing facing;

    protected int powerOutput = 0;

    @Override
    public void onLoad() {
        if(facing == null) {
            IBlockState state = getWorld().getBlockState(getPos());
            if(state.getBlock() instanceof LogicSlabBlock) {
                setFacing(state.getValue(LogicSlabBlock.LOGIC_FACING));
            }
        }
        super.onLoad();
    }

    public LogicFacing getFacing(IBlockState state) {
        // Should not be needed but apparently it sometimes is
        if (facing == null || !(state.getBlock() instanceof LogicSlabBlock)) {
            Logging.warn(null, "LogicTileEntity has unknown/invalid facing!");
            return LogicFacing.DOWN_TOEAST;
        }
        Integer meta = state.getValue(LogicSlabBlock.META_INTERMEDIATE);
        return LogicFacing.getFacingWithMeta(facing, meta);
    }

    public void setFacing(LogicFacing facing) {
        if(facing != this.facing) {
            this.facing = facing;
            markDirty();
        }
    }

    public int getPowerOutput() {
        return powerOutput;
    }

    protected void setRedstoneState(int newout) {
        if (powerOutput == newout) {
            return;
        }
        powerOutput = newout;
        markDirty();
        EnumFacing outputSide = getFacing(getWorld().getBlockState(this.pos)).getInputSide().getOpposite();
        getWorld().neighborChanged(this.pos.offset(outputSide), this.getBlockType(), this.pos);
        //        getWorld().notifyNeighborsOfStateChange(this.pos, this.getBlockType());
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        facing = LogicFacing.VALUES[tagCompound.getInteger("lf")];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("lf", facing.ordinal());
        return tagCompound;
    }
}
