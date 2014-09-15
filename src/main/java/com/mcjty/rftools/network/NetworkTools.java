package com.mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class NetworkTools {
    public static ItemStack readItemStack(ByteBuf dataIn) {
        PacketBuffer buf = new PacketBuffer(dataIn);
        try {
            NBTTagCompound nbt = buf.readNBTTagCompoundFromBuffer();
            return ItemStack.loadItemStackFromNBT(nbt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeItemStack(ByteBuf dataOut, ItemStack itemStack) {
        PacketBuffer buf = new PacketBuffer(dataOut);
        NBTTagCompound nbt = new NBTTagCompound();
        itemStack.writeToNBT(nbt);
        try {
            buf.writeNBTTagCompoundToBuffer(nbt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
