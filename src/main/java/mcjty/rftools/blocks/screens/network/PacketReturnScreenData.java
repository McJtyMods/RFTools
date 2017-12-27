package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import mcjty.rftools.api.screens.data.IModuleData;
import mcjty.rftools.api.screens.data.IModuleDataFactory;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnScreenData implements IMessage {
    private GlobalCoordinate pos;
    private Map<Integer, IModuleData> screenData;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = new GlobalCoordinate(NetworkTools.readPos(buf), buf.readInt());
        int size = buf.readInt();
        screenData = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            int key = buf.readInt();
            int shortId = buf.readInt();
            String id = RFTools.screenModuleRegistry.getNormalId(shortId);
            IModuleDataFactory<?> dataFactory = RFTools.screenModuleRegistry.getModuleDataFactory(id);
            IModuleData data = dataFactory.createData(buf);
            screenData.put(key, data);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos.getCoordinate());
        buf.writeInt(pos.getDimension());

        buf.writeInt(screenData.size());
        for (Map.Entry<Integer, IModuleData> me : screenData.entrySet()) {
            buf.writeInt(me.getKey());
            IModuleData c = me.getValue();
            buf.writeInt(RFTools.screenModuleRegistry.getShortId(c.getId()));
            c.writeToBuf(buf);
        }
    }

    public PacketReturnScreenData() {
    }

    public GlobalCoordinate getPos() {
        return pos;
    }

    public Map<Integer, IModuleData> getScreenData() {
        return screenData;
    }

    public PacketReturnScreenData(GlobalCoordinate pos, Map<Integer, IModuleData> screenData) {
        this.pos = pos;
        this.screenData = screenData;
    }

    public static class Handler implements IMessageHandler<PacketReturnScreenData, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnScreenData message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> PacketGetScreenDataHelper.setScreenData(message));
            return null;
        }

    }
}