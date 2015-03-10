package com.mcjty.rftools.blocks.endergen;

import com.mcjty.rftools.RFTools;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class EndergenicPearl {
    private int ticksLeft;
    private final Coordinate destination;
    private final int age;

    public EndergenicPearl(int ticksLeft, Coordinate destination, int age) {
        this.ticksLeft = ticksLeft;
        this.destination = destination;
        this.age = age;
    }

    public EndergenicPearl(NBTTagCompound tagCompound) {
        ticksLeft = tagCompound.getInteger("t");
        destination = Coordinate.readFromNBT(tagCompound, "dest");
        age = tagCompound.getInteger("age");
    }

    public int getTicksLeft() {
        return ticksLeft;
    }

    public int getAge() {
        return age;
    }

    public Coordinate getDestination() {
        return destination;
    }

    // Return true if the pearl has to be removed (it arrived).
    public boolean handleTick(World world) {
        ticksLeft--;
        if (ticksLeft <= 0) {
            // We arrived. Check that the destination is still there.
            TileEntity te = world.getTileEntity(destination.getX(), destination.getY(), destination.getZ());
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity endergenicTileEntity = (EndergenicTileEntity) te;
                endergenicTileEntity.receivePearl(age);
            } else {
                RFTools.log("Pearl: where did the destination go?");
            }
            return true;
        }
        return false;
    }

    public NBTTagCompound getTagCompound() {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger("t", ticksLeft);
        Coordinate.writeToNBT(tagCompound, "dest", destination);
        tagCompound.setInteger("age", age);
        return tagCompound;
    }
}
