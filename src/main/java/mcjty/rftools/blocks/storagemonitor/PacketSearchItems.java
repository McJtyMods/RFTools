package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.network.Argument;
import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.monitor.BlockPosNet;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketSearchItems extends PacketRequestListFromServer<BlockPosNet, PacketSearchItems, PacketSearchReady> {

    public PacketSearchItems() {
    }

    public PacketSearchItems(BlockPos pos, String search) {
        super(RFTools.MODID, pos, StorageScannerTileEntity.CMD_STARTSEARCH, new Argument("search", search));
    }

    public static class Handler implements IMessageHandler<PacketSearchItems, IMessage> {
        @Override
        public IMessage onMessage(PacketSearchItems message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSearchItems message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.pos);
            if(!(te instanceof CommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            CommandHandler commandHandler = (CommandHandler) te;
            List<BlockPos> list = (List<BlockPos>) commandHandler.executeWithResultList(message.command, message.args);
            if (list == null) {
                Logging.log("Command " + message.command + " was not handled!");
                return;
            }
            RFToolsMessages.INSTANCE.sendTo(new PacketSearchReady(message.pos, StorageScannerTileEntity.CLIENTCMD_SEARCHREADY, list), ctx.getServerHandler().playerEntity);
        }
    }

}
