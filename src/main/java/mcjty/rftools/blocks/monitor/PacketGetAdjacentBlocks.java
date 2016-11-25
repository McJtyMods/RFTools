package mcjty.rftools.blocks.monitor;

import mcjty.lib.network.CommandHandler;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetAdjacentBlocks extends PacketRequestListFromServer<BlockPosNet, PacketGetAdjacentBlocks, PacketAdjacentBlocksReady> {

    public PacketGetAdjacentBlocks() {

    }

    public PacketGetAdjacentBlocks(BlockPos pos) {
        super(RFTools.MODID, pos, RFMonitorBlockTileEntity.CMD_GETADJACENTBLOCKS);
    }

    public static class Handler implements IMessageHandler<PacketGetAdjacentBlocks, IMessage> {
        @Override
        public IMessage onMessage(PacketGetAdjacentBlocks message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetAdjacentBlocks message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().playerEntity.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof CommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            CommandHandler commandHandler = (CommandHandler) te;
            List<BlockPos> list = commandHandler.executeWithResultList(message.command, message.args, Type.create(BlockPos.class));
            RFToolsMessages.INSTANCE.sendTo(new PacketAdjacentBlocksReady(message.pos, RFMonitorBlockTileEntity.CLIENTCMD_ADJACENTBLOCKSREADY, list), ctx.getServerHandler().playerEntity);
        }
    }

}
