package mcjty.rftools.blocks.storagemonitor;


import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketReturnInventoryInfo implements IMessage {

    private List<InventoryInfo> inventories;

    public List<InventoryInfo> getInventories() {
        return inventories;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = NetworkTools.readPos(buf);
            String name = NetworkTools.readString(buf);
            boolean routable = buf.readBoolean();
            Block block = null;
            if (buf.readBoolean()) {
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(NetworkTools.readString(buf)));
            }
            inventories.add(new InventoryInfo(pos, name, routable, block));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(inventories.size());
        for (InventoryInfo info : inventories) {
            NetworkTools.writePos(buf, info.getPos());
            NetworkTools.writeString(buf, info.getName());
            buf.writeBoolean(info.isRoutable());
            if (info.getBlock() == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                String id = info.getBlock().getRegistryName().toString();
                NetworkTools.writeString(buf, id);
            }
        }
    }

    public PacketReturnInventoryInfo() {
    }

    public PacketReturnInventoryInfo(ByteBuf buf) {
    }

    public PacketReturnInventoryInfo(List<InventoryInfo> inventories) {
        this.inventories = inventories;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ReturnInfoHelper.onMessageFromServer(this);
        });
        ctx.setPacketHandled(true);
    }

    public static class InventoryInfo {
        private final BlockPos pos;
        private final String name;
        private final boolean routable;
        private final Block block;

        public InventoryInfo(BlockPos pos, String name, boolean routable, Block block) {
            this.pos = pos;
            this.name = name;
            this.routable = routable;
            this.block = block;
        }

        public BlockPos getPos() {
            return pos;
        }

        public String getName() {
            return name;
        }

        public boolean isRoutable() {
            return routable;
        }

        public Block getBlock() {
            return block;
        }
    }
}