package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.blocks.screens.modules.ScreenModule;
import com.mcjty.rftools.blocks.screens.modulesclient.ClientScreenModule;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleScreenTileEntity extends GenericTileEntity implements ISidedInventory {

    public static final String CMD_ = "settings";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ScreenContainer.factory, ScreenContainer.SCREEN_MODULES);

    // This is a map that contains a map from the coordinate of the screen to a map of screen data from the server indexed by slot number,
    @SideOnly(Side.CLIENT)
    public static Map<Coordinate,Map<Integer,String>> screenData = new HashMap<Coordinate, Map<Integer, String>>();

    // Cached client screen modules
    private List<ClientScreenModule> clientScreenModules = null;

    private boolean needsServerData = false;

    // Cached server screen modules
    private List<ScreenModule> screenModules = null;


    @Override
    protected void checkStateClient() {
        super.checkStateClient();
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return ScreenContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        return ScreenContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        return ScreenContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        clientScreenModules = null;
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        clientScreenModules = null;
    }

    @Override
    public String getInventoryName() {
        return "Screen Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i+ScreenContainer.SLOT_MODULES] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
        clientScreenModules = null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = ScreenContainer.SLOT_MODULES ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    public void updateModuleData(int slot, NBTTagCompound tagCompound) {
        ItemStack stack = inventoryHelper.getStacks()[slot];
        stack.setTagCompound(tagCompound);
        markDirty();
    }

    // This is called client side.
    public List<ClientScreenModule> getClientScreenModules() {
        if (clientScreenModules == null) {
            needsServerData = false;
            clientScreenModules = new ArrayList<ClientScreenModule>();
            for (ItemStack itemStack : inventoryHelper.getStacks()) {
                if (itemStack != null && itemStack.getItem() instanceof ModuleProvider) {
                    ModuleProvider moduleProvider = (ModuleProvider) itemStack.getItem();
                    ClientScreenModule clientScreenModule;
                    try {
                        clientScreenModule = moduleProvider.getClientScreenModule().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                    clientScreenModule.setupFromNBT(itemStack.getTagCompound(), worldObj.provider.dimensionId, xCoord, yCoord, zCoord);
                    clientScreenModules.add(clientScreenModule);
                    if (clientScreenModule.needsServerData()) {
                        needsServerData = true;
                    }
                } else {
                    clientScreenModules.add(null);        // To keep the indexing correct so that the modules correspond with there slot number.
                }
            }

        }
        return clientScreenModules;
    }

    public boolean isNeedsServerData() {
        return needsServerData;
    }

    // This is called server side.
    public List<ScreenModule> getScreenModules() {
        if (screenModules == null) {
            screenModules = new ArrayList<ScreenModule>();
            for (ItemStack itemStack : inventoryHelper.getStacks()) {
                if (itemStack != null && itemStack.getItem() instanceof ModuleProvider) {
                    ModuleProvider moduleProvider = (ModuleProvider) itemStack.getItem();
                    ScreenModule screenModule;
                    try {
                        screenModule = moduleProvider.getServerScreenModule().newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        continue;
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        continue;
                    }
                    screenModule.setupFromNBT(itemStack.getTagCompound(), worldObj.provider.dimensionId, xCoord, yCoord, zCoord);
                    screenModules.add(screenModule);
                } else {
                    screenModules.add(null);        // To keep the indexing correct so that the modules correspond with there slot number.
                }
            }

        }
        return screenModules;
    }


    // This is called server side.
    public Map<Integer,String> getScreenData() {
        HashMap<Integer, String> map = new HashMap<Integer, String>();
        List<ScreenModule> screenModules = getScreenModules();
        int moduleIndex = 0;
        for (ScreenModule module : screenModules) {
            if (module != null) {
                String data = module.getData();
                if (data != null) {
                    map.put(moduleIndex, data);
                }
            }
            moduleIndex++;
        }
        return map;
    }
}
