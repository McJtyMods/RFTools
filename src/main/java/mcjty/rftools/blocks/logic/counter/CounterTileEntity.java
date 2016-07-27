package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class CounterTileEntity extends LogicTileEntity {

    public static final String CMD_SETCOUNTER = "setCounter";
    public static final String CMD_SETCURRENT = "setCurrent";

    // For pulse detection.
    private boolean prevIn = false;

    private int counter = 1;
    private int current = 0;

    public CounterTileEntity() {
    }

    public int getCounter() {
        return counter;
    }

    public int getCurrent() {
        return current;
    }

    public void setCounter(int counter) {
        this.counter = counter;
        current = 0;
        markDirtyClient();
    }

    public void setCurrent(int current) {
        this.current = current;
        markDirtyClient();
    }

    protected void update() {
        if (worldObj.isRemote) {
            return;
        }
        boolean pulse = (powerLevel > 0) && !prevIn;
        prevIn = powerLevel > 0;

        boolean newout = false;

        if (pulse) {
            current++;
            if (current >= counter) {
                current = 0;
                newout = true;
            }

            markDirty();
            setRedstoneState(newout);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("rs");
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        counter = tagCompound.getInteger("counter");
        if (counter == 0) {
            counter = 1;
        }
        current = tagCompound.getInteger("current");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powered);
        tagCompound.setBoolean("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("counter", counter);
        tagCompound.setInteger("current", current);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETCOUNTER.equals(command)) {
            setCounter(args.get("counter").getInteger());
            return true;
        } else if (CMD_SETCURRENT.equals(command)) {
            setCurrent(args.get("current").getInteger());
            return true;
        }
        return false;
    }
}
