package mcjty.rftools.items.builder;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public interface IFormula {

    void setup(BlockPos thisCoord, BlockPos dimension, BlockPos offset, ItemStack card);

    int isInside(int x, int y, int z);

    default boolean isCustom() { return false; }
}
