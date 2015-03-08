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

public class DimletScramblerTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_GETSCRAMBLING = "getScrambling";
    public static final String CLIENTCMD_GETSCRAMBLING = "getScrambling";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimletScramblerContainer.factory, 4);

    private int scrambling = 0;
    private float bonus = 0.0f;

    public int getScrambling() {
        return scrambling;
    }

    public DimletScramblerTileEntity() {
        super(DimletConfiguration.SCRAMBLER_MAXENERGY, DimletConfiguration.SCRAMBLER_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        if (scrambling > 0) {
            scrambling--;
            if (scrambling == 0) {
                DimletKey key = DimletRandomizer.getRandomDimlet(bonus, worldObj.rand);
                InventoryHelper.mergeItemStack(this, KnownDimletConfiguration.makeKnownDimlet(key, worldObj), 3, 4, new ArrayList<InventoryHelper.SlotModifier>());
            }
            markDirty();
        } else {
            ItemStack input1 = inventoryHelper.getStacks()[0];
            ItemStack input2 = inventoryHelper.getStacks()[1];
            ItemStack input3 = inventoryHelper.getStacks()[2];
            ItemStack outputStack = inventoryHelper.getStacks()[3];
            if (isValidInput(input1, input2, input3) && outputStack == null) {
                startScrambling();
            }
        }
    }

    private boolean isValidInput(ItemStack input1, ItemStack input2, ItemStack input3) {
        if (input1 == null || input2 == null || input3 == null) {
            return false;
        }
        if (input1.getItem() != ModItems.knownDimlet || input2.getItem() != ModItems.knownDimlet || input3.getItem() != ModItems.knownDimlet) {
            return false;
        }
        DimletKey key1 =  KnownDimletConfiguration.getDimletKey(input1, worldObj);
        DimletKey key2 =  KnownDimletConfiguration.getDimletKey(input2, worldObj);
        DimletKey key3 =  KnownDimletConfiguration.getDimletKey(input3, worldObj);
        int cntCraftable = (KnownDimletConfiguration.craftableDimlets.contains(key1) ? 1 : 0) +
                (KnownDimletConfiguration.craftableDimlets.contains(key2) ? 1 : 0) +
                (KnownDimletConfiguration.craftableDimlets.contains(key3) ? 1 : 0);
        return cntCraftable <= 1;       // Only allow at most one craftable dimlet.
    }

    private void startScrambling() {
        int rf = DimletConfiguration.rfScrambleOperation;
        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            // Not enough energy.
            return;
        }
        extractEnergy(ForgeDirection.DOWN, rf, false);

        ItemStack[] input = inventoryHelper.getStacks();

        DimletKey key1 =  KnownDimletConfiguration.getDimletKey(input[0], worldObj);
        DimletKey key2 =  KnownDimletConfiguration.getDimletKey(input[1], worldObj);
        DimletKey key3 =  KnownDimletConfiguration.getDimletKey(input[2], worldObj);

        input[0].splitStack(1);
        if (input[0].stackSize == 0) {
            input[0] = null;
        }
        input[1].splitStack(1);
        if (input[1].stackSize == 0) {
            input[1] = null;
        }
        input[2].splitStack(1);
        if (input[2].stackSize == 0) {
            input[2] = null;
        }

        int rarity1 = KnownDimletConfiguration.getEntry(key1).getRarity();
        int rarity2 = KnownDimletConfiguration.getEntry(key2).getRarity();
        int rarity3 = KnownDimletConfiguration.getEntry(key3).getRarity();
        float b = (rarity1 + rarity2 + rarity3) / 3.0f;
        bonus = (b / 50.0f) * (getInfusedFactor() / 3.0f + 1.0f);  // An average of rarity 5 will give the best bonus which is 0.1

        scrambling = 64;
        markDirty();
    }

    // Request the scrambling amount from the server. This has to be called on the client side.
    public void requestScramblingFromServer() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETSCRAMBLING,
                CLIENTCMD_GETSCRAMBLING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETSCRAMBLING.equals(command)) {
            return scrambling;
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETSCRAMBLING.equals(command)) {
            scrambling = result;
            return true;
        }
        return false;
    }


    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[] { 0, 1, 2, 3 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimletScramblerContainer.factory.isInputSlot(index) || DimletScramblerContainer.factory.isSpecificItemSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return DimletScramblerContainer.factory.isOutputSlot(index);
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
        return "Scrambler Inventory";
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
        scrambling = tagCompound.getInteger("scrambling");
        bonus = tagCompound.getFloat("bonus");
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
        tagCompound.setInteger("scrambling", scrambling);
        tagCompound.setFloat("bonus", bonus);
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
