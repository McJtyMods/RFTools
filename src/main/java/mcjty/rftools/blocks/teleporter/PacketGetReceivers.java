package mcjty.rftools.blocks.teleporter;

import mcjty.lib.network.Argument;
import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.List;

public class PacketGetReceivers extends PacketRequestListFromServer<TeleportDestinationClientInfo, PacketGetReceivers, PacketReceiversReady> {

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(BlockPos pos, String playerName) {
        super(RFTools.MODID, pos, DialingDeviceTileEntity.CMD_GETRECEIVERS, new Argument("player", playerName));
    }

    public static class Handler implements IMessageHandler<PacketGetReceivers, IMessage> {
        @Override
        public IMessage onMessage(PacketGetReceivers message, MessageContext ctx) {
            MinecraftServer.getServer().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetReceivers message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
            if(!(te instanceof CommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            CommandHandler commandHandler = (CommandHandler) te;
            List<TeleportDestinationClientInfo> list = (List<TeleportDestinationClientInfo>) commandHandler.executeWithResultList(message.command, message.args);
            if (list == null) {
                Logging.log("Command " + message.command + " was not handled!");
                return;
            }
            SimpleNetworkWrapper wrapper = PacketHandler.modNetworking.get(message.modid);
            PacketReceiversReady msg = new PacketReceiversReady(message.pos, DialingDeviceTileEntity.CLIENTCMD_GETRECEIVERS, list);
            wrapper.sendTo(msg, ctx.getServerHandler().playerEntity);
        }

    }
}
