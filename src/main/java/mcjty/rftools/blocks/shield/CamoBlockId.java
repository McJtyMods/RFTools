package mcjty.rftools.blocks.shield;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NBTUtil;

public class CamoBlockId implements Comparable<CamoBlockId> {
    private final BlockState state;

    public CamoBlockId(BlockState mimicBlock) {
        state = mimicBlock;
    }

    public BlockState getBlockState() {
        return state;
    }

    @Override
    public String toString() {
        return NBTUtil.writeBlockState(state).toString();
    }

    @Override
    public int compareTo(CamoBlockId camoBlockId) {
        // @todo 1.14, is this ok?
        return getBlockState().getBlock().getRegistryName().compareTo(camoBlockId.getBlockState().getBlock().getRegistryName());
    }
}
