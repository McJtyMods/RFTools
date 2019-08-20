package mcjty.rftools.blocks.crafter;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.craftinggrid.CraftingRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSelectRecipe implements IMessage {
    private BlockPos pos;

    private int recipeIndex;
    private ItemStack items[];
    private boolean keepOne;
    private CraftingRecipe.CraftMode craftInternal;

    @Override
    public void fromBytes(ByteBuf buf) {
        pos = NetworkTools.readPos(buf);
        keepOne = buf.readBoolean();
        craftInternal = CraftingRecipe.CraftMode.values()[buf.readByte()];

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
        NetworkTools.writePos(buf, pos);
        buf.writeBoolean(keepOne);
        buf.writeByte(craftInternal.ordinal());

        buf.writeByte(recipeIndex);
        if (items != null) {
            buf.writeByte(items.length);
            for (ItemStack item : items) {
                if (item.isEmpty()) {
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

    public PacketSelectRecipe() {
    }

    public PacketSelectRecipe(BlockPos pos, int recipeIndex, CraftingInventory inv, ItemStack result, boolean keepOne, CraftingRecipe.CraftMode craftInternal) {
        this.pos = pos;
        this.recipeIndex = recipeIndex;
        this.items = new ItemStack[10];
        if (inv != null) {
            for (int i = 0 ; i < 9 ; i++) {
                ItemStack slot = inv.getStackInSlot(i);
                if (!slot.isEmpty()) {
                    items[i] = slot.copy();
                } else {
                    items[i] = ItemStack.EMPTY;
                }
            }
        }
        items[9] = result.isEmpty() ? ItemStack.EMPTY : result.copy();
        this.keepOne = keepOne;
        this.craftInternal = craftInternal;
    }

    public static class Handler implements IMessageHandler<PacketSelectRecipe, IMessage> {
        @Override
        public IMessage onMessage(PacketSelectRecipe message, MessageContext ctx) {
            FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(() -> handle(message, ctx));
            return null;
        }

        private void handle(PacketSelectRecipe message, MessageContext ctx) {
            TileEntity te = ctx.getServerHandler().player.getEntityWorld().getTileEntity(message.pos);
            if(!(te instanceof CrafterBaseTE)) {
                Logging.logError("Wrong type of tile entity (expected CrafterBaseTE)!");
                return;
            }
            CrafterBaseTE crafterBlockTileEntity = (CrafterBaseTE) te;
            if (message.recipeIndex != -1) {
                CraftingRecipe recipe = crafterBlockTileEntity.getRecipe(message.recipeIndex);
                recipe.setRecipe(message.items, message.items[9]);
                recipe.setKeepOne(message.keepOne);
                recipe.setCraftMode(message.craftInternal);
                crafterBlockTileEntity.markDirtyClient();
            }
        }
    }
}
