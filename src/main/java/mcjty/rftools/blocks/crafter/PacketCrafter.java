package mcjty.rftools.blocks.crafter;

import mcjty.lib.network.NetworkTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.craftinggrid.CraftingRecipe;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketCrafter {
    private BlockPos pos;

    private int recipeIndex;
    private ItemStack items[];
    private boolean keepOne;
    private CraftingRecipe.CraftMode craftInternal;

    public void toBytes(PacketBuffer buf) {
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

    public PacketCrafter() {
    }

    public PacketCrafter(PacketBuffer buf) {
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
                    items[i] = ItemStack.EMPTY;
                }
            }
        }
    }

    public PacketCrafter(BlockPos pos, int recipeIndex, CraftingInventory inv, ItemStack result, boolean keepOne, CraftingRecipe.CraftMode craftInternal) {
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
        } else {
            for (int i = 0 ; i < 9 ; i++) {
                items[i] = ItemStack.EMPTY;
            }
        }
        items[9] = result.isEmpty() ? ItemStack.EMPTY : result.copy();
        this.keepOne = keepOne;
        this.craftInternal = craftInternal;
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            TileEntity te = ctx.getSender().getEntityWorld().getTileEntity(pos);
            if(!(te instanceof CrafterBaseTE)) {
                Logging.logError("Wrong type of tile entity (expected CrafterBaseTE)!");
                return;
            }
            CrafterBaseTE crafterBlockTileEntity = (CrafterBaseTE) te;
            crafterBlockTileEntity.noRecipesWork = false;
            if (recipeIndex != -1) {
                updateRecipe(crafterBlockTileEntity);
            }
        });
        ctx.setPacketHandled(true);
    }

    private void updateRecipe(CrafterBaseTE crafterBlockTileEntity) {
        CraftingRecipe recipe = crafterBlockTileEntity.getRecipe(recipeIndex);
        recipe.setRecipe(items, items[9]);
        recipe.setKeepOne(keepOne);
        recipe.setCraftMode(craftInternal);
        crafterBlockTileEntity.selectRecipe(recipeIndex);
        crafterBlockTileEntity.markDirtyClient();
    }
}
