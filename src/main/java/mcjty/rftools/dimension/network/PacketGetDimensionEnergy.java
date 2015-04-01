package mcjty.rftools.dimension.network;

import mcjty.rftools.dimension.DimensionStorage;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.world.World;

public class PacketGetDimensionEnergy implements IMessage,IMessageHandler<PacketGetDimensionEnergy, PacketReturnEnergy> {
    private int dimension;

    @Override
    public void fromBytes(ByteBuf buf) {
        dimension = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimension);
    }

    public PacketGetDimensionEnergy() {
    }

    public PacketGetDimensionEnergy(int dimension) {
        this.dimension = dimension;
    }

    @Override
    public PacketReturnEnergy onMessage(PacketGetDimensionEnergy message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(world);

        return new PacketReturnEnergy(message.dimension, dimensionStorage.getEnergyLevel(message.dimension));
    }

}