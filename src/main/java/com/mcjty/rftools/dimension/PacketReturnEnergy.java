package com.mcjty.rftools.dimension;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketReturnEnergy implements IMessage,IMessageHandler<PacketReturnEnergy, IMessage> {
    private int id;
    private int energy;

    @Override
    public void fromBytes(ByteBuf buf) {
        id = buf.readInt();
        energy = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(id);
        buf.writeInt(energy);
    }

    public PacketReturnEnergy() {
    }

    public PacketReturnEnergy(int id, int energy) {
        this.id = id;
        this.energy = energy;
    }

    @Override
    public IMessage onMessage(PacketReturnEnergy message, MessageContext ctx) {
        World world = Minecraft.getMinecraft().theWorld;
        DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(world);
        dimensionStorage.setEnergyLevel(message.id, message.energy);
        return null;
    }

}