package mcjty.rftools.items.teleportprobe;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import net.minecraft.entity.player.EntityPlayer;

public class PacketForceTeleport implements IMessage, IMessageHandler<PacketForceTeleport, IMessage> {
    private int x;
    private int y;
    private int z;
    private int dim;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        dim = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(dim);
    }

    public PacketForceTeleport() {
    }

    public PacketForceTeleport(int x, int y, int z, int dim) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
    }

    @Override
    public IMessage onMessage(PacketForceTeleport message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;

        int currentId = player.worldObj.provider.dimensionId;
        if (currentId != message.dim) {
            TeleportationTools.teleportToDimension(player, message.dim, message.x + .5, message.y + 1, message.z + .5);
        } else {
            player.setPositionAndUpdate(message.x+.5, message.y + 1.5, message.z+.5);
        }
        return null;
    }

}
