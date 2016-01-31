package mcjty.rftools.blocks.builder;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockMeta;
import mcjty.rftools.items.builder.GuiChamberDetails;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.HashMap;
import java.util.Map;

public class PacketChamberInfoReady implements IMessage {
    private Map<BlockMeta,Integer> blocks;
    private Map<BlockMeta,Integer> costs;
    private Map<String,Integer> entities;
    private Map<String,Integer> entityCosts;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        blocks = new HashMap<>(size);
        costs = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            byte meta = buf.readByte();
            int count = buf.readInt();
            int cost = buf.readInt();
            Block block = Block.blockRegistry.getObjectById(id);
            BlockMeta bm = new BlockMeta(block, meta);
            blocks.put(bm, count);
            costs.put(bm, cost);
        }

        size = buf.readInt();
        entities = new HashMap<>(size);
        entityCosts = new HashMap<>(size);
        for (int i = 0 ; i < size ; i++) {
            String className = NetworkTools.readString(buf);
            int count = buf.readInt();
            int cost = buf.readInt();
            entities.put(className, count);
            entityCosts.put(className, cost);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blocks.size());
        for (Map.Entry<BlockMeta, Integer> entry : blocks.entrySet()) {
            Block block = entry.getKey().getBlock();
            buf.writeInt(Block.blockRegistry.getIDForObject(block));
            buf.writeByte(entry.getKey().getMeta());
            buf.writeInt(entry.getValue());
            buf.writeInt(costs.get(entry.getKey()));
        }
        buf.writeInt(entities.size());
        for (Map.Entry<String, Integer> entry : entities.entrySet()) {
            NetworkTools.writeString(buf, entry.getKey());
            buf.writeInt(entry.getValue());
            buf.writeInt(entityCosts.get(entry.getKey()));
        }
    }

    public PacketChamberInfoReady() {
    }

    public PacketChamberInfoReady(Map<BlockMeta,Integer> blocks, Map<BlockMeta,Integer> costs, Map<String,Integer> entities, Map<String,Integer> entityCosts) {
        this.blocks = new HashMap<>(blocks);
        this.costs = new HashMap<>(costs);
        this.entities = new HashMap<>(entities);
        this.entityCosts = new HashMap<>(entityCosts);
    }

    public static class Handler implements IMessageHandler<PacketChamberInfoReady, IMessage> {
        @Override
        public IMessage onMessage(PacketChamberInfoReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketChamberInfoReady message, MessageContext ctx) {
            GuiChamberDetails.setItemsWithCount(message.blocks, message.costs, message.entities, message.entityCosts);
        }
    }

}
