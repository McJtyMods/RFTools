package mcjty.rftools.items.builder;

import net.minecraft.util.math.BlockPos;

public interface IFormula {

    void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset);

    int isInside(int x, int y, int z);
}
