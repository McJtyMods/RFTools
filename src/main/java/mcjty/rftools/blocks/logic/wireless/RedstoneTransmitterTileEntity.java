package mcjty.rftools.blocks.logic.wireless;

import java.util.Map;

import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class RedstoneTransmitterTileEntity extends RedstoneChannelTileEntity {

    public static final String CMD_SETANALOG = "setAnalog";

    private boolean analog = false;
    private int prevIn = 0;

    public RedstoneTransmitterTileEntity() {
    }

    public boolean getAnalog() {
        return analog;
    }

    public void setAnalog(boolean analog) {
        this.analog = analog;
        markDirtyClient();
        update();
    }

    public void update() {
        if (getWorld().isRemote) {
            return;
        }

        if (channel == -1) {
            return;
        }

        int powerLevel = this.powerLevel;
        if(!analog && powerLevel > 0) {
            powerLevel = 15;
        }
        if (powerLevel != prevIn) {
            prevIn = powerLevel;
            markDirty();
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getOrCreateChannel(channel);
            ch.setValue(powerLevel);
            channels.save(getWorld());
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if(tagCompound.hasKey("prevIn", 3 /* int */)) {
            prevIn = tagCompound.getInteger("prevIn");
        } else {
            prevIn = tagCompound.getBoolean("prevIn") ? 15 : 0; // backwards compatibility
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        analog = tagCompound.getBoolean("analog");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setBoolean("analog", analog);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETANALOG.equals(command)) {
            setAnalog(args.get("analog").getBoolean());
            return true;
        }
        return false;
    }
}
