package mcjty.rftools.blocks.monitor;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketAdjacentTankBlocksReady {

    public BlockPos pos;
    public List<BlockPos> list;
    public String command;

    public PacketAdjacentTankBlocksReady() {
    }

    public PacketAdjacentTankBlocksReady(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        command = NetworkTools.readString(buf);
        list = NetworkTools.readPosList(buf);
    }

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
        NetworkTools.writeString(buf, command);
        NetworkTools.writePosList(buf, list);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = McJtyLib.proxy.getClientWorld().getTileEntity(pos);
            if (!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(BlockPos.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
