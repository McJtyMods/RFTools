package mcjty.rftools.blocks.storage;

import mcjty.rftools.blocks.crafter.CrafterContainer;
import mcjty.rftools.blocks.crafter.CraftingRecipe;
import mcjty.rftools.craftinggrid.CraftingGrid;
import mcjty.rftools.craftinggrid.CraftingGridProvider;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class ModularStorageItemInventory implements IInventory, CraftingGridProvider, JEIRecipeAcceptor {
    private ItemStack stacks[];
    private final EntityPlayer entityPlayer;
    private CraftingGrid craftingGrid = new CraftingGrid();

    public ModularStorageItemInventory(EntityPlayer player) {
        this.entityPlayer = player;
        int maxSize = getMaxSize();
        stacks = new ItemStack[maxSize];
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            entityPlayer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(tagCompound);
        }
        tagCompound.setInteger("maxSize", maxSize);
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < Math.min(bufferTagList.tagCount(), maxSize) ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void setRecipe(List<ItemStack> stacks) {
        setInventorySlotContents(CrafterContainer.SLOT_CRAFTOUTPUT, stacks.get(0));
        for (int i = 1 ; i < stacks.size() ; i++) {
            setInventorySlotContents(CrafterContainer.SLOT_CRAFTINPUT + i-1, stacks.get(i));
        }

    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {

    }

    @Override
    public CraftingGrid getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public void craft(EntityPlayerMP player, int n) {
        CraftingRecipe craftingRecipe = craftingGrid.getActiveRecipe();

        IRecipe recipe = craftingRecipe.getCachedRecipe(player.worldObj);
        if (recipe == null) {
            // @todo give error?
            return;
        }

        if (craftingRecipe.getResult() != null && craftingRecipe.getResult().stackSize > 0) {
            if (n == -1) {
                n = craftingRecipe.getResult().getMaxStackSize();
            }

            int remainder = n % craftingRecipe.getResult().stackSize;
            n /= craftingRecipe.getResult().stackSize;
            if (remainder != 0) {
                n++;
            }
            if (n * craftingRecipe.getResult().stackSize > craftingRecipe.getResult().getMaxStackSize()) {
                n--;
            }

            for (int i = 0 ; i < n ; i++) {
                List<ItemStack> result = StorageCraftingTools.testAndConsumeCraftingItems(player, craftingRecipe, this, 0);
                if (result.isEmpty()) {
                    return;
                }
                for (ItemStack stack : result) {
                    if (!player.inventory.addItemStackToInventory(stack)) {
                        player.entityDropItem(stack, 1.05f);
                    }
                }
            }
        }
    }


    private int getMaxSize() {
        ItemStack heldItem = entityPlayer.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem == null) {
            return 0;
        }
        if (heldItem.getItem() != ModularStorageSetup.storageModuleTabletItem) {
            return 0;
        }
        if (heldItem.getTagCompound() == null) {
            return 0;
        }
        return StorageModuleItem.MAXSIZE[heldItem.getTagCompound().getInteger("childDamage")];
    }

    public ItemStack[] getStacks() {
        return stacks;
    }

    @Override
    public int getSizeInventory() {
        return getMaxSize();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= getMaxSize()) {
            return null;
        } else {
            return stacks[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index >= stacks.length) {
            return null;
        }
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = null;
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (stacks[index].stackSize == 0) {
                stacks[index] = null;
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= stacks.length) {
            return;
        }
        stacks[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        NBTTagList bufferTagList = new NBTTagList();
        int numStacks = 0;
        for (int i = 0 ; i < getMaxSize() ; i++) {
            ItemStack stack = stacks[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
                if (stack.stackSize > 0) {
                    numStacks++;
                }
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        tagCompound.setTag("Items", bufferTagList);
        tagCompound.setInteger("count", numStacks);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index < getMaxSize();
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, null);
        return stack;
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "modular storage";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }
}
