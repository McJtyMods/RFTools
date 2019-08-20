package mcjty.rftools.blocks.builder;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.items.builder.GuiChamberDetails;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PacketChamberInfoReady {
    private Map<BlockState,Integer> blocks;
    private Map<BlockState,Integer> costs;
    private Map<BlockState,ItemStack> stacks;
    private Map<String,Integer> entities;
    private Map<String,Integer> entityCosts;
    private Map<String,Entity> realEntities;
    private Map<String,String> playerNames;

    private static final byte ENTITY_NONE = 0;
    private static final byte ENTITY_NORMAL = 1;
    private static final byte ENTITY_PLAYER = 2;

    public void toBytes(PacketBuffer buf) {
        buf.writeInt(blocks.size());
        for (Map.Entry<BlockState, Integer> entry : blocks.entrySet()) {
            BlockState bm = entry.getKey();
            buf.writeInt(Block.getStateId(bm));
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
                if (entity instanceof PlayerEntity) {
                    buf.writeByte(ENTITY_PLAYER);
                    int entityId = entity.getEntityId();
                    buf.writeInt(entityId);
                    NetworkTools.writeString(buf, entity.getDisplayName().getFormattedText());
                } else {
                    buf.writeByte(ENTITY_NORMAL);
                    CompoundNBT nbt = entity.serializeNBT();
                    writeNBT(buf, nbt);
                }
            } else {
                buf.writeByte(ENTITY_NONE);
            }
        }
    }

    private static CompoundNBT readNBT(PacketBuffer buf) {
        return buf.readCompoundTag();
    }

    private static void writeNBT(PacketBuffer dataOut, CompoundNBT nbt) {
        PacketBuffer buf = new PacketBuffer(dataOut);
        try {
            buf.writeCompoundTag(nbt);
        } catch (Exception e) {
            Logging.logError("Error writing packet chamber info", e);
        }
    }


    public PacketChamberInfoReady() {
    }

    public PacketChamberInfoReady(PacketBuffer buf) {
        int size = buf.readInt();
        blocks = new HashMap<>(size);
        costs = new HashMap<>(size);
        stacks = new HashMap<>();
        for (int i = 0 ; i < size ; i++) {
            BlockState bm = Block.getStateById(buf.readInt());
            int count = buf.readInt();
            int cost = buf.readInt();
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
                CompoundNBT nbt = readNBT(buf);
                // @todo 1.14
//                EntityType<?> value = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(fixed));
//
//                entity = value.create(McJtyLib.proxy.getClientWorld(), nbt, null, null, new BlockPos(0, 0, 0), SpawnReason.COMMAND, false, false);
//
//                Entity entity = EntityList.createEntityFromNBT(nbt, RFTools.proxy.getClientWorld());
//                realEntities.put(className, entity);
            } else if (how == ENTITY_PLAYER) {
                int entityId = buf.readInt();
                String entityName = NetworkTools.readString(buf);
                Entity entity = McJtyLib.proxy.getClientWorld().getEntityByID(entityId);
                if (entity != null) {
                    realEntities.put(className, entity);
                }
                playerNames.put(className, entityName);
            }
        }
    }

    public PacketChamberInfoReady(Map<BlockState,Integer> blocks, Map<BlockState,Integer> costs,
                                  Map<BlockState,ItemStack> stacks,
                                  Map<String,Integer> entities, Map<String,Integer> entityCosts,
                                  Map<String,Entity> realEntities) {
        this.blocks = new HashMap<>(blocks);
        this.costs = new HashMap<>(costs);
        this.stacks = new HashMap<>(stacks);
        this.entities = new HashMap<>(entities);
        this.entityCosts = new HashMap<>(entityCosts);
        this.realEntities = new HashMap<>(realEntities);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            GuiChamberDetails.setItemsWithCount(blocks, costs, stacks,
                    entities, entityCosts, realEntities, playerNames);
        });
        ctx.setPacketHandled(true);
    }
}
