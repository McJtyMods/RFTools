package mcjty.rftools.blocks.endergen;

import mcjty.lib.network.Argument;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.logic.LogicSlabBlock;
import mcjty.rftools.blocks.logic.LogicTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class EnderMonitorTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_MODE = "mode";

    private EnderMonitorMode mode = EnderMonitorMode.MODE_LOSTPEARL;

    private boolean needpulse = false;

    private boolean redstoneOut = false;

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
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        boolean newout = false;

        if (needpulse) {
            markDirty();
            newout = true;
            needpulse = false;
        }

        if (newout != redstoneOut) {
            redstoneOut = newout;
            Logging.log(worldObj, this, "Ender Monitor output to " + newout);
            IBlockState state = worldObj.getBlockState(getPos());
            worldObj.setBlockState(getPos(), state.withProperty(LogicSlabBlock.OUTPUTPOWER, redstoneOut), 2);
            worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
            worldObj.notifyBlockUpdate(this.pos, state, state, 3);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");

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
        tagCompound.setBoolean("rs", redstoneOut);
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
