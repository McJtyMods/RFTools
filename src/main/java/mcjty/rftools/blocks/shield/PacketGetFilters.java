package mcjty.rftools.blocks.shield;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ICommandHandler;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.network.TypedMapTools;
import mcjty.lib.thirteen.Context;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.shield.filters.ShieldFilter;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetFilters implements IMessage {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetFilters() {
    }

    public PacketGetFilters(ByteBuf buf) {
        fromBytes(buf);
    }

    public PacketGetFilters(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        params = TypedMapTools.readArguments(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<Context> supplier) {
        Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof ICommandHandler)) {
                Logging.log("createStartScanPacket: TileEntity is not a CommandHandler!");
                return;
            }
            ICommandHandler commandHandler = (ICommandHandler) te;
            List<ShieldFilter> list = commandHandler.executeWithResultList(ShieldTEBase.CMD_GETFILTERS, params, Type.create(ShieldFilter.class));
            RFToolsMessages.INSTANCE.sendTo(new PacketFiltersReady(pos, ShieldTEBase.CLIENTCMD_GETFILTERS, list), ctx.getSender());
        });
        ctx.setPacketHandled(true);
    }
}
