package mcjty.rftools.blocks.endergen;

import mcjty.entity.GenericTileEntity;
import mcjty.entity.SyncedValue;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.network.Argument;
import mcjty.varia.Logging;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class EnderMonitorTileEntity extends GenericTileEntity {

    public static final String CMD_MODE = "mode";

    private EnderMonitorMode mode = EnderMonitorMode.MODE_LOSTPEARL;

    private boolean needpulse = false;

    private SyncedValue<Boolean> redstoneOut = new SyncedValue<Boolean>(false);

    public EnderMonitorTileEntity() {
        registerSyncedObject(redstoneOut);
    }

    public EnderMonitorMode getMode() {
        return mode;
    }

    public void setMode(EnderMonitorMode mode) {
        this.mode = mode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * Callback from the endergenic in case something happens.
     * @param mode is the new mode to set
     */
    public void fireFromEndergenic(EnderMonitorMode mode) {
        if (this.mode != mode) {
            return; // Not monitoring this mode. We do nothing.
        }

        needpulse = true;
        markDirty();
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        boolean newout = false;

        if (needpulse) {
            markDirty();
            newout = true;
            needpulse = false;
        }

        if (newout != redstoneOut.getValue()) {
            redstoneOut.setValue(newout);
            Logging.log(worldObj, this, "Ender Monitor output to " + newout);
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

        needpulse = tagCompound.getBoolean("needPulse");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        int m = tagCompound.getInteger("mode");
        mode = EnderMonitorMode.values()[m];
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Boolean value = redstoneOut.getValue();
        tagCompound.setBoolean("rs", value == null ? false : value);
        tagCompound.setBoolean("needPulse", needpulse);
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
