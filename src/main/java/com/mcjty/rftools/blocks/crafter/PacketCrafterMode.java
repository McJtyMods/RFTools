package com.mcjty.rftools.blocks.crafter;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketCrafterMode implements IMessage, IMessageHandler<PacketCrafterMode, IMessage> {
    private int x;
    private int y;
    private int z;

    private int rsMode;
    private int speedMode;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        rsMode = buf.readByte();
        speedMode = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(rsMode);
        buf.writeByte(speedMode);
    }

    public PacketCrafterMode() {
    }

    public PacketCrafterMode(int x, int y, int z, int rsMode, int speedMode) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.rsMode = rsMode;
        this.speedMode = speedMode;
    }

    @Override
    public IMessage onMessage(PacketCrafterMode message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof CrafterBlockTileEntity3)) {
            // @Todo better logging
            System.out.println("createPowerMonitotPacket: TileEntity is not a CrafterBlockTileEntity!");
            return null;
        }
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) te;
        crafterBlockTileEntity.setRedstoneMode(message.rsMode);
        crafterBlockTileEntity.setSpeedMode(message.speedMode);
        return null;
    }

}
