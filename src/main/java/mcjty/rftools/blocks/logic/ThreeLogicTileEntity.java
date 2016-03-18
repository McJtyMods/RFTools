package mcjty.rftools.blocks.logic;

import mcjty.lib.network.Argument;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class ThreeLogicTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_SETSTATE = "setState";

    private int powered = 0;
    private boolean redstoneOut = false;
    private int[] logicTable = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };    // 0 == off, 1 == on, -1 == keep

    public ThreeLogicTileEntity() {
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    @Override
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markDirty();
        }
    }

    public int getState(int index) {
        return logicTable[index];
    }

    private void checkStateServer() {
        int s = logicTable[powered];
        if (s == -1) {
            return; // Nothing happens (keep mode)
        }
        if ((s == 1) == redstoneOut) {
            return; // Output already ok.
        }
        redstoneOut = s == 1;
        IBlockState state = worldObj.getBlockState(getPos());
        worldObj.setBlockState(getPos(), state.withProperty(LogicSlabBlock.OUTPUTPOWER, redstoneOut), 2);
        worldObj.notifyNeighborsOfStateChange(this.pos, this.getBlockType());
        worldObj.markBlockForUpdate(this.pos);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");
        powered = tagCompound.getInteger("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        for (int i = 0 ; i < 8 ; i++) {
            logicTable[i] = tagCompound.getInteger("state" + i);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", redstoneOut);
        tagCompound.setInteger("powered", powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        for (int i = 0 ; i < 8 ; i++) {
            tagCompound.setInteger("state" + i, logicTable[i]);
        }
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETSTATE.equals(command)) {
            logicTable[args.get("index").getInteger()] = args.get("state").getInteger();
            markDirty();
            markDirtyClient();
            return true;
        }
        return false;
    }
}
