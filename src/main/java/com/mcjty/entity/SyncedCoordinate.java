package com.mcjty.entity;

import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;

public class SyncedCoordinate extends SyncedVersionedObject {

    private Coordinate coordinate;

    public SyncedCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        coordinate = Coordinate.readFromNBT(tagCompound, "c");
    }

    public void readFromNBT(NBTTagCompound tagCompound, String tagName) {
        NBTTagCompound xCompound = tagCompound.getCompoundTag(tagName);
        readFromNBT(xCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        Coordinate.writeToNBT(tagCompound, "c", coordinate);
    }

    public void writeToNBT(NBTTagCompound tagCompound, String tagName) {
        NBTTagCompound xCompound = new NBTTagCompound();
        writeToNBT(xCompound);
        tagCompound.setTag(tagName, xCompound);
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate c) {
        coordinate = c;
        serverVersion++;
    }
}
