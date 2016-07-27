package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class TimerTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_SETDELAY = "setDelay";
    public static final String CMD_SETCURRENT = "setDelay";

    // For pulse detection.
    private boolean prevIn = false;

    private int delay = 20;
    private int timer = 0;

    public TimerTileEntity() {
    }

    public int getDelay() {
        return delay;
    }

    public int getTimer() {
        return timer;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        timer = delay;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        boolean pulse = (powerLevel > 0) && !prevIn;
        prevIn = powerLevel > 0;

        markDirty();

        if (pulse) {
            timer = delay;
        }

        boolean newout;

        timer--;
        if (timer <= 0) {
            timer = delay;
            newout = true;
        } else {
            newout = false;
        }

        setRedstoneState(newout);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("rs");
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInteger("timer");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        delay = tagCompound.getInteger("delay");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powered);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setInteger("timer", timer);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("delay", delay);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETDELAY.equals(command)) {
            setDelay(args.get("delay").getInteger());
            return true;
        }
        return false;
    }
}
