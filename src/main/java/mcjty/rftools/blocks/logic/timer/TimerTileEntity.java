package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.container.LogicTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class TimerTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_SETDELAY = "setDelay";
    public static final String CMD_SETCURRENT = "setDelay";
    public static final String CMD_SETPAUSES = "setPauses";

    // For pulse detection.
    private boolean prevIn = false;

    private int delay = 20;
    private int timer = 0;
    private boolean redstonePauses = false;

    public TimerTileEntity() {
    }

    public int getDelay() {
        return delay;
    }

    public int getTimer() {
        return timer;
    }

    public boolean getRedstonePauses() {
        return redstonePauses;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        timer = delay;
        markDirtyClient();
    }

    public void setRedstonePauses(boolean redstonePauses) {
        this.redstonePauses = redstonePauses;
        if(redstonePauses && powerLevel > 0) {
            timer = delay;
        }
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
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

        int newout;

        if(!redstonePauses || !prevIn) {
            timer--;
        }
        if (timer <= 0) {
            timer = delay;
            newout = 15;
        } else {
            newout = 0;
        }

        setRedstoneState(newout);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInteger("timer");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        delay = tagCompound.getInteger("delay");
        redstonePauses = tagCompound.getBoolean("redstonePauses");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setInteger("timer", timer);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("delay", delay);
        tagCompound.setBoolean("redstonePauses", redstonePauses);
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
        } else if (CMD_SETPAUSES.equals(command)) {
            setRedstonePauses(args.get("pauses").getBoolean());
            return true;
        }
        return false;
    }
}
