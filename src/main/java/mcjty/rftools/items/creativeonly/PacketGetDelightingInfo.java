package mcjty.rftools.items.creativeonly;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketGetDelightingInfo implements IMessage {
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketGetDelightingInfo() {
    }

    public PacketGetDelightingInfo(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketGetDelightingInfo, IMessage> {
        @Override
        public IMessage onMessage(PacketGetDelightingInfo message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetDelightingInfo message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().player;
            World world = player.getEntityWorld();

            List<String> blockClasses = new ArrayList<>();
            List<String> teClasses = new ArrayList<>();
            Map<String,DelightingInfoHelper.NBTDescription> nbtData = new HashMap<>();

            int metadata = DelightingInfoHelper.fillDelightingData(message.pos.getX(), message.pos.getY(), message.pos.getZ(), world, blockClasses, teClasses, nbtData);
            RFToolsMessages.INSTANCE.sendTo(new PacketDelightingInfoReady(blockClasses, teClasses, nbtData, metadata), ctx.getServerHandler().player);
        }
    }

}