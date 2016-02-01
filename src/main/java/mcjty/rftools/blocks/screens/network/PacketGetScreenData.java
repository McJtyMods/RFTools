package mcjty.rftools.blocks.screens.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.screens.ScreenTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.Map;

public class PacketGetScreenData implements IMessage {
    private String modid;
    private GlobalCoordinate pos;
    private long millis;

    @Override
    public void fromBytes(ByteBuf buf) {
        modid = NetworkTools.readString(buf);
        pos = new GlobalCoordinate(NetworkTools.readPos(buf), buf.readInt());
        millis = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writeString(buf, modid);
        NetworkTools.writePos(buf, pos.getCoordinate());
        buf.writeInt(pos.getDimension());
        buf.writeLong(millis);
    }

    public PacketGetScreenData() {
    }

    public PacketGetScreenData(String modid, GlobalCoordinate pos, long millis) {
        this.modid = modid;
        this.pos = pos;
        this.millis = millis;
    }

    public static class Handler implements IMessageHandler<PacketGetScreenData, IMessage> {
        @Override
        public IMessage onMessage(PacketGetScreenData message, MessageContext ctx) {
            MinecraftServer.getServer().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetScreenData message, MessageContext ctx) {
            World world = ctx.getServerHandler().playerEntity.worldObj;
            if (message.pos.getDimension() != world.provider.getDimensionId()) {
                return;
            }
            TileEntity te = world.getTileEntity(message.pos.getCoordinate());
            if(!(te instanceof ScreenTileEntity)) {
                Logging.logError("PacketGetScreenData: TileEntity is not a SimpleScreenTileEntity!");
                return;
            }
            Map<Integer, Object[]> screenData = ((ScreenTileEntity) te).getScreenData(message.millis);

            SimpleNetworkWrapper wrapper = PacketHandler.modNetworking.get(message.modid);
            PacketReturnScreenData msg = new PacketReturnScreenData(message.pos, screenData);
            wrapper.sendTo(msg, ctx.getServerHandler().playerEntity);
        }

    }
}