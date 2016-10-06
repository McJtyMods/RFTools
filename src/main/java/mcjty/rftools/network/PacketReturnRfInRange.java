package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketReturnRfInRange implements IMessage {
    private Map<BlockPos, PacketGetRfInRange.MachineInfo> levels;

    // Clientside
    public static Map<BlockPos, PacketGetRfInRange.MachineInfo> clientLevels;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        levels = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = NetworkTools.readPos(buf);
            int e = buf.readInt();
            int m = buf.readInt();
            Integer usage = null;
            if (buf.readBoolean()) {
                usage = buf.readInt();
            }
            levels.put(pos, new PacketGetRfInRange.MachineInfo(e, m, usage));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(levels.size());
        for (Map.Entry<BlockPos, PacketGetRfInRange.MachineInfo> entry : levels.entrySet()) {
            NetworkTools.writePos(buf, entry.getKey());
            PacketGetRfInRange.MachineInfo info = entry.getValue();
            buf.writeInt(info.getEnergy());
            buf.writeInt(info.getMaxEnergy());
            if (info.getEnergyPerTick() != null) {
                buf.writeBoolean(true);
                buf.writeInt(info.getEnergyPerTick());
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public Map<BlockPos, PacketGetRfInRange.MachineInfo> getLevels() {
        return levels;
    }

    public PacketReturnRfInRange() {
    }

    public PacketReturnRfInRange(Map<BlockPos, PacketGetRfInRange.MachineInfo> levels) {
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