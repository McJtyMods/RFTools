package mcjty.rftools.network;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetPlayers {

    protected BlockPos pos;
    protected String command;
    protected TypedMap params;
    private String clientcmd;

    public PacketGetPlayers(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        command = NetworkTools.readString(buf);
        params = TypedMapTools.readArguments(buf);
        clientcmd = NetworkTools.readString(buf);
    }

    public void toBytes(PacketBuffer buf) {
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

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<String> list = commandHandler.executeWithResultList(command, params, Type.STRING);
            RFToolsMessages.INSTANCE.sendTo(new PacketPlayersReady(pos, clientcmd, list),
                    ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}
