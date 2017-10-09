package mcjty.rftools.blocks.shaper;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.RFTools;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketReturnLocatorEnergyConsumption implements IMessage {
    private int energy;

    @Override
    public void fromBytes(ByteBuf buf) {
        energy = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(energy);
    }

    public PacketReturnLocatorEnergyConsumption() {
    }

    public PacketReturnLocatorEnergyConsumption(int energy) {
        this.energy = energy;
    }

    public static class Handler implements IMessageHandler<PacketReturnLocatorEnergyConsumption, IMessage> {
        @Override
        public IMessage onMessage(PacketReturnLocatorEnergyConsumption message, MessageContext ctx) {
            RFTools.proxy.addScheduledTaskClient(() -> handle(message));
            return null;
        }

        private void handle(PacketReturnLocatorEnergyConsumption message) {
            GuiLocator.energyConsumption = message.energy;
        }
    }
}