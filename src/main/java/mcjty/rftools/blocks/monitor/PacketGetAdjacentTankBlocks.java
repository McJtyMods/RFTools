package mcjty.rftools.blocks.monitor;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.PacketRequestListFromServer;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.lib.typed.Type;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketGetAdjacentTankBlocks extends PacketRequestListFromServer<BlockPos, PacketGetAdjacentTankBlocks, PacketAdjacentTankBlocksReady> {

    public PacketGetAdjacentTankBlocks() {

    }

    public PacketGetAdjacentTankBlocks(BlockPos pos) {
        super(RFTools.MODID, pos, LiquidMonitorBlockTileEntity.CMD_GETADJACENTBLOCKS, TypedMap.EMPTY);
    }

    public static class Handler implements IMessageHandler<PacketGetAdjacentTankBlocks, IMessage> {
        @Override
        public IMessage onMessage(PacketGetAdjacentTankBlocks message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketGetAdjacentTankBlocks message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<BlockPos> list = commandHandler.executeWithResultList(message.command, message.params, Type.create(BlockPos.class));
            RFToolsMessages.INSTANCE.sendTo(new PacketAdjacentBlocksReady(message.pos, RFMonitorBlockTileEntity.CLIENTCMD_ADJACENTBLOCKSREADY, list), ctx.getServerHandler().player);
        }
    }
}
