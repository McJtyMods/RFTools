package mcjty.rftools.blocks.teleporter;

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

import static mcjty.rftools.blocks.teleporter.DialingDeviceTileEntity.PARAM_PLAYER;

public class PacketGetReceivers {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetReceivers() {
    }

    public PacketGetReceivers(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetReceivers(BlockPos pos, String playerName) {
        this.pos = pos;
        this.params = TypedMap.builder().put(PARAM_PLAYER, playerName).build();
    }

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if (!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<TeleportDestinationClientInfo> list = commandHandler.executeWithResultList(DialingDeviceTileEntity.CMD_GETRECEIVERS, params, Type.create(TeleportDestinationClientInfo.class));
            PacketReceiversReady msg = new PacketReceiversReady(pos, DialingDeviceTileEntity.CLIENTCMD_GETRECEIVERS, list);
            RFToolsMessages.INSTANCE.sendTo(msg, ctx.getSender().connection.netManager, NetworkDirection.PLAY_TO_CLIENT);
        });
        ctx.setPacketHandled(true);
    }
}