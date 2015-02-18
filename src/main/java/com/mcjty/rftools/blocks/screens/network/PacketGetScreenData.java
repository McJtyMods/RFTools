package com.mcjty.rftools.blocks.screens.network;

import com.mcjty.rftools.blocks.screens.ScreenTileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Map;

public class PacketGetScreenData implements IMessage,IMessageHandler<PacketGetScreenData, PacketReturnScreenData> {
    private int x;
    private int y;
    private int z;
    private long millis;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        millis = buf.readLong();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeLong(millis);
    }

    public PacketGetScreenData() {
    }

    public PacketGetScreenData(int tileX, int tileY, int tileZ, long millis) {
        this.x = tileX;
        this.y = tileY;
        this.z = tileZ;
        this.millis = millis;
    }

    @Override
    public PacketReturnScreenData onMessage(PacketGetScreenData message, MessageContext ctx) {
        World world = ctx.getServerHandler().playerEntity.worldObj;
        TileEntity te = world.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof ScreenTileEntity)) {
            // @Todo better logging
            System.out.println("PacketGetScreenData: TileEntity is not a SimpleScreenTileEntity!");
            return null;
        }
        Map<Integer, Object[]> screenData = ((ScreenTileEntity) te).getScreenData(message.millis);
        return new PacketReturnScreenData(message.x, message.y, message.z, screenData);
    }

}