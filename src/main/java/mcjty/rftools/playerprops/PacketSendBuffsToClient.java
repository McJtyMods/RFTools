package mcjty.rftools.playerprops;

import mcjty.rftools.PlayerBuff;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PacketSendBuffsToClient implements IMessage {
    private List<PlayerBuff> buffs;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readByte();
        buffs = new ArrayList<PlayerBuff>(size);
        for (int i = 0 ; i < size ; i++) {
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

    public PacketSendBuffsToClient(Map<PlayerBuff,Integer> buffs) {
        this.buffs = new ArrayList<PlayerBuff>(buffs.keySet());
    }

    public List<PlayerBuff> getBuffs() {
        return buffs;
    }
}
