package mcjty.rftools.blocks.shaper;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRequestLocatorEnergyConsumption implements IMessage {
    private BlockPos pos;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetworkTools.writePos(buf, pos);
    }

    public PacketRequestLocatorEnergyConsumption() {
    }

    public PacketRequestLocatorEnergyConsumption(BlockPos pos) {
        this.pos = pos;
    }

    public static class Handler implements IMessageHandler<PacketRequestLocatorEnergyConsumption, IMessage> {
        @Override
        public IMessage onMessage(PacketRequestLocatorEnergyConsumption message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketRequestLocatorEnergyConsumption message, MessageContext ctx) {
            World world = ctx.getServerHandler().player.getEntityWorld();
            TileEntity te = world.getTileEntity(message.pos);
            if (te instanceof LocatorTileEntity) {
                int energy = ((LocatorTileEntity) te).getEnergyPerScan();
                RFToolsMessages.INSTANCE.sendTo(new PacketReturnLocatorEnergyConsumption(energy), ctx.getServerHandler().player);
            }
        }
    }


}
