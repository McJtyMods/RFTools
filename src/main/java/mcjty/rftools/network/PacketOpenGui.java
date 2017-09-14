package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.container.GenericBlock;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketOpenGui implements IMessage {

    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketOpenGui() {
    }

    public PacketOpenGui(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketOpenGui, IMessage> {
        @Override
        public IMessage onMessage(PacketOpenGui message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketOpenGui message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            World world = player.getEntityWorld();
            Block block = world.getBlockState(message.pos).getBlock();
            if (block instanceof GenericBlock) {
                player.openGui(RFTools.instance, ((GenericBlock) block).getGuiID(), world,
                        message.pos.getX(), message.pos.getY(), message.pos.getZ());
            }
        }
    }

}