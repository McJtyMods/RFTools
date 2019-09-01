package mcjty.rftools.blocks.logic.wireless;

import net.minecraft.nbt.CompoundNBT;

public class RedstoneTransmitterTileEntity extends RedstoneChannelTileEntity {

    private int prevIn = -1;

    public RedstoneTransmitterTileEntity() {
    }

    @Override
    public void setChannel(int channel) {
        super.setChannel(channel);
        update();
    }

    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        if (channel == -1) {
            return;
        }

        if (powerLevel != prevIn) {
            prevIn = powerLevel;
            markDirty();
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powerLevel);
            channels.save();
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        if(tagCompound.contains("prevIn", 3 /* int */)) {
            prevIn = tagCompound.getInteger("prevIn");
        } else {
            prevIn = tagCompound.getBoolean("prevIn") ? 15 : 0; // backwards compatibility
        }
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        return tagCompound;
    }
}
