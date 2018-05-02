package mcjty.rftools.blocks.logic.wireless;

import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class RedstoneReceiverTileEntity extends RedstoneChannelTileEntity implements ITickable {

    public static final String CMD_SETANALOG = "setAnalog";

    private boolean analog = false;

    public RedstoneReceiverTileEntity() {
    }

    public boolean getAnalog() {
        return analog;
    }

    public void setAnalog(boolean analog) {
        this.analog = analog;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        setRedstoneState(checkOutput());
    }

    public int checkOutput() {
        if (channel != -1) {
            RedstoneChannels channels = RedstoneChannels.getChannels(getWorld());
            RedstoneChannels.RedstoneChannel ch = channels.getChannel(channel);
            if (ch != null) {
                int newout = ch.getValue();
                if(!analog && newout > 0) {
                    return 15;
                }
                return newout;
            }
        }
        return 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if(tagCompound.hasKey("rs", 3 /* int */)) {
            powerOutput = tagCompound.getInteger("rs");
        } else {
            powerOutput = tagCompound.getBoolean("rs") ? 15 : 0; // backwards compatibility
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("rs", powerOutput);
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
