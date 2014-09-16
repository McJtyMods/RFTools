package com.mcjty.rftools.blocks.crafter;

import com.mcjty.rftools.network.NetworkTools;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.tileentity.TileEntity;

import java.io.IOException;

public class PacketCrafter implements IMessage, IMessageHandler<PacketCrafter, IMessage> {
    private int x;
    private int y;
    private int z;

    private int recipeIndex;
    private ItemStack items[];

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        recipeIndex = buf.readByte();
        int l = buf.readByte();
        items = new ItemStack[l];
        for (int i = 0 ; i < l ; i++) {
            boolean b = buf.readBoolean();
            if (b) {
                items[i] = NetworkTools.readItemStack(buf);
            } else {
                items[i] = null;
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeByte(recipeIndex);
        buf.writeByte(items.length);
        for (ItemStack item : items) {
            if (item == null) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, item);
            }
        }
    }

    public PacketCrafter() {
    }

    public PacketCrafter(int x, int y, int z, int recipeIndex, ItemStack[] items) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.recipeIndex = recipeIndex;
        this.items = items;
    }

    @Override
    public IMessage onMessage(PacketCrafter message, MessageContext ctx) {
        EntityPlayer player = ctx.getServerHandler().playerEntity;
        TileEntity te = player.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof CrafterBlockTileEntity)) {
            // @Todo better logging
            System.out.println("createPowerMonitotPacket: TileEntity is not a CrafterBlockTileEntity!");
            return null;
        }
        CrafterBlockTileEntity crafterBlockTileEntity = (CrafterBlockTileEntity) te;
        if (message.recipeIndex != -1) {
            crafterBlockTileEntity.setRecipe(message.recipeIndex, message.items);
        }
        return null;
    }

}
