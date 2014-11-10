package com.mcjty.rftools.items.devdelight;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketGetDelightingInfo implements IMessage, IMessageHandler<PacketGetDelightingInfo, PacketDelightingInfoReady> {
    private int x;
    private int y;
    private int z;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
    }

    public PacketGetDelightingInfo() {
    }

    public PacketGetDelightingInfo(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public PacketDelightingInfoReady onMessage(PacketGetDelightingInfo message, MessageContext ctx) {
        int x = message.x;
        int y = message.y;
        int z = message.z;
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        World world = player.worldObj;

        List<String> blockClasses = new ArrayList<String>();
        List<String> teClasses = new ArrayList<String>();
        Map<String,DelightingInfoHelper.NBTDescription> nbtData = new HashMap<String, DelightingInfoHelper.NBTDescription>();

        int metadata = DelightingInfoHelper.fillDelightingData(x, y, z, world, blockClasses, teClasses, nbtData);

        return new PacketDelightingInfoReady(blockClasses, teClasses, nbtData, metadata);
    }
}