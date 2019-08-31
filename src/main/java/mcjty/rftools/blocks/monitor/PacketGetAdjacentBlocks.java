package mcjty.rftools.blocks.monitor;

import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetAdjacentBlocks {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetAdjacentBlocks() {
    }

    public PacketGetAdjacentBlocks(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetAdjacentBlocks(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
        TypedMapTools.writeArguments(buf, params);
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
            List<BlockPos> list = commandHandler.executeWithResultList(RFMonitorBlockTileEntity.CMD_GETADJACENTBLOCKS, params, Type.create(BlockPos.class));
            RFToolsMessages.INSTANCE.sendTo(new PacketAdjacentBlocksReady(pos, RFMonitorBlockTileEntity.CLIENTCMD_ADJACENTBLOCKSREADY, list),
                    ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }

}
