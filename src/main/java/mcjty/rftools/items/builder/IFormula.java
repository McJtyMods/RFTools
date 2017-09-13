package mcjty.rftools.items.builder;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface IFormula {

    void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, NBTTagCompound card);

    int isInside(int x, int y, int z);

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
