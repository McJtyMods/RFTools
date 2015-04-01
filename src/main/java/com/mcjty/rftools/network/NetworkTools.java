package com.mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class NetworkTools {
    /// This function supports itemstacks with more then 64 items.
    public static ItemStack readItemStack(ByteBuf dataIn) {
        PacketBuffer buf = new PacketBuffer(dataIn);
        try {
            NBTTagCompound nbt = buf.readNBTTagCompoundFromBuffer();
            ItemStack stack = ItemStack.loadItemStackFromNBT(nbt);
            stack.stackSize = buf.readInt();
            return stack;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /// This function supports itemstacks with more then 64 items.
    public static void writeItemStack(ByteBuf dataOut, ItemStack itemStack) {
        PacketBuffer buf = new PacketBuffer(dataOut);
        NBTTagCompound nbt = new NBTTagCompound();
        itemStack.writeToNBT(nbt);
        try {
            buf.writeNBTTagCompoundToBuffer(nbt);
            buf.writeInt(itemStack.stackSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
