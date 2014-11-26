package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class DimensionBuilderTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_GETBUILDING = "getBuilding";
    public static final String CLIENTCMD_GETBUILDING = "getBuilding";

    private static int buildPercentage = 0;
    private int ticker = 5;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionBuilderContainer.factory, 1);

    public DimensionBuilderTileEntity() {
        super(DimletConfiguration.BUILDER_MAXENERGY, DimletConfiguration.BUILDER_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = 5;

        ItemStack itemStack = inventoryHelper.getStacks()[0];
        if (itemStack == null || itemStack.stackSize == 0) {
            setState(-1, 0);
            return;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int ticksLeft = tagCompound.getInteger("ticksLeft");
        int tickCost = tagCompound.getInteger("tickCost");
        if (ticksLeft > 0) {
            ticksLeft--;
            tagCompound.setInteger("ticksLeft", ticksLeft);
        }

        setState(ticksLeft, tickCost);
    }

    private void setState(int ticksLeft, int tickCost) {
        int state = 0;
        if (ticksLeft == 0) {
            state = 0;
        } else if (ticksLeft == -1) {
            state = 1;
        } else if (((ticksLeft >> 2) & 1) == 0) {
            state = 2;
        } else {
            state = 3;
        }
        int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newmeta = BlockTools.setState(metadata, state);
        if (newmeta != metadata) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newmeta, 2);
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimletResearcherContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimletResearcherContainer.factory.isInputSlot(index);
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
        return "Builder Inventory";
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

    // Request the building percentage from the server. This has to be called on the client side.
    public void requiestBuildingPercentage() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETBUILDING,
                CLIENTCMD_GETBUILDING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETBUILDING.equals(command)) {
            ItemStack itemStack = inventoryHelper.getStacks()[0];
            if (itemStack == null || itemStack.stackSize == 0) {
                return 0;
            } else {
                NBTTagCompound tagCompound = itemStack.getTagCompound();
                int ticksLeft = tagCompound.getInteger("ticksLeft");
                int tickCost = tagCompound.getInteger("tickCost");
                return (tickCost - ticksLeft) * 100 / tickCost;
            }
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETBUILDING.equals(command)) {
            buildPercentage = result;
            return true;
        }
        return false;
    }

    public static int getBuildPercentage() {
        return buildPercentage;
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
