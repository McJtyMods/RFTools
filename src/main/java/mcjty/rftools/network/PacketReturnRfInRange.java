package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.EnergyTools;
import mcjty.rftools.RFTools;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnRfInRange implements IMessage {
    private Map<BlockPos, EnergyTools.EnergyLevel> levels;

    // Clientside
    public static Map<BlockPos, EnergyTools.EnergyLevel> clientLevels;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        levels = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = NetworkTools.readPos(buf);
            int e = buf.readInt();
            int m = buf.readInt();
            levels.put(pos, new EnergyTools.EnergyLevel(e, m));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(levels.size());
        for (Map.Entry<BlockPos, EnergyTools.EnergyLevel> entry : levels.entrySet()) {
            NetworkTools.writePos(buf, entry.getKey());
            buf.writeInt(entry.getValue().getEnergy());
            buf.writeInt(entry.getValue().getMaxEnergy());
        }
    }

    public Map<BlockPos, EnergyTools.EnergyLevel> getLevels() {
        return levels;
    }

    public PacketReturnRfInRange() {
    }

    public PacketReturnRfInRange(Map<BlockPos, EnergyTools.EnergyLevel> levels) {
        this.levels = levels;
    }

    public static class Handler implements IMessageHandler<PacketReturnRfInRange, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnRfInRange message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private void handle(PacketReturnRfInRange message) {
            clientLevels = message.levels;
        }
    }
}