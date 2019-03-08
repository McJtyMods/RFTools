package mcjty.rftools.blocks.storage;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.rftools.RFTools;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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
                items.add(Pair.of(-slotIdx-1, ItemStack.EMPTY));
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
            if (pair.getRight().isEmpty()) {
                buf.writeInt(-pair.getLeft()-1);  // Negative index to indicate an empty stack
            } else {
                buf.writeInt(pair.getLeft());
                NetworkTools.writeItemStack(buf, pair.getRight());
            }
        }
    }

    public PacketSyncSlotsToClient() {
    }

    public PacketSyncSlotsToClient(ByteBuf buf) {
        fromBytes(buf);
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

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storage = (ModularStorageTileEntity) te;
                storage.syncInventoryFromServer(maxSize, numStacks, sortMode, viewMode, groupMode, filter);
                Container container = RFTools.proxy.getClientPlayer().openContainer;
                if (container instanceof ModularStorageContainer) {
                    for (Pair<Integer, ItemStack> pair : items) {
                        container.putStackInSlot(pair.getLeft(), pair.getRight());
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
