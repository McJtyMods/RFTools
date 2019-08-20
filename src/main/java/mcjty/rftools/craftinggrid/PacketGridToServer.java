package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.function.Supplier;

public class PacketGridToServer extends PacketGridSync implements IMessage {

    private ItemStack[] stacks = new ItemStack[0];

    @Override
    public void fromBytes(ByteBuf buf) {
        convertFromBytes(buf);
        int len = buf.readInt();
        stacks = new ItemStack[len];
        for (int i = 0 ; i < len ; i++) {
            if (buf.readBoolean()) {
                stacks[i] = NetworkTools.readItemStack(buf);
            } else {
                stacks[i] = ItemStack.EMPTY;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }

    }

    public PacketGridToServer() {
    }

    public PacketGridToServer(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGridToServer(BlockPos pos, CraftingGrid grid) {
        init(pos, grid);
        stacks = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            stacks[i] = grid.getCraftingGridInventory().getStackInSlot(i);
        }
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();
            World world = player.getEntityWorld();
            CraftingGridProvider provider = handleMessage(world, player);
            if (provider != null) {
                CraftingGridInventory inventory = provider.getCraftingGrid().getCraftingGridInventory();
                for (int i = 0 ; i < 10 ; i++) {
                    inventory.setInventorySlotContents(i, stacks[i]);
                }
                provider.markInventoryDirty();
            }
        });
        ctx.setPacketHandled(true);
    }
}
