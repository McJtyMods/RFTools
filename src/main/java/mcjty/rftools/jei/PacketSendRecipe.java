package mcjty.rftools.jei;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.List;

public class PacketSendRecipe implements IMessage {
    private List<ItemStack> stacks;
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        int l = buf.readInt();
        stacks = new ArrayList<>(l);
        for (int i = 0 ; i < l ; i++) {
            if (buf.readBoolean()) {
                stacks.add(NetworkTools.readItemStack(buf));
            } else {
                stacks.add(null);
            }
        }
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(stacks.size());
        for (ItemStack stack : stacks) {
            if (stack != null) {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            } else {
                buf.writeBoolean(false);
            }
        }
        NetworkTools.writePos(buf, pos);
    }

    public PacketSendRecipe() {
    }

    public PacketSendRecipe(List<ItemStack> stacks, BlockPos pos) {
        this.stacks = stacks;
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketSendRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketSendRecipe message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSendRecipe message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            TileEntity te = world.getTileEntity(message.pos);
            if (te instanceof JEIRecipeAcceptor) {
                JEIRecipeAcceptor acceptor = (JEIRecipeAcceptor) te;
                acceptor.setRecipe(message.stacks);
            }
        }

    }
}