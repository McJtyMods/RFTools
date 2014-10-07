package com.mcjty.rftools.blocks.teleporter;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketSetReceiverName implements IMessage, IMessageHandler<PacketSetReceiverName, IMessage> {
    private int x;
    private int y;
    private int z;
    private String name;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        byte[] dst = new byte[buf.readInt()];
        buf.readBytes(dst);
        name = new String(dst);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(name.length());
        buf.writeBytes(name.getBytes());
    }

    public PacketSetReceiverName() {
    }

    public PacketSetReceiverName(int x, int y, int z, String name) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
    }

    @Override
    public IMessage onMessage(PacketSetReceiverName message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof MatterReceiverTileEntity)) {
            // @Todo better logging
            System.out.println("createGetInventoryPacket: TileEntity is not a MatterReceiverTileEntity!");
            return null;
        }
        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
        matterReceiverTileEntity.setName(message.name);
        return null;
    }

}
