package mcjty.rftools.craftinggrid;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.tools.ItemStackTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
                stacks[i] = ItemStackTools.getEmptyStack();
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        convertToBytes(buf);
        buf.writeInt(stacks.length);
        for (ItemStack stack : stacks) {
            if (ItemStackTools.isValid(stack)) {
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
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            World world = player.getEntityWorld();
            CraftingGridProvider provider = message.handleMessage(world, player);
            if (provider != null) {
                CraftingGridInventory inventory = provider.getCraftingGrid().getCraftingGridInventory();
                for (int i = 0 ; i < 10 ; i++) {
                    inventory.setInventorySlotContents(i, message.stacks[i]);
                }
                provider.markInventoryDirty();
            }
        }
    }
}
