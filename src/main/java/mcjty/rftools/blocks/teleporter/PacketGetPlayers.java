package mcjty.rftools.blocks.teleporter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetPlayers extends PacketRequestListFromServer<PlayerName, PacketGetPlayers, PacketPlayersReady> {
    private String clientcmd;

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        clientcmd = NetworkTools.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        NetworkTools.writeString(buf, clientcmd);
    }

    public PacketGetPlayers() {
    }

    public PacketGetPlayers(BlockPos pos, String cmd, String clientcmd) {
        super(pos, cmd);
        this.clientcmd = clientcmd;
    }

    @Override
    public PacketPlayersReady onMessage(PacketGetPlayers message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
        if(!(te instanceof CommandHandler)) {
            Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
            return null;
        }
        CommandHandler commandHandler = (CommandHandler) te;
        List<PlayerName> list = (List<PlayerName>) commandHandler.executeWithResultList(message.command, message.args);
        if (list == null) {
            Logging.log("Command " + message.command + " was not handled!");
            return null;
        }
        return new PacketPlayersReady(message.pos, message.clientcmd, list);
    }

    @Override
    protected PacketPlayersReady createMessageToClient(BlockPos pos, List<PlayerName> result) {
        throw new RuntimeException("Something is wrong. Code should never come here!");
    }
}
