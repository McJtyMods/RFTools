package mcjty.rftools.blocks.teleporter;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketReceiversReady {

    public BlockPos pos;
    public List<TeleportDestinationClientInfo> list;
    public String command;

    public PacketReceiversReady() {
    }

    public PacketReceiversReady(PacketBuffer buf) {
        pos = NetworkTools.readPos(buf);
        command = NetworkTools.readString(buf);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                TeleportDestinationClientInfo item = new TeleportDestinationClientInfo(buf);
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    public PacketReceiversReady(BlockPos pos, String command, List<TeleportDestinationClientInfo> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    public void toBytes(PacketBuffer buf) {
        NetworkTools.writePos(buf, pos);
        NetworkTools.writeString(buf, command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (TeleportDestinationClientInfo item : list) {
                item.toBytes(buf);
            }
        }
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
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(TeleportDestinationClientInfo.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
