package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.varia.BlockMeta;
import net.minecraft.block.Block;
import net.minecraft.network.PacketBuffer;

import java.util.HashMap;
import java.util.Map;

public class PacketChamberInfoReady implements IMessage, IMessageHandler<PacketChamberInfoReady, IMessage> {
    private Map<BlockMeta,Integer> blocks;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        blocks = new HashMap<BlockMeta, Integer>(size);
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            byte meta = buf.readByte();
            int count = buf.readInt();
            Block block = (Block) Block.blockRegistry.getObjectById(id);
            blocks.put(new BlockMeta(block, meta), count);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blocks.size());
        PacketBuffer buffer = new PacketBuffer(buf);
        for (Map.Entry<BlockMeta, Integer> entry : blocks.entrySet()) {
            Block block = entry.getKey().getBlock();
            buffer.writeInt(Block.blockRegistry.getIDForObject(block));
            buffer.writeByte(entry.getKey().getMeta());
            buffer.writeInt(entry.getValue());
        }
    }

    public PacketChamberInfoReady() {
    }

    public PacketChamberInfoReady(Map<BlockMeta,Integer> blocks) {
        this.blocks = new HashMap<BlockMeta, Integer>(blocks);
    }

    @Override
    public IMessage onMessage(PacketChamberInfoReady message, MessageContext ctx) {
        GuiChamberDetails.setItemsWithCount(message.blocks);
        return null;
    }

}
