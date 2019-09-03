package mcjty.rftools.blocks.logic.wireless;

import net.minecraft.nbt.CompoundNBT;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_REDSTONE_TRANSMITTER;

public class RedstoneTransmitterTileEntity extends RedstoneChannelTileEntity {

    private int prevIn = -1;

    public RedstoneTransmitterTileEntity() {
        super(TYPE_REDSTONE_TRANSMITTER);
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
            RedstoneChannels channels = RedstoneChannels.get();
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powerLevel);
            channels.save();
        }
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        if(tagCompound.contains("prevIn", 3 /* int */)) {
            prevIn = tagCompound.getInt("prevIn");
        } else {
            prevIn = tagCompound.getBoolean("prevIn") ? 15 : 0; // backwards compatibility
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("prevIn", prevIn);
        return tagCompound;
    }
}
