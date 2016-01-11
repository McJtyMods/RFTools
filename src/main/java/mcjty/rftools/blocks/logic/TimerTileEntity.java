package mcjty.rftools.blocks.logic;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.entity.SyncedValue;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class TimerTileEntity extends GenericTileEntity implements ITickable {

    public static final String CMD_SETDELAY = "setDelay";
    public static final String CMD_SETCURRENT = "setDelay";

    // For pulse detection.
    private boolean prevIn = false;
    private boolean powered = false;

    private int delay = 1;
    private int timer = 0;
    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);

    public TimerTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    public int getDelay() {
        return delay;
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

    @Override
    public void setPowered(int powered) {
        this.powered = powered > 0;
    }

    private void checkStateServer() {
        boolean pulse = powered && !prevIn;
        prevIn = powered;

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

        if (newout != redstoneOut.getValue()) {
            redstoneOut.setValue(newout);
            notifyBlockUpdate();
        }
    }

    @Override
    protected int updateMetaData(int meta) {
        meta = super.updateMetaData(meta);
        Boolean value = redstoneOut.getValue();
        return BlockTools.setRedstoneSignalOut(meta, value == null ? false : value);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut.setValue(tagCompound.getBoolean("rs"));
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInteger("timer");
        powered = tagCompound.getBoolean("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        delay = tagCompound.getInteger("delay");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Boolean value = redstoneOut.getValue();
        tagCompound.setBoolean("rs", value == null ? false : value);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setInteger("timer", timer);
        tagCompound.setBoolean("powered", powered);
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
