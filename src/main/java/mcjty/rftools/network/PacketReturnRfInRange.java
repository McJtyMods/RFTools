package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketReturnRfInRange implements IMessage {
    private Map<BlockPos, MachineInfo> levels;

    // Clientside
    public static Map<BlockPos, MachineInfo> clientLevels;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        levels = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = NetworkTools.readPos(buf);
            long e = buf.readLong();
            long m = buf.readLong();
            Long usage = null;
            if (buf.readBoolean()) {
                usage = buf.readLong();
            }
            levels.put(pos, new MachineInfo(e, m, usage));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(levels.size());
        for (Map.Entry<BlockPos, MachineInfo> entry : levels.entrySet()) {
            NetworkTools.writePos(buf, entry.getKey());
            MachineInfo info = entry.getValue();
            buf.writeLong(info.getEnergy());
            buf.writeLong(info.getMaxEnergy());
            if (info.getEnergyPerTick() != null) {
                buf.writeBoolean(true);
                buf.writeLong(info.getEnergyPerTick());
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public Map<BlockPos, MachineInfo> getLevels() {
        return levels;
    }

    public PacketReturnRfInRange() {
    }

    public PacketReturnRfInRange(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketReturnRfInRange(Map<BlockPos, MachineInfo> levels) {
        this.levels = levels;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            clientLevels = levels;
        });
        ctx.setPacketHandled(true);
    }

}