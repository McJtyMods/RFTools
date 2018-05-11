package mcjty.rftools.blocks.logic.analog;

import mcjty.lib.blocks.LogicSlabBlock;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.LogicFacing;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;

public class AnalogTileEntity extends LogicTileEntity {

    public static final String CMD_UPDATE = "analog.update";
    public static final Key<Double> PARAM_MUL_EQ = new Key<>("mul_eq", Type.DOUBLE);
    public static final Key<Double> PARAM_MUL_LESS = new Key<>("mul_less", Type.DOUBLE);
    public static final Key<Double> PARAM_MUL_GT = new Key<>("mul_gt", Type.DOUBLE);
    public static final Key<Integer> PARAM_ADD_EQ = new Key<>("add_eq", Type.INTEGER);
    public static final Key<Integer> PARAM_ADD_LESS = new Key<>("add_less", Type.INTEGER);
    public static final Key<Integer> PARAM_ADD_GT = new Key<>("add_gt", Type.INTEGER);

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
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_UPDATE.equals(command)) {
            mulEqual = params.get(PARAM_MUL_EQ).floatValue();
            mulLess = params.get(PARAM_MUL_LESS).floatValue();
            mulGreater = params.get(PARAM_MUL_GT).floatValue();
            addEqual = params.get(PARAM_ADD_EQ);
            addLess = params.get(PARAM_ADD_LESS);
            addGreater = params.get(PARAM_ADD_GT);
            markDirtyClient();
            checkRedstone(world, pos);
            return true;
        }
        return false;
    }

    private static Set<BlockPos> loopDetector = new HashSet<>();

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (loopDetector.add(pos)) {
            try {
                LogicFacing facing = getFacing(state);
                EnumFacing downSide = facing.getSide();
                EnumFacing inputSide = facing.getInputSide();
                EnumFacing rightSide = LogicSlabBlock.rotateLeft(downSide, inputSide);
                EnumFacing leftSide = LogicSlabBlock.rotateRight(downSide, inputSide);

                int outputStrength;
                int inputStrength = getInputStrength(world, pos, inputSide);
                int inputLeft = getInputStrength(world, pos, leftSide);
                int inputRight = getInputStrength(world, pos, rightSide);
                if (inputLeft == inputRight) {
                    outputStrength = (int) (inputStrength * getMulEqual() + getAddEqual());
                } else if (inputLeft < inputRight) {
                    outputStrength = (int) (inputStrength * getMulLess() + getAddLess());
                } else {
                    outputStrength = (int) (inputStrength * getMulGreater() + getAddGreater());
                }
                if (outputStrength > 15) {
                    outputStrength = 15;
                } else if (outputStrength < 0) {
                    outputStrength = 0;
                }

                int oldPower = getPowerLevel();
                setPowerInput(outputStrength);
                if (oldPower != outputStrength) {
                    world.notifyNeighborsOfStateChange(pos, getBlockType(), false);
                }
            } finally {
                loopDetector.remove(pos);
            }
        }
    }

    @Override
    public int getRedstoneOutput(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        if (side == getFacing(state).getInputSide()) {
            return getPowerLevel();
        } else {
            return 0;
        }
    }
}
