package mcjty.rftools.blocks.storagemonitor;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketInventoryReady implements IMessage {
    private BlockPos pos;
    private List<ItemStack> items;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        int size = buf.readInt();
        PacketBuffer buffer = new PacketBuffer(buf);
        items = new ArrayList<ItemStack>();
        for (int i = 0 ; i < size ; i++) {
            try {
                items.add(buffer.readItemStackFromBuffer());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(items.size());
        PacketBuffer buffer = new PacketBuffer(buf);
        for (ItemStack itemStack : items) {
            buffer.writeItemStackToBuffer(itemStack);
        }
    }

    public PacketInventoryReady() {
    }

    public PacketInventoryReady(BlockPos pos, List<ItemStack> items) {
        this.pos = pos;
        this.items = new ArrayList<>();
        this.items.addAll(items);
    }

    public static class Handler implements IMessageHandler<PacketInventoryReady, IMessage> {
        @Override
        public IMessage onMessage(PacketInventoryReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketInventoryReady message, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
            if(!(te instanceof StorageScannerTileEntity)) {
                // @Todo better logging
                System.out.println("createInventoryReadyPacket: TileEntity is not a StorageScannerTileEntity!");
                return;
            }
            StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
            storageScannerTileEntity.storeItemsForClient(message.items);
        }
    }

}
