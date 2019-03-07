package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetPlayers implements IMessage {

    protected BlockPos pos;
    protected String command;
    protected TypedMap params;
    private String clientcmd;

    public PacketGetPlayers(ByteBuf buf) {
        fromBytes(buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        command = NetworkTools.readString(buf);
        params = TypedMapTools.readArguments(buf);
        clientcmd = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        NetworkTools.writeString(buf, command);
        TypedMapTools.writeArguments(buf, params);
        NetworkTools.writeString(buf, clientcmd);
    }

    public PacketGetPlayers() {
    }

    public PacketGetPlayers(BlockPos pos, String cmd, String clientcmd) {
        this.pos = pos;
        this.command = cmd;
        this.params = TypedMap.EMPTY;
        this.clientcmd = clientcmd;
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<String> list = commandHandler.executeWithResultList(command, params, Type.STRING);
            RFToolsMessages.INSTANCE.sendTo(new PacketPlayersReady(pos, clientcmd, list), ctx.getSender());
        });
        ctx.setPacketHandled(true);
    }
}
