package mcjty.rftools.blocks.infuser;

import mcjty.lib.api.Infusable;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.blocks.dimlets.DimletSetup;
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

public class MachineInfuserTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, MachineInfuserContainer.factory, 2);

    private int infusing = 0;

    public MachineInfuserTileEntity() {
        super(DimletConfiguration.INFUSER_MAXENERGY, DimletConfiguration.INFUSER_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        if (infusing > 0) {
            infusing--;
            if (infusing == 0) {
                ItemStack outputStack = inventoryHelper.getStackInSlot(1);
                finishInfusing(outputStack);
            }
            markDirty();
        } else {
            ItemStack inputStack = inventoryHelper.getStackInSlot(0);
            ItemStack outputStack = inventoryHelper.getStackInSlot(1);
            if (inputStack != null && inputStack.getItem() == DimletSetup.dimensionalShard && isInfusable(outputStack)) {
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
        if (infused >= GeneralConfig.maxInfuse) {
            return false;   // Already infused to the maximum.
        }
        return true;
    }

    private NBTTagCompound getTagCompound(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        if (stack.stackSize != 1) {
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
        stack.setTagCompound(tagCompound);
    }

    private void startInfusing() {
        int rf = DimletConfiguration.rfInfuseOperation;
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
        infusing = 5;
        markDirty();
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return new int[] { MachineInfuserContainer.SLOT_SHARDINPUT, MachineInfuserContainer.SLOT_MACHINEOUTPUT};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return MachineInfuserContainer.factory.isInputSlot(index) || MachineInfuserContainer.factory.isSpecificItemSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return MachineInfuserContainer.factory.isOutputSlot(index);
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
        return canPlayerAccess(player);
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
        tagCompound.setInteger("infusing", infusing);
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
