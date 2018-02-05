package mcjty.rftools.blocks.endergen;

import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class EnderMonitorTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_MODE = "mode";

    private EnderMonitorMode mode = EnderMonitorMode.MODE_LOSTPEARL;

    private boolean needpulse = false;

    public EnderMonitorTileEntity() {
    }

    public EnderMonitorMode getMode() {
        return mode;
    }

    public void setMode(EnderMonitorMode mode) {
        this.mode = mode;
        markDirtyClient();
    }

    /**
     * Callback from the endergenic in case something happens.
     * @param mode is the mode to fire
     */
    public void fireFromEndergenic(EnderMonitorMode mode) {
        if (this.mode != mode) {
            return; // Not monitoring this mode. We do nothing.
        }

        needpulse = true;
        markDirtyQuick();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        int newout = 0;

        if (needpulse) {
            markDirtyQuick();
            newout = 15;
            needpulse = false;
        }

        setRedstoneState(newout);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;

        needpulse = tagCompound.getBoolean("needPulse");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        int m = tagCompound.getInteger("mode");
        mode = EnderMonitorMode.values()[m];
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        tagCompound.setBoolean("needPulse", needpulse);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("mode", mode.ordinal());
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("mode").getString();
            setMode(EnderMonitorMode.getMode(m));
            return true;
        }
        return false;
    }

}
