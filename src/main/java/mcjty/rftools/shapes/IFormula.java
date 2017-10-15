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

    /// Return true if this coordinate is visible from at least one side.
    /// WARNING! This function assumes that x,y,z is not a coordinate at the side of the outer box!
    default boolean isVisible(int x, int y, int z) {
        // We know data != null here because isVisible is only called if isInside is already true
        return isClear(x - 1, y, z) || isClear(x + 1, y, z) || isClear(x, y - 1, z) || isClear(x, y + 1, z) || isClear(x, y, z - 1) || isClear(x, y, z + 1);
    }

    default boolean isClear(int x, int y, int z) {
        if (!isInside(x, y, z)) {
            return true;
        }
        IBlockState state = getLastState();
        if (state != null) {
            return ShapeBlockInfo.isNonSolidBlock(state.getBlock());
        } else {
            return false;
        }
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
