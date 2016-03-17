package mcjty.rftools.blocks.logic;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

public class RedstoneReceiverTileEntity extends LogicTileEntity implements ITickable {

    private boolean redstoneOut = false;
    private int channel = -1;

    public RedstoneReceiverTileEntity() {
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(worldObj);
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            boolean newout = false;
            if (ch != null) {
                newout = ch.getValue() != 0;
            }

            if (newout != redstoneOut) {
                redstoneOut = newout;
                IBlockState state = worldObj.getBlockState(getPos());
                worldObj.setBlockState(getPos(), state.withProperty(LogicSlabBlock.OUTPUTPOWER, redstoneOut), 2);
                worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
                worldObj.markBlockForUpdate(this.pos);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        channel = tagCompound.getInteger("channel");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", redstoneOut);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("channel", channel);
    }
}
