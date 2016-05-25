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
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class PacketSyncSlotsToClient implements IMessage {

    private BlockPos pos;
    private int maxSize;
    private int numStacks;
    private String viewMode;
    private String sortMode;
    private boolean groupMode;
    private String filter;
    private List<Pair<Integer,ItemStack>> items;


    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        viewMode = NetworkTools.readString(buf);
        sortMode = NetworkTools.readString(buf);
        groupMode = buf.readBoolean();
        filter = NetworkTools.readString(buf);
        maxSize = buf.readInt();
        numStacks = buf.readInt();
        int s = buf.readInt();
        items = new ArrayList<>(s);
        for (int i = 0 ; i < s ; i++) {
            int slotIdx = buf.readInt();
            if (slotIdx < 0) {
                items.add(Pair.of(-slotIdx-1, null));
            } else {
                ItemStack stack = NetworkTools.readItemStack(buf);
                items.add(Pair.of(slotIdx, stack));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        NetworkTools.writeString(buf, viewMode);
        NetworkTools.writeString(buf, sortMode);
        buf.writeBoolean(groupMode);
        NetworkTools.writeString(buf, filter);
        buf.writeInt(maxSize);
        buf.writeInt(numStacks);
        buf.writeInt(items.size());
        for (Pair<Integer, ItemStack> pair : items) {
            if (pair.getRight() == null) {
                buf.writeInt(-pair.getLeft()-1);  // Negative index to indicate a null stack
            } else {
                buf.writeInt(pair.getLeft());
                NetworkTools.writeItemStack(buf, pair.getRight());
            }
        }
    }

    public PacketSyncSlotsToClient() {
    }

    public PacketSyncSlotsToClient(BlockPos pos,
                                   String sortMode, String viewMode, boolean groupMode, String filter,
                                   int maxSize, int numStacks, List<Pair<Integer,ItemStack>> items) {
        this.sortMode = sortMode;
        this.viewMode = viewMode;
        this.groupMode = groupMode;
        this.filter = filter;
        this.pos = pos;
        this.maxSize = maxSize;
        this.numStacks = numStacks;
        this.items = items;
    }

    public static class Handler implements IMessageHandler<PacketSyncSlotsToClient, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncSlotsToClient message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        public void handle(PacketSyncSlotsToClient message, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(message.maxSize, message.numStacks, message.sortMode, message.viewMode, message.groupMode, message.filter);
                Container container = Minecraft.getMinecraft().thePlayer.openContainer;
                if (container instanceof ModularStorageContainer) {
                    for (Pair<Integer, ItemStack> pair : message.items) {
                        container.putStackInSlot(pair.getLeft(), pair.getRight());
                    }
                }
            }
        }

    }

}
