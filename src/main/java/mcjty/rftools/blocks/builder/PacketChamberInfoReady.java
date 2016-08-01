package mcjty.rftools.blocks.builder;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.BlockMeta;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.builder.GuiChamberDetails;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PacketChamberInfoReady implements IMessage {
    private Map<BlockMeta,Integer> blocks;
    private Map<BlockMeta,Integer> costs;
    private Map<BlockMeta,ItemStack> stacks;
    private Map<String,Integer> entities;
    private Map<String,Integer> entityCosts;
    private Map<String,Entity> realEntities;
    private Map<String,String> playerNames;

    private static final byte ENTITY_NONE = 0;
    private static final byte ENTITY_NORMAL = 1;
    private static final byte ENTITY_PLAYER = 2;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        blocks = new HashMap<>(size);
        costs = new HashMap<>(size);
        stacks = new HashMap<>();
        for (int i = 0 ; i < size ; i++) {
            int id = buf.readInt();
            byte meta = buf.readByte();
            int count = buf.readInt();
            int cost = buf.readInt();
            Block block = Block.REGISTRY.getObjectById(id);
            BlockMeta bm = new BlockMeta(block, meta);
            blocks.put(bm, count);
            costs.put(bm, cost);
            if (buf.readBoolean()) {
                ItemStack stack = NetworkTools.readItemStack(buf);
                stacks.put(bm, stack);
            }
        }

        size = buf.readInt();
        entities = new HashMap<>(size);
        entityCosts = new HashMap<>(size);
        realEntities = new HashMap<>();
        playerNames = new HashMap<>();
        for (int i = 0 ; i < size ; i++) {
            String className = NetworkTools.readString(buf);
            int count = buf.readInt();
            int cost = buf.readInt();
            entities.put(className, count);
            entityCosts.put(className, cost);

            byte how = buf.readByte();
            if (how == ENTITY_NORMAL) {
                NBTTagCompound nbt = readNBT(buf);
                Entity entity = EntityList.createEntityFromNBT(nbt, RFTools.proxy.getClientWorld());
                realEntities.put(className, entity);
            } else if (how == ENTITY_PLAYER) {
                int entityId = buf.readInt();
                String entityName = NetworkTools.readString(buf);
                Entity entity = RFTools.proxy.getClientWorld().getEntityByID(entityId);
                if (entity != null) {
                    realEntities.put(className, entity);
                }
                playerNames.put(className, entityName);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blocks.size());
        for (Map.Entry<BlockMeta, Integer> entry : blocks.entrySet()) {
            BlockMeta bm = entry.getKey();
            Block block = bm.getBlock();
            buf.writeInt(Block.REGISTRY.getIDForObject(block));
            buf.writeByte(bm.getMeta());
            buf.writeInt(entry.getValue());
            buf.writeInt(costs.get(bm));
            if (stacks.containsKey(bm)) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stacks.get(bm));
            } else {
                buf.writeBoolean(false);
            }
        }
        buf.writeInt(entities.size());
        for (Map.Entry<String, Integer> entry : entities.entrySet()) {
            String name = entry.getKey();
            NetworkTools.writeString(buf, name);
            buf.writeInt(entry.getValue());
            buf.writeInt(entityCosts.get(name));
            if (realEntities.containsKey(name)) {
                Entity entity = realEntities.get(name);
                if (entity instanceof EntityPlayer) {
                    buf.writeByte(ENTITY_PLAYER);
                    int entityId = entity.getEntityId();
                    buf.writeInt(entityId);
                    NetworkTools.writeString(buf, entity.getDisplayName().getFormattedText());
                } else {
                    buf.writeByte(ENTITY_NORMAL);
                    NBTTagCompound nbt = entity.serializeNBT();
                    writeNBT(buf, nbt);
                }
            } else {
                buf.writeByte(ENTITY_NONE);
            }
        }
    }

    private static NBTTagCompound readNBT(ByteBuf dataIn) {
        PacketBuffer buf = new PacketBuffer(dataIn);
        try {
            return buf.readNBTTagCompoundFromBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeNBT(ByteBuf dataOut, NBTTagCompound nbt) {
        PacketBuffer buf = new PacketBuffer(dataOut);
        try {
            buf.writeNBTTagCompoundToBuffer(nbt);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public PacketChamberInfoReady() {
    }

    public PacketChamberInfoReady(Map<BlockMeta,Integer> blocks, Map<BlockMeta,Integer> costs,
                                  Map<BlockMeta,ItemStack> stacks,
                                  Map<String,Integer> entities, Map<String,Integer> entityCosts,
                                  Map<String,Entity> realEntities) {
        this.blocks = new HashMap<>(blocks);
        this.costs = new HashMap<>(costs);
        this.stacks = new HashMap<>(stacks);
        this.entities = new HashMap<>(entities);
        this.entityCosts = new HashMap<>(entityCosts);
        this.realEntities = new HashMap<>(realEntities);
    }

    public static class Handler implements IMessageHandler<PacketChamberInfoReady, IMessage> {
        @Override
        public IMessage onMessage(PacketChamberInfoReady message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketChamberInfoReady message, MessageContext ctx) {
            GuiChamberDetails.setItemsWithCount(message.blocks, message.costs, message.stacks,
                    message.entities, message.entityCosts, message.realEntities, message.playerNames);
        }
    }

}
