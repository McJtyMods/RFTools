package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;

import java.util.Collection;

public class ByteBufTools {

    public static <T extends Enum> void writeEnum(ByteBuf buf, T value, T nullValue) {
        if (value == null) {
            buf.writeInt(nullValue.ordinal());
        } else {
            buf.writeInt(value.ordinal());
        }
    }

    public static <T extends Enum> T readEnum(ByteBuf buf, T[] values) {
        return values[buf.readInt()];
    }

    public static void writeEnumCollection(ByteBuf buf, Collection<? extends Enum> collection) {
        buf.writeInt(collection.size());
        for (Enum type : collection) {
            buf.writeInt(type.ordinal());
        }
    }

    public static <T extends Enum> void readEnumCollection(ByteBuf buf, Collection<T> collection, T[] values) {
        collection.clear();
        int size = buf.readInt();
        for (int i = 0 ; i < size ; i++) {
            collection.add(values[buf.readInt()]);
        }
    }

    public static void writeString(ByteBuf buf, String str) {
        if (str == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(str.length());
            buf.writeBytes(str.getBytes());
        }
    }

    public static String readString(ByteBuf buf) {
        int size = buf.readInt();
        if (size == -1) {
            return null;
        }
        byte[] dst = new byte[size];
        buf.readBytes(dst);
        return new String(dst);
    }

    public static void writeFloat(ByteBuf buf, Float f) {
        if (f != null) {
            buf.writeBoolean(true);
            buf.writeFloat(f);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static Float readFloat(ByteBuf buf) {
        if (buf.readBoolean()) {
            return buf.readFloat();
        } else {
            return null;
        }
    }
}
