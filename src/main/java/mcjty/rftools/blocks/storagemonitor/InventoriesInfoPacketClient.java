package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.clientinfo.InfoPacketClient;
import net.minecraft.block.Block;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public class InventoriesInfoPacketClient implements InfoPacketClient {

    private List<InventoryInfo> inventories;

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

    @Override
    public void fromBytes(ByteBuf byteBuf) {
        int size = byteBuf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0 ; i < size ; i++) {
            BlockPos pos = NetworkTools.readPos(byteBuf);
            String name = NetworkTools.readString(byteBuf);
            boolean routable = byteBuf.readBoolean();
            Block block = null;
            if (byteBuf.readBoolean()) {
                block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(NetworkTools.readString(byteBuf)));
            }
            inventories.add(new InventoryInfo(pos, name, routable, block));
        }

    }

    @Override
    public void toBytes(ByteBuf byteBuf) {
        byteBuf.writeInt(inventories.size());
        for (InventoryInfo info : inventories) {
            NetworkTools.writePos(byteBuf, info.getPos());
            NetworkTools.writeString(byteBuf, info.getName());
            byteBuf.writeBoolean(info.isRoutable());
            if (info.getBlock() == null) {
                byteBuf.writeBoolean(false);
            } else {
                byteBuf.writeBoolean(true);
                String id = info.getBlock().getRegistryName().toString();
                NetworkTools.writeString(byteBuf, id);
            }
        }
    }

    public InventoriesInfoPacketClient() {
    }

    public InventoriesInfoPacketClient(List<InventoryInfo> inventories) {
        this.inventories = inventories;
    }

    @Override
    public void onMessageClient(EntityPlayerSP entityPlayerSP) {
        GuiStorageScanner.fromServer_inventories = inventories;
    }
}
