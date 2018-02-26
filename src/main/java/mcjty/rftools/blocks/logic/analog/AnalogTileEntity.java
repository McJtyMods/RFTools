package mcjty.rftools.blocks.logic.analog;

import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.LogicBlockSetup;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class AnalogTileEntity extends LogicTileEntity {

    public static final String CMD_UPDATE = "update";

    private float mulEqual = 1.0f;
    private float mulLess = 1.0f;
    private float mulGreater = 1.0f;

    private int addEqual = 0;
    private int addLess = 0;
    private int addGreater = 0;

    public int getPowerLevel() {
        return powerLevel;
    }

    public float getMulEqual() {
        return mulEqual;
    }

    public void setMulEqual(float mulEqual) {
        this.mulEqual = mulEqual;
        markDirtyQuick();
    }

    public float getMulLess() {
        return mulLess;
    }

    public void setMulLess(float mulLess) {
        this.mulLess = mulLess;
        markDirtyQuick();
    }

    public float getMulGreater() {
        return mulGreater;
    }

    public void setMulGreater(float mulGreater) {
        this.mulGreater = mulGreater;
        markDirtyQuick();
    }

    public int getAddEqual() {
        return addEqual;
    }

    public void setAddEqual(int addEqual) {
        this.addEqual = addEqual;
    }

    public int getAddLess() {
        return addLess;
    }

    public void setAddLess(int addLess) {
        this.addLess = addLess;
    }

    public int getAddGreater() {
        return addGreater;
    }

    public void setAddGreater(int addGreater) {
        this.addGreater = addGreater;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        mulEqual = tagCompound.getFloat("mulE");
        mulLess = tagCompound.getFloat("mulL");
        mulGreater = tagCompound.getFloat("mulG");
        addEqual = tagCompound.getInteger("addE");
        addLess = tagCompound.getInteger("addL");
        addGreater = tagCompound.getInteger("addG");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setFloat("mulE", mulEqual);
        tagCompound.setFloat("mulL", mulLess);
        tagCompound.setFloat("mulG", mulGreater);
        tagCompound.setInteger("addE", addEqual);
        tagCompound.setInteger("addL", addLess);
        tagCompound.setInteger("addG", addGreater);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_UPDATE.equals(command)) {
            mulEqual = args.get("mulE").getDouble().floatValue();
            mulLess = args.get("mulL").getDouble().floatValue();
            mulGreater = args.get("mulG").getDouble().floatValue();
            addEqual = args.get("addE").getInteger();
            addLess = args.get("addL").getInteger();
            addGreater = args.get("addG").getInteger();
            markDirtyClient();
            LogicBlockSetup.analogBlock.checkRedstone(world, pos);
            return true;
        }
        return false;
    }
}
