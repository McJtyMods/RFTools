package com.mcjty.rftools.blocks.monitor;

import com.mcjty.rftools.network.PacketListFromClient;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;

public class PacketAdjacentBlocksReady extends PacketListFromClient<PacketAdjacentBlocksReady,Coordinate> {

    public PacketAdjacentBlocksReady() {
    }

    public PacketAdjacentBlocksReady(int x, int y, int z, String command, List<Coordinate> list) {
        super(x, y, z, command, list);
    }

    @Override
    protected Coordinate createItem(ByteBuf buf) {
        return new Coordinate(buf);
    }
}
