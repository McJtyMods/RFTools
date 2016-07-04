package mcjty.rftools.blocks.storage;

import mcjty.rftools.craftinggrid.CraftingGrid;
import mcjty.rftools.craftinggrid.CraftingGridProvider;
import mcjty.rftools.craftinggrid.InventoriesItemSource;
import mcjty.rftools.craftinggrid.StorageCraftingTools;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class RemoteStorageItemInventory implements IInventory, CraftingGridProvider, JEIRecipeAcceptor {
    private ItemStack stacks[] = new ItemStack[RemoteStorageItemContainer.MAXSIZE_STORAGE];
    private final EntityPlayer entityPlayer;
    private CraftingGrid craftingGrid = new CraftingGrid();

    public RemoteStorageItemInventory(EntityPlayer player) {
        this.entityPlayer = player;
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        if (tagCompound == null) {
            tagCompound = new NBTTagCompound();
            entityPlayer.getHeldItem(EnumHand.MAIN_HAND).setTagCompound(tagCompound);
        }
        craftingGrid.readFromNBT(tagCompound.getCompoundTag("grid"));
    }

    private RemoteStorageTileEntity getRemoteStorage() {
        int id = getStorageID();
        if (id == -1) {
            return null;
        }
        return RemoteStorageIdRegistry.getRemoteStorage(entityPlayer.worldObj, id);
    }

    private int getStorageID() {
        if (entityPlayer.getHeldItem(EnumHand.MAIN_HAND) == null || entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound() == null) {
            return -1;
        }
        return entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getInteger("id");
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
    public int[] craft(EntityPlayerMP player, int n, boolean test) {
        InventoriesItemSource itemSource = new InventoriesItemSource()
                .add(player.inventory, 0).add(this, 0);
        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
            return null;
        }
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0 ; i < stacks.size() ; i++) {
            craftingGrid.getCraftingGridInventory().setInventorySlotContents(i, stacks.get(i));
        }
        markDirty();
    }

    private boolean isServer() {
        return !entityPlayer.worldObj.isRemote;
    }

    private ItemStack[] getStacks() {
        if (isServer()) {
            RemoteStorageTileEntity storage = getRemoteStorage();
            if (storage == null) {
                return new ItemStack[0];
            }
            int si = storage.findRemoteIndex(getStorageID());
            if (si == -1) {
                return new ItemStack[0];
            }
            return storage.getRemoteStacks(si);
        } else {
            int maxSize = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getInteger("maxSize");
            if (maxSize != stacks.length) {
                stacks = new ItemStack[maxSize];
            }
            return stacks;
        }
    }

    @Override
    public int getSizeInventory() {
        if (isServer()) {
            RemoteStorageTileEntity storage = getRemoteStorage();
            if (storage == null) {
                return 0;
            }
            int si = storage.findRemoteIndex(getStorageID());
            if (si == -1) {
                return 0;
            }
            int maxStacks = storage.getMaxStacks(si);
            entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().setInteger("maxSize", maxStacks);
            return maxStacks;
        } else {
            return entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getInteger("maxSize");
        }
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        if (isServer()) {
            RemoteStorageTileEntity storage = getRemoteStorage();
            if (storage == null) {
                return null;
            }
            int si = storage.findRemoteIndex(getStorageID());
            if (si == -1) {
                return null;
            }
            return storage.getRemoteSlot(si, index);
        } else {
            return stacks[index];
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (isServer()) {
            RemoteStorageTileEntity storage = getRemoteStorage();
            if (storage == null) {
                return null;
            }
            int si = storage.findRemoteIndex(getStorageID());
            if (si == -1) {
                return null;
            }
            return storage.decrStackSizeRemote(si, index, amount);
        } else {
            if (index >= stacks.length) {
                return null;
            }
            if (stacks[index] != null) {
                markDirty();
                if (stacks[index].stackSize <= amount) {
                    ItemStack old = stacks[index];
                    stacks[index] = null;
                    return old;
                }
                ItemStack its = stacks[index].splitStack(amount);
                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }
                return its;
            }
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (isServer()) {
            RemoteStorageTileEntity storage = getRemoteStorage();
            if (storage == null) {
                return;
            }
            int si = storage.findRemoteIndex(getStorageID());
            if (si == -1) {
                return;
            }
            storage.updateRemoteSlot(si, getInventoryStackLimit(), index, stack);
        } else {
            if (index >= stacks.length) {
                return;
            }
            stacks[index] = stack;
            if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                stack.stackSize = getInventoryStackLimit();
            }
            markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public void markDirty() {
        RemoteStorageTileEntity storage = getRemoteStorage();
        if (storage != null) {
            storage.markDirty();
        }
        NBTTagCompound tagCompound = entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
        tagCompound.setTag("grid", craftingGrid.writeToNBT());
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        ItemStack[] s = getStacks();
        if (index >= s.length) {
            return false;
        }
        if (isServer()) {
            RemoteStorageTileEntity storage = getRemoteStorage();
            if (storage == null) {
                return false;
            }
            int si = storage.findRemoteIndex(getStorageID());
            if (si == -1) {
                return false;
            }
            if (index >= storage.getMaxStacks(si)) {
                return false;
            }
        }
        return true;
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
        return "remote inventory";
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
