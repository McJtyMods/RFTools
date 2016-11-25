package mcjty.rftools.blocks.storage;

import mcjty.lib.compat.CompatInventory;
import mcjty.lib.tools.ItemStackList;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.craftinggrid.CraftingGrid;
import mcjty.rftools.craftinggrid.CraftingGridProvider;
import mcjty.rftools.craftinggrid.InventoriesItemSource;
import mcjty.rftools.craftinggrid.StorageCraftingTools;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.List;

public class ModularStorageItemInventory implements CompatInventory, CraftingGridProvider, JEIRecipeAcceptor {
    private ItemStackList stacks;
    private final EntityPlayer entityPlayer;
    private CraftingGrid craftingGrid = new CraftingGrid();

    public ModularStorageItemInventory(EntityPlayer player) {
        this.entityPlayer = player;
        int maxSize = getMaxSize();
        stacks = ItemStackList.create(maxSize);
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            entityPlayer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(tagCompound);
        }
        tagCompound.setInteger("maxSize", maxSize);
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < Math.min(bufferTagList.tagCount(), maxSize) ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks.set(i, ItemStackTools.loadFromNBT(nbtTagCompound));
        }
        craftingGrid.readFromNBT(tagCompound.getCompoundTag("grid"));

    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0 ; i < stacks.size() ; i++) {
            craftingGrid.getCraftingGridInventory().setInventorySlotContents(i, stacks.get(i));
        }
        markDirty();
    }

    @Override
    public void storeRecipe(int index) {
        getCraftingGrid().storeRecipe(index);
    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
        markDirty();
    }

    @Override
    public CraftingGrid getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public void markInventoryDirty() {
        markDirty();
    }

    @Override
    @Nonnull
    public int[] craft(EntityPlayerMP player, int n, boolean test) {
        InventoriesItemSource itemSource = new InventoriesItemSource()
                .add(player.inventory, 0).add(this, 0);
        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
            return new int[0];
        }
    }


    private int getMaxSize() {
        ItemStack heldItem = entityPlayer.getHeldItem(EnumHand.MAIN_HAND);
        if (ItemStackTools.isEmpty(heldItem)) {
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

    public ItemStackList getStacks() {
        return stacks;
    }

    @Override
    public int getSizeInventory() {
        return getMaxSize();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (index >= getMaxSize()) {
            return ItemStackTools.getEmptyStack();
        } else {
            return stacks.get(index);
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index >= stacks.size()) {
            return ItemStackTools.getEmptyStack();
        }
        if (ItemStackTools.isValid(stacks.get(index))) {
            if (ItemStackTools.getStackSize(stacks.get(index)) <= amount) {
                ItemStack old = stacks.get(index);
                stacks.set(index, ItemStackTools.getEmptyStack());
                markDirty();
                return old;
            }
            ItemStack its = stacks.get(index).splitStack(amount);
            if (ItemStackTools.isEmpty(stacks.get(index))) {
                stacks.set(index, ItemStackTools.getEmptyStack());
            }
            markDirty();
            return its;
        }
        return ItemStackTools.getEmptyStack();
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index >= stacks.size()) {
            return;
        }
        stacks.set(index, stack);
        if (ItemStackTools.isValid(stack) && ItemStackTools.getStackSize(stack) > getInventoryStackLimit()) {
            ItemStackTools.setStackSize(stack, getInventoryStackLimit());
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
            ItemStack stack = stacks.get(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (ItemStackTools.isValid(stack)) {
                stack.writeToNBT(nbtTagCompound);
                if (ItemStackTools.getStackSize(stack) > 0) {
                    numStacks++;
                }
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        tagCompound.setTag("Items", bufferTagList);
        tagCompound.setInteger("count", numStacks);
        tagCompound.setTag("grid", craftingGrid.writeToNBT());
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return index < getMaxSize();
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        ItemStack stack = getStackInSlot(index);
        setInventorySlotContents(index, ItemStackTools.getEmptyStack());
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
