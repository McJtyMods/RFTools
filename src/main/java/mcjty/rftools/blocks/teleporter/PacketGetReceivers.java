package mcjty.rftools.blocks.teleporter;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.PacketHandler;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

import java.util.List;

import static mcjty.rftools.blocks.teleporter.DialingDeviceTileEntity.PARAM_PLAYER;

public class PacketGetReceivers extends PacketRequestListFromServer<TeleportDestinationClientInfo, PacketGetReceivers, PacketReceiversReady> {

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(BlockPos pos, String playerName) {
        super(RFTools.MODID, pos, DialingDeviceTileEntity.CMD_GETRECEIVERS, TypedMap.builder().put(PARAM_PLAYER, playerName).build());
    }

    public static class Handler implements IMessageHandler<PacketGetReceivers, IMessage> {
        @Override
        public IMessage onMessage(PacketGetReceivers message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetReceivers message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<TeleportDestinationClientInfo> list = commandHandler.executeWithResultList(message.command, message.params, Type.create(TeleportDestinationClientInfo.class));
            SimpleNetworkWrapper wrapper = PacketHandler.modNetworking.get(message.modid);
            PacketReceiversReady msg = new PacketReceiversReady(message.pos, DialingDeviceTileEntity.CLIENTCMD_GETRECEIVERS, list);
            wrapper.sendTo(msg, ctx.getServerHandler().player);
        }

    }
}
