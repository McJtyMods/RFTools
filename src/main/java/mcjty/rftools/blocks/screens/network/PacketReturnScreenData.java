package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.screens.modules.ScreenDataType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnScreenData implements IMessage {
    private BlockPos pos;
    Map<Integer, Object[]> screenData;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        int size = buf.readInt();
        screenData = new HashMap<Integer, Object[]>(size);
        for (int i = 0 ; i < size ; i++) {
            int key = buf.readInt();
            int arsize = buf.readInt();
            Object[] ar = new Object[arsize];
            for (int j = 0 ; j < arsize ; j++) {
                byte type = buf.readByte();
                ScreenDataType dataType = ScreenDataType.values()[type];
                ar[j] = dataType.readObject(buf);
            }
            screenData.put(key, ar);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(screenData.size());
        for (Map.Entry<Integer, Object[]> me : screenData.entrySet()) {
            buf.writeInt(me.getKey());
            Object[] c = me.getValue();
            buf.writeInt(c.length);
            for (Object o : c) {
                ScreenDataType.writeObject(buf, o);
            }
        }
    }

    public PacketReturnScreenData() {
    }

    public BlockPos getPos() {
        return pos;
    }

    public PacketReturnScreenData(BlockPos pos, Map<Integer, Object[]> screenData) {
        this.pos = pos;
        this.screenData = screenData;
    }

    public static class Handler implements IMessageHandler<PacketReturnScreenData, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnScreenData message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> PacketGetScreenDataHelper.setScreenData(message));
            return null;
        }

    }
}