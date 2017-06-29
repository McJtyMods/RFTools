package mcjty.rftools.blocks.crafter;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class CraftingRecipe {
    private InventoryCrafting inv = new InventoryCrafting(new Container() {
        @Override
        public boolean canInteractWith(EntityPlayer var1) {
            return false;
        }
    }, 3, 3);
    private ItemStack result = ItemStack.EMPTY;

    private boolean recipePresent = false;
    private IRecipe recipe = null;

    private boolean keepOne = false;

    enum CraftMode {
        EXT("Ext"),
        INT("Int"),
        EXTC("ExtC");

        private final String description;

        CraftMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private CraftMode craftMode = CraftMode.EXT;

    public static IRecipe findRecipe(World world, InventoryCrafting inv) {
        for (IRecipe r : CraftingManager.REGISTRY) {
            if (r != null && r.matches(inv, world)) {
                return r;
            }
        }
        return null;
    }

    public void readFromNBT(NBTTagCompound tagCompound) {
        NBTTagList nbtTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < nbtTagList.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = nbtTagList.getCompoundTagAt(i);
            inv.setInventorySlotContents(i, new ItemStack(nbtTagCompound));
        }
        NBTTagCompound resultCompound = tagCompound.getCompoundTag("Result");
        if (resultCompound != null) {
            result = new ItemStack(resultCompound);
        } else {
            result = ItemStack.EMPTY;
        }
        keepOne = tagCompound.getBoolean("Keep");
        craftMode = CraftMode.values()[tagCompound.getByte("Int")];
        recipePresent = false;
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList nbtTagList = new NBTTagList();
        for (int i = 0 ; i < 9 ; i++) {
            ItemStack stack = inv.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (!stack.isEmpty()) {
                stack.writeToNBT(nbtTagCompound);
            }
            nbtTagList.appendTag(nbtTagCompound);
        }
        NBTTagCompound resultCompound = new NBTTagCompound();
        if (!result.isEmpty()) {
            result.writeToNBT(resultCompound);
        }
        tagCompound.setTag("Result", resultCompound);
        tagCompound.setTag("Items", nbtTagList);
        tagCompound.setBoolean("Keep", keepOne);
        tagCompound.setByte("Int", (byte) craftMode.ordinal());
    }

    public void setRecipe(ItemStack[] items, ItemStack result) {
        for (int i = 0 ; i < 9 ; i++) {
            inv.setInventorySlotContents(i, items[i]);
        }
        this.result = result;
        recipePresent = false;
    }

    public InventoryCrafting getInventory() {
        return inv;
    }

    public void setResult(ItemStack result) {
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    public IRecipe getCachedRecipe(World world) {
        if (!recipePresent) {
            recipePresent = true;
            recipe = findRecipe(world, inv);
        }
        return recipe;
    }

    public boolean isKeepOne() {
        return keepOne;
    }

    public void setKeepOne(boolean keepOne) {
        this.keepOne = keepOne;
    }

    public CraftMode getCraftMode() {
        return craftMode;
    }

    public void setCraftMode(CraftMode craftMode) {
        this.craftMode = craftMode;
    }
}
