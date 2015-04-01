package mcjty.rftools.blocks.storagemonitor;

import mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;

public class InvBlockInfo {
    private final Coordinate coordinate;
    private final int size;

    public InvBlockInfo(Coordinate coordinate, int size) {
        this.coordinate = coordinate;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public static InvBlockInfo readFromNBT(NBTTagCompound tagCompound) {
        Coordinate coordinate = Coordinate.readFromNBT(tagCompound, "c");
        int size = tagCompound.getInteger("size");
        InvBlockInfo invBlockInfo = new InvBlockInfo(coordinate, size);
        return invBlockInfo;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tagCompound = Coordinate.writeToNBT(coordinate);
        tagCompound.setInteger("size", size);
        return tagCompound;
    }
}
