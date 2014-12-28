package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.dimension.DimensionDescriptor;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
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

import java.util.Map;
import java.util.Random;

public class DimensionEditorTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_GETEDITING = "getEditing";
    public static final String CLIENTCMD_GETEDITING = "getEditing";

    private static int editPercentage = 0;
    private int ticker = 5;

    private int target = 0;
    private boolean isEditing = false;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionEditorContainer.factory, 1);

    public DimensionEditorTileEntity() {
        super(DimletConfiguration.EDITOR_MAXENERGY, DimletConfiguration.EDITOR_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        if (!isEditing) {
            return;
        }

        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = 5;

        NBTTagCompound tagCompound = validateItemStack();
        if (tagCompound == null) return;

        int editTicksLeft = tagCompound.getInteger("editTicksLeft");
        int editTickCost = tagCompound.getInteger("editTickCost");
        if (editTicksLeft > 0) {
            editTicksLeft = editDimensionTick(tagCompound, editTicksLeft);
        } else {
            stopEditing();
            return;
        }

        setState(editTicksLeft, editTickCost);
    }

    private NBTTagCompound validateItemStack() {
        ItemStack itemStack = inventoryHelper.getStacks()[0];
        if (itemStack == null || itemStack.stackSize == 0) {
            stopEditing();
            return null;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int ticksLeft = tagCompound.getInteger("ticksLeft");

        if (ticksLeft > 0) {
            // This tab is in building progress. Don't try to edit it.
            stopEditing();
            return null;
        }

        if (target == 0) {
            // There is no valid target. Don't try to edit.
            stopEditing();
            return null;
        }
        return tagCompound;
    }

    private void stopEditing() {
        setState(-1, 0);
        isEditing = false;
        markDirty();
    }

    private static Random random = new Random();

    private int editDimensionTick(NBTTagCompound tagCompound, int editTicksLeft) {
        int createCost = tagCompound.getInteger("rfCreateCost");
        createCost = (int) (createCost * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) >= createCost) {
            extractEnergy(ForgeDirection.DOWN, createCost, false);
            editTicksLeft--;
            if (random.nextFloat() < getInfusedFactor()) {
                // Randomly reduce another tick if the device is infused.
                editTicksLeft--;
                if (editTicksLeft < 0) {
                    editTicksLeft = 0;
                }
            }
            tagCompound.setInteger("editTicksLeft", editTicksLeft);
            if (editTicksLeft <= 0) {
                // @todo The actual editing goes here!
                RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(worldObj);
                DimensionDescriptor descriptor = new DimensionDescriptor(tagCompound);
                String name = tagCompound.getString("name");
                int id = manager.createNewDimension(worldObj, descriptor, name);
                tagCompound.setInteger("id", id);
            }
        }
        return editTicksLeft;
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
        return "Editor Inventory";
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
    public void requestBuildingPercentage() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETEDITING,
                CLIENTCMD_GETEDITING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETEDITING.equals(command)) {
            ItemStack itemStack = inventoryHelper.getStacks()[0];
            if (itemStack == null || itemStack.stackSize == 0) {
                return 0;
            } else {
                NBTTagCompound tagCompound = itemStack.getTagCompound();
                int editTicksLeft = tagCompound.getInteger("editTicksLeft");
                int editTickCost = tagCompound.getInteger("editTickCost");
                return (editTickCost - editTicksLeft) * 100 / editTickCost;
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
        if (CLIENTCMD_GETEDITING.equals(command)) {
            editPercentage = result;
            return true;
        }
        return false;
    }

    public static int getEditPercentage() {
        return editPercentage;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        target = tagCompound.getInteger("target");
        isEditing = tagCompound.getBoolean("editing");
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
        tagCompound.setInteger("target", target);
        tagCompound.setBoolean("editing", isEditing);
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
