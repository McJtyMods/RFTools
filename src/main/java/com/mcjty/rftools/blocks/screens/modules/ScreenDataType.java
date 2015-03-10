package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.network.NetworkTools;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;

public enum ScreenDataType {
    TYPE_NULL,
    TYPE_BYTE,
    TYPE_INT,
    TYPE_LONG,
    TYPE_DOUBLE,
    TYPE_FLOAT,
    TYPE_STRING,
    TYPE_BOOLEAN,
    TYPE_ITEMSTACK;

    public Object readObject(ByteBuf buf) {
        switch (this) {
            case TYPE_NULL:
                return null;
            case TYPE_BYTE:
                return buf.readByte();
            case TYPE_INT:
                return buf.readInt();
            case TYPE_LONG:
                return buf.readLong();
            case TYPE_DOUBLE:
                return buf.readDouble();
            case TYPE_FLOAT:
                return buf.readFloat();
            case TYPE_BOOLEAN:
                return buf.readBoolean();
            case TYPE_STRING:
                byte[] dst = new byte[buf.readInt()];
                buf.readBytes(dst);
                return new String(dst);
            case TYPE_ITEMSTACK:
                return NetworkTools.readItemStack(buf);
        }
        return null;
    }

    public static void writeObject(ByteBuf buf, Object obj) {
        if (obj == null) {
            buf.writeByte(TYPE_NULL.ordinal());
        } else if (obj instanceof Long) {
            buf.writeByte(TYPE_LONG.ordinal());
            buf.writeLong((Long) obj);
        } else if (obj instanceof Integer) {
            buf.writeByte(TYPE_INT.ordinal());
            buf.writeInt((Integer) obj);
        } else if (obj instanceof Byte) {
            buf.writeByte(TYPE_BYTE.ordinal());
            buf.writeByte((Byte) obj);
        } else if (obj instanceof Float) {
            buf.writeByte(TYPE_FLOAT.ordinal());
            buf.writeFloat((Float) obj);
        } else if (obj instanceof Double) {
            buf.writeByte(TYPE_FLOAT.ordinal());
            buf.writeDouble((Double) obj);
        } else if (obj instanceof Boolean) {
            buf.writeByte(TYPE_BOOLEAN.ordinal());
            buf.writeBoolean((Boolean) obj);
        } else if (obj instanceof String) {
            buf.writeByte(TYPE_STRING.ordinal());
            String s  = (String) obj;
            buf.writeInt(s.length());
            buf.writeBytes(s.getBytes());
        } else if (obj instanceof ItemStack) {
            buf.writeByte(TYPE_ITEMSTACK.ordinal());
            NetworkTools.writeItemStack(buf, (ItemStack) obj);
        } else {
            RFTools.log("Weird ScreenDataType!");
        }
    }
}
