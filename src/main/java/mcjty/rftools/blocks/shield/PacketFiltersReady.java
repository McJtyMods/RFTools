package mcjty.rftools.blocks.shield;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.ClientCommandHandler;
import mcjty.lib.network.PacketListFromServer;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.shield.filters.AbstractShieldFilter;
import mcjty.rftools.blocks.shield.filters.ShieldFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.List;

public class PacketFiltersReady extends PacketListFromServer<PacketFiltersReady,ShieldFilter> {

    public PacketFiltersReady() {
    }

    public PacketFiltersReady(BlockPos pos, String command, List<ShieldFilter> list) {
        super(pos, command, list);
    }

    public static class Handler implements IMessageHandler<PacketFiltersReady, IMessage> {
        @Override
        public IMessage onMessage(PacketFiltersReady message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketFiltersReady message, MessageContext ctx) {
            TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.pos);
            if(!(te instanceof ClientCommandHandler)) {
                Logging.log("createInventoryReadyPacket: TileEntity is not a ClientCommandHandler!");
                return;
            }
            ClientCommandHandler clientCommandHandler = (ClientCommandHandler) te;
            if (!clientCommandHandler.execute(message.command, message.list)) {
                Logging.log("Command " + message.command + " was not handled!");
            }
        }
    }

    @Override
    protected ShieldFilter createItem(ByteBuf buf) {
        return AbstractShieldFilter.createFilter(buf);
    }
}
