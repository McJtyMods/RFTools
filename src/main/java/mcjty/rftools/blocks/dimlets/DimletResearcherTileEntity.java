package mcjty.rftools.blocks.dimlets;

import mcjty.container.InventoryHelper;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.items.dimlets.DimletKey;
import mcjty.rftools.items.dimlets.DimletRandomizer;
import mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import mcjty.network.Argument;
import mcjty.network.PacketHandler;
import mcjty.network.PacketRequestIntegerFromServer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class DimletResearcherTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory {

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
                InventoryHelper.mergeItemStack(this, KnownDimletConfiguration.makeKnownDimlet(key, worldObj), 1, 2, null);
            }
            markDirty();
        } else {
            ItemStack inputStack = inventoryHelper.getStackInSlot(0);
            ItemStack outputStack = inventoryHelper.getStackInSlot(1);
            if (inputStack != null && inputStack.getItem() == DimletSetup.unknownDimlet && outputStack == null) {
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
        consumeEnergy(rf);

        inventoryHelper.getStackInSlot(0).splitStack(1);
        if (inventoryHelper.getStackInSlot(0).stackSize == 0) {
            inventoryHelper.setStackInSlot(0, null);
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
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
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
            inventoryHelper.setStackInSlot(i, ItemStack.loadItemStackFromNBT(nbtTagCompound));
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
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }
}
