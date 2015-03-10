package com.mcjty.rftools.items.devdelight;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PacketDelightingInfoReady implements IMessage, IMessageHandler<PacketDelightingInfoReady, IMessage> {
    private List<String> blockClasses;
    private List<String> teClasses;
    private Map<String,DelightingInfoHelper.NBTDescription> nbtData;
    private int metadata;

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        blockClasses = new ArrayList<String>(size);
        for (int i = 0 ; i < size ; i++) {
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            blockClasses.add(new String(dst));
        }

        size = buf.readInt();
        teClasses = new ArrayList<String>(size);
        for (int i = 0 ; i < size ; i++) {
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            teClasses.add(new String(dst));
        }

        size = buf.readInt();
        nbtData = new HashMap<String, DelightingInfoHelper.NBTDescription>(size);
        for (int i = 0 ; i < size ; i++) {
            byte[] dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            String key = new String(dst);

            dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            String type = new String(dst);

            dst = new byte[buf.readInt()];
            buf.readBytes(dst);
            String value = new String(dst);

            nbtData.put(key, new DelightingInfoHelper.NBTDescription(type, value));
        }

        metadata = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(blockClasses.size());
        for (String c : blockClasses) {
            buf.writeInt(c.length());
            buf.writeBytes(c.getBytes());
        }
        buf.writeInt(teClasses.size());
        for (String c : teClasses) {
            buf.writeInt(c.length());
            buf.writeBytes(c.getBytes());
        }
        buf.writeInt(nbtData.size());
        for (Map.Entry<String,DelightingInfoHelper.NBTDescription> me : nbtData.entrySet()) {
            String key = me.getKey();
            DelightingInfoHelper.NBTDescription value = me.getValue();
            buf.writeInt(key.length());
            buf.writeBytes(key.getBytes());
            buf.writeInt(value.getType().length());
            buf.writeBytes(value.getType().getBytes());
            buf.writeInt(value.getValue().length());
            buf.writeBytes(value.getValue().getBytes());
        }
        buf.writeInt(metadata);
    }

    public PacketDelightingInfoReady() {
    }

    public PacketDelightingInfoReady(List<String> blockClasses, List<String> teClasses, Map<String,DelightingInfoHelper.NBTDescription> nbtData, int metadata) {
        this.blockClasses = new ArrayList<String>(blockClasses);
        this.teClasses = new ArrayList<String>(teClasses);
        this.nbtData = new HashMap<String, DelightingInfoHelper.NBTDescription>(nbtData);
        this.metadata = metadata;
    }

    @Override
    public IMessage onMessage(PacketDelightingInfoReady message, MessageContext ctx) {
        GuiDevelopersDelight.setServerBlockClasses(message.blockClasses);
        GuiDevelopersDelight.setServerTEClasses(message.teClasses);
        GuiDevelopersDelight.setServerNBTData(message.nbtData);
        GuiDevelopersDelight.setMetadata(message.metadata);
        return null;
    }

}
