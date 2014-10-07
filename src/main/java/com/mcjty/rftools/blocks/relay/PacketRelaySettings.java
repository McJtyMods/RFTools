package com.mcjty.rftools.blocks.relay;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketRelaySettings implements IMessage, IMessageHandler<PacketRelaySettings, IMessage> {
    private int x;
    private int y;
    private int z;

    private int rfOn;
    private int rfOff;

    public PacketRelaySettings() {
    }

    public PacketRelaySettings(int x, int y, int z, int rfOn, int rfOff) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.rfOn = rfOn;
        this.rfOff = rfOff;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        rfOn = buf.readInt();
        rfOff = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(rfOn);
        buf.writeInt(rfOff);
    }

    @Override
    public IMessage onMessage(PacketRelaySettings message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof RelayTileEntity)) {
            // @Todo better logging
            System.out.println("createSettingsPacket: TileEntity is not a RelayTileEntity!");
            return null;
        }
        RelayTileEntity relayTileEntity = (RelayTileEntity) te;
        relayTileEntity.setRfOn(message.rfOn);
        relayTileEntity.setRfOff(message.rfOff);
        return null;
    }
}
