package mcjty.rftools.items.teleportprobe;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketForceTeleport implements IMessage {
    private BlockPos pos;
    private int dim;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        dim = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        buf.writeInt(dim);
    }

    public PacketForceTeleport() {
    }

    public PacketForceTeleport(BlockPos pos, int dim) {
        this.pos = pos;
        this.dim = dim;
    }

    public static class Handler implements IMessageHandler<PacketForceTeleport, IMessage> {
        @Override
        public IMessage onMessage(PacketForceTeleport message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketForceTeleport message, MessageContext ctx) {
            EntityPlayer player = ctx.getServerHandler().playerEntity;


            boolean probeInMainHand = player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof TeleportProbeItem;
            boolean probeInOffHand = player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() instanceof TeleportProbeItem;
            if ((!probeInMainHand) && (!probeInOffHand)) {
                return;
            }

            int x = message.pos.getX();
            int y = message.pos.getY();
            int z = message.pos.getZ();
            int currentId = player.worldObj.provider.getDimension();
            if (currentId != message.dim) {
                TeleportationTools.teleportToDimension(player, message.dim, x + .5, y + 1, z + .5);
            } else {
                player.setPositionAndUpdate(x+.5, y + 1.5, z+.5);
            }
        }
    }
}
