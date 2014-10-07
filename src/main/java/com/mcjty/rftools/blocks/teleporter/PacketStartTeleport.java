package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

public class PacketStartTeleport implements IMessage, IMessageHandler<PacketStartTeleport, IMessage> {
    private int x;
    private int y;
    private int z;
    private TeleportDestination destination;
    private String player;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();

        Coordinate c = new Coordinate(buf.readInt(), buf.readInt(), buf.readInt());
        int dim = buf.readInt();
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        destination = new TeleportDestination(c, dim);
        destination.setName(new String(dst));

        dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        player = new String(dst);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(destination.getCoordinate().getX());
        buf.writeInt(destination.getCoordinate().getY());
        buf.writeInt(destination.getCoordinate().getZ());
        buf.writeInt(destination.getDimension());
        buf.writeInt(destination.getName().length());
        buf.writeBytes(destination.getName().getBytes());
        buf.writeInt(player.length());
        buf.writeBytes(player.getBytes());
    }

    public PacketStartTeleport() {
    }

    public PacketStartTeleport(int x, int y, int z, TeleportDestination destination, String player) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.destination = destination;
        this.player = player;
    }

    @Override
    public IMessage onMessage(PacketStartTeleport message, MessageContext ctx) {
        TileEntity te = Minecraft.getMinecraft().theWorld.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof DialingDeviceTileEntity)) {
            // @Todo better logging
            System.out.println("createInventoryReadyPacket: TileEntity is not a DialingDeviceTileEntity!");
            return null;
        }
        DialingDeviceTileEntity dialingDeviceTileEntity = (DialingDeviceTileEntity) te;
        dialingDeviceTileEntity.startTeleport(message.destination, message.player);
        return null;
    }

}
