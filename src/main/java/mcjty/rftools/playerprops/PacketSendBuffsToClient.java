package mcjty.rftools.playerprops;

import io.netty.buffer.ByteBuf;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.PlayerBuff;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PacketSendBuffsToClient implements IMessage {
    private List<PlayerBuff> buffs;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readByte();
        buffs = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            buffs.add(PlayerBuff.values()[buf.readByte()]);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeByte(buffs.size());
        for (PlayerBuff buff : buffs) {
            buf.writeByte(buff.ordinal());
        }
    }

    public PacketSendBuffsToClient() {
        buffs = null;
    }

    public PacketSendBuffsToClient(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketSendBuffsToClient(Map<PlayerBuff, Integer> buffs) {
        this.buffs = new ArrayList<>(buffs.keySet());
    }

    public List<PlayerBuff> getBuffs() {
        return buffs;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            SendBuffsToClientHelper.setBuffs(this);
        });
        ctx.setPacketHandled(true);
    }
}

