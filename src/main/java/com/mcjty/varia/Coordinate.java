package com.mcjty.varia;

import com.mcjty.rftools.network.ByteBufConverter;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

public class Coordinate implements ByteBufConverter {
    private final int x;
    private final int y;
    private final int z;

    public static final Coordinate INVALID = new Coordinate(-1, -1, -1);

    public Coordinate(ByteBuf buf) {
        this(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public boolean isValid() {
        return y >= 0;
    }

    public Coordinate addDirection(ForgeDirection direction) {
        return new Coordinate(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Coordinate that = (Coordinate) o;

        if (x != that.x) {
            return false;
        }
        if (y != that.y) {
            return false;
        }
        if (z != that.z) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public String toString() {
        return x + "," + y + "," + z;
    }

    public static Coordinate readFromNBT(NBTTagCompound tagCompound, String tagName) {
        int[] array = tagCompound.getIntArray(tagName);
        if (array.length == 0) {
            return null;
        } else {
            return new Coordinate(array[0], array[1], array[2]);
        }
    }

    public static void writeToNBT(NBTTagCompound tagCompound, String tagName, Coordinate coordinate) {
        if (coordinate == null) {
            tagCompound.setIntArray(tagName, new int[] { });
        } else {
            tagCompound.setIntArray(tagName, new int[] { coordinate.getX(), coordinate.getY(), coordinate.getZ() });
        }
    }

    public static NBTTagCompound writeToNBT(Coordinate coordinate) {
        NBTTagCompound tagCompound = new NBTTagCompound();
        writeToNBT(tagCompound, "c", coordinate);
        return tagCompound;
    }

}
