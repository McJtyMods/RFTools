package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

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
                stacks[i] = null;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            if (stack != null) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }

    }

    public PacketGridToServer() {
    }

    public PacketGridToServer(BlockPos pos, CraftingGrid grid) {
        init(pos, grid);
        stacks = new ItemStack[10];
        for (int i = 0 ; i < 10 ; i++) {
            stacks[i] = grid.getCraftingGridInventory().getStackInSlot(i);
        }
    }

    public static class Handler implements IMessageHandler<PacketGridToServer, IMessage> {
        @Override
        public IMessage onMessage(PacketGridToServer message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGridToServer message, MessageContext ctx) {
            CraftingGridProvider provider = message.handleMessage(ctx.getServerHandler().playerEntity.worldObj);
            if (provider != null) {
                CraftingGridInventory inventory = provider.getCraftingGrid().getCraftingGridInventory();
                for (int i = 0 ; i < 10 ; i++) {
                    inventory.setInventorySlotContents(i, message.stacks[i]);
                }
            }
        }
    }
}
