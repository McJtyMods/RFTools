package mcjty.rftools.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.lib.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetPlayers extends PacketRequestListFromServer<String, PacketGetPlayers, PacketPlayersReady> {
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
        super(RFTools.MODID, pos, cmd);
        this.clientcmd = clientcmd;
    }

    public static class Handler implements IMessageHandler<PacketGetPlayers, IMessage> {
        @Override
        public IMessage onMessage(PacketGetPlayers message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetPlayers message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof CommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            CommandHandler commandHandler = (CommandHandler) te;
            List<String> list = commandHandler.executeWithResultList(message.command, message.args, Type.STRING);
            RFToolsMessages.INSTANCE.sendTo(new PacketPlayersReady(message.pos, message.clientcmd, list), ctx.getServerHandler().player);
        }
    }
}
