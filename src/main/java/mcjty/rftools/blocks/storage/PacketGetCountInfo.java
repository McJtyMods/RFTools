package mcjty.rftools.blocks.storage;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class PacketGetCountInfo implements IMessage,IMessageHandler<PacketGetCountInfo, PacketReturnCountInfo> {
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

    public PacketGetCountInfo() {
    }

    public PacketGetCountInfo(int dim, int x, int y, int z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PacketReturnCountInfo onMessage(PacketGetCountInfo message, MessageContext ctx) {
        WorldServer world = DimensionManager.getWorld(message.dim);
        int cnt = -1;
        if (world != null) {
            TileEntity te = world.getTileEntity(message.x, message.y, message.z);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
                cnt = modularStorageTileEntity.getNumStacks();
            }
        }
        return new PacketReturnCountInfo(cnt);
    }

}