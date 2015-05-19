package mcjty.rftools.network;

import mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Map;

public class AbstractServerCommand implements IMessage {
    protected int x;
    protected int y;
    protected int z;
    protected String command;
    protected Map<String,Argument> args;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        command = NetworkTools.readString(buf);
        args = readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        NetworkTools.writeString(buf, command);
        writeArguments(buf, args);
    }

    public static Map<String,Argument> readArguments(ByteBuf buf) {
        Map<String,Argument> args;
        int size = buf.readInt();
        if (size == 0) {
            args = null;
        } else {
            args = new HashMap<String,Argument>(size);
            for (int i = 0 ; i < size ; i++) {
                String key = NetworkTools.readString(buf);
                ArgumentType type = ArgumentType.getType(buf.readByte());
                switch (type) {
                    case TYPE_STRING:
                        args.put(key, new Argument(key, NetworkTools.readString(buf)));
                        break;
                    case TYPE_INTEGER:
                        args.put(key, new Argument(key, buf.readInt()));
                        break;
                    case TYPE_COORDINATE:
                        int cx = buf.readInt();
                        int cy = buf.readInt();
                        int cz = buf.readInt();
                        if (cx == -1 && cy == -1 && cz == -1) {
                            args.put(key, new Argument(key, (Coordinate) null));
                        } else {
                            args.put(key, new Argument(key, new Coordinate(cx, cy, cz)));
                        }
                        break;
                    case TYPE_BOOLEAN:
                        args.put(key, new Argument(key, buf.readByte() == 1));
                        break;
                    case TYPE_DOUBLE:
                        args.put(key, new Argument(key, buf.readDouble()));
                        break;
                }
            }
        }
        return args;
    }

    public static void writeArguments(ByteBuf buf, Map<String,Argument> args) {
        if (args == null) {
            buf.writeInt(0);
        } else {
            buf.writeInt(args.size());
            for (Argument arg : args.values()) {
                String key = arg.getName();
                NetworkTools.writeString(buf, key);
                buf.writeByte(arg.getType().getIndex());
                switch (arg.getType()) {
                    case TYPE_STRING:
                        NetworkTools.writeString(buf, arg.getString());
                        break;
                    case TYPE_INTEGER:
                        buf.writeInt(arg.getInteger());
                        break;
                    case TYPE_COORDINATE:
                        Coordinate c = arg.getCoordinate();
                        if (c == null) {
                            buf.writeInt(-1);
                            buf.writeInt(-1);
                            buf.writeInt(-1);
                        } else {
                            buf.writeInt(c.getX());
                            buf.writeInt(c.getY());
                            buf.writeInt(c.getZ());
                        }
                        break;
                    case TYPE_BOOLEAN:
                        buf.writeByte(arg.getBoolean() ? 1 : 0);
                        break;
                    case TYPE_DOUBLE:
                        buf.writeDouble(arg.getDouble());
                        break;
                }
            }
        }
    }

    protected AbstractServerCommand() {
    }

    protected AbstractServerCommand(int x, int y, int z, String command, Argument... arguments) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.command = command;
        if (arguments == null) {
            this.args = null;
        } else {
            args = new HashMap<String, Argument>(arguments.length);
            for (Argument arg : arguments) {
                args.put(arg.getName(), arg);
            }
        }
    }

}
