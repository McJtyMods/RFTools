package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.varia.BlockPosTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public class InvBlockInfo {
    private final BlockPos coordinate;
    private final int size;

    public InvBlockInfo(BlockPos coordinate, int size) {
        this.coordinate = coordinate;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public BlockPos getCoordinate() {
        return coordinate;
    }

    public static InvBlockInfo readFromNBT(NBTTagCompound tagCompound) {
        BlockPos coordinate = BlockPosTools.readFromNBT(tagCompound, "c");
        int size = tagCompound.getInteger("size");
        InvBlockInfo invBlockInfo = new InvBlockInfo(coordinate, size);
        return invBlockInfo;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = BlockPosTools.writeToNBT(coordinate);
        tagCompound.setInteger("size", size);
        return tagCompound;
    }
}
