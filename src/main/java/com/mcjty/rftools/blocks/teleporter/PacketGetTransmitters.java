package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.network.PacketRequestListFromClient;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public class PacketGetTransmitters extends PacketRequestListFromClient<TransmitterInfo, PacketGetTransmitters, PacketTransmittersReady> {

    public PacketGetTransmitters() {
    }

    public PacketGetTransmitters(int x, int y, int z) {
        super(x, y, z, DialingDeviceTileEntity.CMD_GETTRANSMITTERS);
    }

    @Override
    protected PacketTransmittersReady createMessageToClient(int x, int y, int z, List<TransmitterInfo> result) {
        return new PacketTransmittersReady(x, y, z, DialingDeviceTileEntity.CLIENTCMD_GETTRANSMITTERS, result);
    }
}
