package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletRandomizer;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.Map;

public class DimletResearcherTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_GETRESEARCHING = "getResearching";
    public static final String CLIENTCMD_GETRESEARCHING = "getResearching";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimletResearcherContainer.factory, 2);

    private int researching = 0;

    public int getResearching() {
        return researching;
    }

    public DimletResearcherTileEntity() {
        super(DimletConfiguration.RESEARCHER_MAXENERGY, DimletConfiguration.RESEARCHER_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        if (researching > 0) {
            researching--;
            if (researching == 0) {
                DimletKey key = DimletRandomizer.getRandomDimlet(worldObj.rand);
                InventoryHelper.mergeItemStack(this, KnownDimletConfiguration.makeKnownDimlet(key, worldObj), 1, 2, new ArrayList<InventoryHelper.SlotModifier>());
            }
            markDirty();
        } else {
            ItemStack inputStack = inventoryHelper.getStacks()[0];
            ItemStack outputStack = inventoryHelper.getStacks()[1];
            if (inputStack != null && inputStack.getItem() == ModItems.unknownDimlet && outputStack == null) {
                startResearching();
            }
        }
    }

    private void startResearching() {
        int rf = DimletConfiguration.rfResearchOperation;
        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            // Not enough energy.
            return;
        }
        extractEnergy(ForgeDirection.DOWN, rf, false);

        inventoryHelper.getStacks()[0].splitStack(1);
        if (inventoryHelper.getStacks()[0].stackSize == 0) {
            inventoryHelper.getStacks()[0] = null;
        }
        researching = 16;
        markDirty();
    }

    // Request the researching amount from the server. This has to be called on the client side.
    public void requestResearchingFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETRESEARCHING,
                CLIENTCMD_GETRESEARCHING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETRESEARCHING.equals(command)) {
            return researching;
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETRESEARCHING.equals(command)) {
            researching = result;
            return true;
        }
        return false;
    }


    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[] { DimletResearcherContainer.SLOT_DIMLETINPUT, DimletResearcherContainer.SLOT_DIMLETOUTPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimletResearcherContainer.factory.isInputSlot(index) || DimletResearcherContainer.factory.isSpecificItemSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return DimletResearcherContainer.factory.isOutputSlot(index);
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
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public String getInventoryName() {
        return "Researcher Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 16;
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
        researching = tagCompound.getInteger("researching");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("researching", researching);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }
}
