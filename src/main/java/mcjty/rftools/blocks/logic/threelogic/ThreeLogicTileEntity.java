package mcjty.rftools.blocks.logic.threelogic;

import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class ThreeLogicTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_SETSTATE = "setState";

    private int[] logicTable = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };    // 0 == off, 1 == on, -1 == keep

    public ThreeLogicTileEntity() {
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    public int getState(int index) {
        return logicTable[index];
    }

    private void checkStateServer() {
        int s = logicTable[powerLevel];
        if (s == -1) {
            return; // Nothing happens (keep mode)
        }
        setRedstoneState(s == 1 ? 15 : 0);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        for (int i = 0 ; i < 8 ; i++) {
            logicTable[i] = tagCompound.getInteger("state" + i);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        return tagCompound;
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
