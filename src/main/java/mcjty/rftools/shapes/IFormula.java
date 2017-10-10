package mcjty.rftools.shapes;

import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.varia.Check32;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface IFormula {

    void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card);

    default void getCheckSumClient(NBTTagCompound cardTag, Check32 crc) {
        ShapeCardItem.getLocalChecksum(cardTag, crc);
    }

    default IFormulaIndex createIndex(int x, int y, int z) {
        return new DefaultFormulaIndex(x, y, z);
    }

    default boolean isInside(IFormulaIndex index) {
        DefaultFormulaIndex d = (DefaultFormulaIndex) index;
        return isInside(d.getX(), d.getY(), d.getZ());
    }

    default boolean isVisibleFromSomeSide(IFormulaIndex index) {
        DefaultFormulaIndex d = (DefaultFormulaIndex) index;
        int x = d.getX();
        int y = d.getY();
        int z = d.getZ();
        return isClear(this, x - 1, y, z) || isClear(this, x + 1, y, z) || isClear(this, x, y - 1, z) || isClear(this, x, y + 1, z) || isClear(this, x, y, z - 1) || isClear(this, x, y, z + 1);
    }

    static boolean isClear(IFormula formula, int x, int y, int z) {
        if (!formula.isInside(x, y, z)) {
            return true;
        }
        IBlockState state = formula.getLastState();
        if (state != null) {
            return ShapeBlockInfo.isNonSolidBlock(state.getBlock());
        } else {
            return false;
        }
    }


    boolean isInside(int x, int y, int z);

    default boolean isInsideSafe(int x, int y, int z) {
        return isInside(x, y, z);
    }

    /// Return the blockstate resulting from the last isInside test
    default IBlockState getLastState() { return null; }

    default boolean isBorder(int x, int y, int z) {
        if (!isInsideSafe(x - 1, y, z) || !isInsideSafe(x + 1, y, z) || !isInsideSafe(x, y, z - 1) ||
                !isInsideSafe(x, y, z + 1) || !isInsideSafe(x, y - 1, z) || !isInsideSafe(x, y + 1, z)) {
            return isInside(x, y, z);
        }
        return false;
    }

    default boolean isCustom() { return false; }

    default IFormula correctFormula(boolean solid) {
        if (solid) {
            return this;
        } else {
            return new IFormula() {
                @Override
                public void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card) {
                    IFormula.this.setup(thisCoord, dimension, offset, card);
                }

                @Override
                public void getCheckSumClient(NBTTagCompound cardTag, Check32 crc) {
                    IFormula.this.getCheckSumClient(cardTag, crc);
                }

                @Override
                public IBlockState getLastState() {
                    return IFormula.this.getLastState();
                }

                @Override
                public boolean isInside(int x, int y, int z) {
                    return IFormula.this.isBorder(x, y, z);
                }

                @Override
                public boolean isBorder(int x, int y, int z) {
                    return IFormula.this.isBorder(x, y, z);
                }
            };
        }
    }
}
