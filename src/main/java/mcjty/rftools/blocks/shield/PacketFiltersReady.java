package mcjty.rftools.blocks.shield;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.IClientCommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.shield.filters.AbstractShieldFilter;
import mcjty.rftools.blocks.shield.filters.ShieldFilter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PacketFiltersReady implements IMessage {

    public BlockPos pos;
    public List<ShieldFilter> list;
    public String command;

    public PacketFiltersReady() {
    }

    public PacketFiltersReady(ByteBuf buf) {
    }

    public PacketFiltersReady(BlockPos pos, String command, List<ShieldFilter> list) {
        this.pos = pos;
        this.command = command;
        this.list = new ArrayList<>();
        this.list.addAll(list);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        command = NetworkTools.readString(buf);

        int size = buf.readInt();
        if (size != -1) {
            list = new ArrayList<>(size);
            for (int i = 0 ; i < size ; i++) {
                ShieldFilter item = AbstractShieldFilter.createFilter(buf);
                list.add(item);
            }
        } else {
            list = null;
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);

        NetworkTools.writeString(buf, command);

        if (list == null) {
            buf.writeInt(-1);
        } else {
            buf.writeInt(list.size());
            for (ShieldFilter item : list) {
                item.toBytes(buf);
            }
        }
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(pos);
            if(!(te instanceof IClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            IClientCommandHandler clientCommandHandler = (IClientCommandHandler) te;
            if (!clientCommandHandler.receiveListFromServer(command, list, Type.create(ShieldFilter.class))) {
                Logging.log("Command " + command + " was not handled!");
            }
        });
        ctx.setPacketHandled(true);
    }
}
