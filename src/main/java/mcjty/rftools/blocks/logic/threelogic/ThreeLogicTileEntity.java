package mcjty.rftools.blocks.logic.threelogic;

import mcjty.lib.container.LogicTileEntity;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.typed.Key;
import mcjty.typed.Type;
import mcjty.typed.TypedMap;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class ThreeLogicTileEntity extends LogicTileEntity {

    public static final String CMD_SETSTATE = "logic.setState";
    public static final Key<Integer> PARAM_INDEX = new Key<>("index", Type.INTEGER);
    public static final Key<Integer> PARAM_STATE = new Key<>("state", Type.INTEGER);

    private int[] logicTable = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };    // 0 == off, 1 == on, -1 == keep

    public ThreeLogicTileEntity() {
    }

//    @Override
//    public void update() {
//        if (!getWorld().isRemote) {
//            checkStateServer();
//        }
//    }
//
    public int getState(int index) {
        return logicTable[index];
    }

    public void checkRedstone() {
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
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETSTATE.equals(command)) {
            logicTable[params.get(PARAM_INDEX)] = params.get(PARAM_STATE);
            markDirtyClient();
            LogicBlockSetup.threeLogicBlock.checkRedstone(world, pos);
            return true;
        }
        return false;
    }
}
