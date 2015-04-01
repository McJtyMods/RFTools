package mcjty.rftools.blocks.storagemonitor;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PacketInventoryReady implements IMessage, IMessageHandler<PacketInventoryReady, IMessage> {
    private int x;
    private int y;
    private int z;
    private List<ItemStack> items;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
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
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(items.size());
        PacketBuffer buffer = new PacketBuffer(buf);
        for (ItemStack itemStack : items) {
            try {
                buffer.writeItemStackToBuffer(itemStack);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public PacketInventoryReady() {
    }

    public PacketInventoryReady(int x, int y, int z, List<ItemStack> items) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.items = new ArrayList<ItemStack>();
        this.items.addAll(items);
    }

    @Override
    public IMessage onMessage(PacketInventoryReady message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof StorageScannerTileEntity)) {
            // @Todo better logging
            System.out.println("createInventoryReadyPacket: TileEntity is not a StorageScannerTileEntity!");
            return null;
        }
        StorageScannerTileEntity storageScannerTileEntity = (StorageScannerTileEntity) te;
        storageScannerTileEntity.storeItemsForClient(message.items);
        return null;
    }

}
