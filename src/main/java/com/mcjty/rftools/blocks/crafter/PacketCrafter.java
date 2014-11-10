package com.mcjty.rftools.blocks.crafter;

import com.mcjty.rftools.network.NetworkTools;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class PacketCrafter implements IMessage, IMessageHandler<PacketCrafter, IMessage> {
    private int x;
    private int y;
    private int z;

    private int recipeIndex;
    private ItemStack items[];
    private boolean keepOne;
    private boolean craftInternal;

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readInt();
        y = buf.readInt();
        z = buf.readInt();
        keepOne = buf.readBoolean();
        craftInternal = buf.readBoolean();

        recipeIndex = buf.readByte();
        int l = buf.readByte();
        if (l == 0) {
            items = null;
        } else {
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
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeInt(z);
        buf.writeBoolean(keepOne);
        buf.writeBoolean(craftInternal);

        buf.writeByte(recipeIndex);
        if (items != null) {
            buf.writeByte(items.length);
            for (ItemStack item : items) {
                if (item == null) {
                    buf.writeBoolean(false);
                } else {
                    buf.writeBoolean(true);
                    NetworkTools.writeItemStack(buf, item);
                }
            }
        } else {
            buf.writeByte(0);
        }
    }

    public PacketCrafter() {
    }

    public PacketCrafter(int x, int y, int z, int recipeIndex, InventoryCrafting inv, ItemStack result, boolean keepOne, boolean craftInternal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.recipeIndex = recipeIndex;
        this.items = new ItemStack[10];
        if (inv != null) {
            for (int i = 0 ; i < 9 ; i++) {
                items[i] = inv.getStackInSlot(i);
            }
        }
        items[9] = result;
        this.keepOne = keepOne;
        this.craftInternal = craftInternal;
    }

    @Override
    public IMessage onMessage(PacketCrafter message, MessageContext ctx) {
        TileEntity te = ctx.getServerHandler().playerEntity.worldObj.getTileEntity(message.x, message.y, message.z);
        if(!(te instanceof CrafterBlockTileEntity3)) {
            // @Todo better logging
            System.out.println("createPowerMonitotPacket: TileEntity is not a CrafterBlockTileEntity!");
            return null;
        }
        CrafterBlockTileEntity3 crafterBlockTileEntity = (CrafterBlockTileEntity3) te;
        if (message.recipeIndex != -1) {
            CraftingRecipe recipe = crafterBlockTileEntity.getRecipe(message.recipeIndex);
            recipe.setRecipe(message.items, message.items[9]);
            recipe.setKeepOne(message.keepOne);
            recipe.setCraftInternal(message.craftInternal);
        }
        return null;
    }

}
