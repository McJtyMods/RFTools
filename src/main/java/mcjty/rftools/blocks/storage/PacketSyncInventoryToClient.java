package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.theoneprobe.network.NetworkTools;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncInventoryToClient implements IMessage {

    private BlockPos pos;
    private int maxSize;
    private int numStacks;
    private List<ItemStack> items;


    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        maxSize = buf.readInt();
        numStacks = buf.readInt();
        int s = buf.readInt();
        items = new ArrayList<>(s);
        for (int i = 0 ; i < s ; i++) {
            boolean b = buf.readBoolean();
            if (b) {
                items.add(NetworkTools.readItemStack(buf));
            } else {
                items.add(null);
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(maxSize);
        buf.writeInt(numStacks);
        buf.writeInt(items.size());
        for (ItemStack item : items) {
            if (item != null) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, item);
            } else {
                buf.writeBoolean(false);
            }
        }
    }

    public PacketSyncInventoryToClient() {
    }

    public PacketSyncInventoryToClient(BlockPos pos, int maxSize, int numStacks, List<ItemStack> items) {
        this.pos = pos;
        this.maxSize = maxSize;
        this.numStacks = numStacks;
        this.items = items;
    }

    public static class Handler implements IMessageHandler<PacketSyncInventoryToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncInventoryToClient message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketSyncInventoryToClient message, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(message.maxSize, message.numStacks);
                Container container = Minecraft.getMinecraft().thePlayer.openContainer;
                if (container instanceof ModularStorageContainer) {
                    for (int i = 0; i < message.items.size(); i++) {
                        container.putStackInSlot(i, message.items.get(i));
                    }
                }
            }
        }

    }

}
