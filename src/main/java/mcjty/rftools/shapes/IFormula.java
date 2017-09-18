package mcjty.rftools.shapes;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface IFormula {

    void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card);

    int isInside(int x, int y, int z);

    /// Return the blockstate resulting from the last isInside test
    default IBlockState getLastState() { return null; }

    default boolean isBorder(int x, int y, int z) {
        if (isInside(x, y, z) == 0) {
            return false;
        }
        int cnt = isInside(x - 1, y, z);
        cnt += isInside(x + 1, y, z);
        cnt += isInside(x, y, z - 1);
        cnt += isInside(x, y, z + 1);
        cnt += isInside(x, y - 1, z);
        cnt += isInside(x, y + 1, z);
        return cnt != 6;
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
                public IBlockState getLastState() {
                    return IFormula.this.getLastState();
                }

                @Override
                public int isInside(int x, int y, int z) {
                    return IFormula.this.isBorder(x, y, z) ? 1 : 0;
                }

                @Override
                public boolean isBorder(int x, int y, int z) {
                    return IFormula.this.isBorder(x, y, z);
                }
            };
        }
    }
}
