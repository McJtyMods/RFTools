package mcjty.rftools.blocks.screens.network;

import mcjty.rftools.blocks.screens.ScreenTileEntity;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

public class PacketModuleUpdate implements IMessage, IMessageHandler<PacketModuleUpdate, IMessage> {
    private int x;
    private int y;
    private int z;

    private int slotIndex;
    private NBTTagCompound tagCompound;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        slotIndex = buf.readInt();
        PacketBuffer buffer = new PacketBuffer(buf);
        try {
            tagCompound = buffer.readNBTTagCompoundFromBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeInt(slotIndex);
        PacketBuffer buffer = new PacketBuffer(buf);
        try {
            buffer.writeNBTTagCompoundToBuffer(tagCompound);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public PacketModuleUpdate() {
    }

    public PacketModuleUpdate(int x, int y, int z, int slotIndex, NBTTagCompound tagCompound) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.slotIndex = slotIndex;
        this.tagCompound = tagCompound;
    }

    @Override
    public IMessage onMessage(PacketModuleUpdate message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof ScreenTileEntity)) {
            // @Todo better logging
            System.out.println("PacketModuleUpdate: TileEntity is not a SimpleScreenTileEntity!");
            return null;
        }
        ((ScreenTileEntity) te).updateModuleData(message.slotIndex, message.tagCompound);
        return null;
    }

}
