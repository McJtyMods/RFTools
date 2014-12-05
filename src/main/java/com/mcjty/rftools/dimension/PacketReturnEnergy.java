package com.mcjty.rftools.dimension;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class PacketReturnEnergy implements IMessage {
    int id;
    int energy;

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
}