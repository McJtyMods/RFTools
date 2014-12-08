package com.mcjty.rftools.blocks.infuser;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.Infusable;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineInfuserTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

//    public static final String CMD_GETRESEARCHING = "getResearching";
//    public static final String CLIENTCMD_GETRESEARCHING = "getResearching";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, MachineInfuserContainer.factory, 2);

    private int infusing = 0;

    public int getInfusing() {
        return infusing;
    }

    public MachineInfuserTileEntity() {
        super(DimletConfiguration.INFUSER_MAXENERGY, DimletConfiguration.INFUSER_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        if (infusing > 0) {
            infusing--;
            if (infusing == 0) {
                ItemStack outputStack = inventoryHelper.getStacks()[1];
                finishInfusing(outputStack);
            }
            markDirty();
        } else {
            ItemStack inputStack = inventoryHelper.getStacks()[0];
            ItemStack outputStack = inventoryHelper.getStacks()[1];
            if (inputStack != null && inputStack.getItem() == ModItems.unknownDimlet && isInfusable(outputStack)) {
                startInfusing();
            }
        }
    }

    private boolean isInfusable(ItemStack stack) {
        NBTTagCompound tagCompound = getTagCompound(stack);
        if (tagCompound == null) {
            return false;
        }
        int infused = tagCompound.getInteger("infused");
        if (infused >= DimletConfiguration.maxInfuse) {
            return false;   // Already infused to the maximum.
        }
        return true;
    }

    private NBTTagCompound getTagCompound(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        Item item = stack.getItem();
        if (!(item instanceof ItemBlock)) {
            return null;
        }
        Block block = ((ItemBlock)item).field_150939_a;
        if (!(block instanceof Infusable)) {
            return null;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return new NBTTagCompound();
        } else {
            return tagCompound;
        }
    }

    private void finishInfusing(ItemStack stack) {
        NBTTagCompound tagCompound = getTagCompound(stack);
        if (tagCompound == null) {
            return;
        }
        int infused = tagCompound.getInteger("infused");
        tagCompound.setInteger("infused", infused+1);
    }

    private void startInfusing() {
        int rf = getEnergyStored(ForgeDirection.DOWN);
        if (rf < DimletConfiguration.rfInfuseOperation) {
            // Not enough energy.
            return;
        }
        extractEnergy(ForgeDirection.DOWN, DimletConfiguration.rfInfuseOperation, false);

        inventoryHelper.getStacks()[0].splitStack(1);
        if (inventoryHelper.getStacks()[0].stackSize == 0) {
            inventoryHelper.getStacks()[0] = null;
        }
        infusing = 5;
        markDirty();
    }

//    // Request the researching amount from the server. This has to be called on the client side.
//    public void requestResearchingFromServer() {
//        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
//                CMD_GETRESEARCHING,
//                CLIENTCMD_GETRESEARCHING));
//    }
//
//    @Override
//    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
//        Integer rc = super.executeWithResultInteger(command, args);
//        if (rc != null) {
//            return rc;
//        }
//        if (CMD_GETRESEARCHING.equals(command)) {
//            return researching;
//        }
//        return null;
//    }
//
//    @Override
//    public boolean execute(String command, Integer result) {
//        boolean rc = super.execute(command, result);
//        if (rc) {
//            return true;
//        }
//        if (CLIENTCMD_GETRESEARCHING.equals(command)) {
//            researching = result;
//            return true;
//        }
//        return false;
//    }
//
//
    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return MachineInfuserContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return MachineInfuserContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return MachineInfuserContainer.factory.isOutputSlot(index);
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
        return "Infuser Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
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
        infusing = tagCompound.getInteger("infusing");
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
        tagCompound.setInteger("infusing", infusing);
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
