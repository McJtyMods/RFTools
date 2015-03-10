package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.description.DimensionDescriptor;
import com.mcjty.rftools.items.ModItems;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DimensionEnscriberTileEntity extends GenericTileEntity implements ISidedInventory {

    public static final String CMD_STORE = "store";
    public static final String CMD_EXTRACT = "extract";
    public static final String CMD_SETNAME = "setName";

    private boolean tabSlotHasChanged = false;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionEnscriberContainer.factory, DimensionEnscriberContainer.SIZE_DIMLETS+1);

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimensionEnscriberContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimensionEnscriberContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return DimensionEnscriberContainer.factory.isOutputSlot(index);
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
        return "Enscriber Inventory";
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

    private void storeDimlets() {
        DimensionDescriptor descriptor = convertToDimensionDescriptor();
        ItemStack realizedTab = createRealizedTab(descriptor);
        inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB] = realizedTab;

        markDirty();
    }

    /**
     * Create a realized dimension tab by taking a map of ids per type and storing
     * that in the NBT of the realized dimension tab.
     */
    private ItemStack createRealizedTab(DimensionDescriptor descriptor) {
        ItemStack realizedTab = new ItemStack(ModItems.realizedDimensionTab, 1, 0);
        NBTTagCompound tagCompound = new NBTTagCompound();
        descriptor.writeToNBT(tagCompound);

        // Check if the dimension already exists and if so set the progress to 100%.
        RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(worldObj);
        Integer id = manager.getDimensionID(descriptor);
        if (id != null) {
            // The dimension was already created.
            tagCompound.setInteger("ticksLeft", 0);
            tagCompound.setInteger("id", id);
        }

        realizedTab.setTagCompound(tagCompound);
        return realizedTab;
    }

    /**
     * Convert the dimlets in the inventory to a dimension descriptor.
     */
    private DimensionDescriptor convertToDimensionDescriptor() {
        List<DimensionDescriptor.DimletDescriptor> descriptors = new ArrayList<DimensionDescriptor.DimletDescriptor>();

        long forcedSeed = 0;

        for (int i = 0 ; i < DimensionEnscriberContainer.SIZE_DIMLETS ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i + DimensionEnscriberContainer.SLOT_DIMLETS];
            if (stack != null && stack.stackSize > 0) {
                DimletKey key = KnownDimletConfiguration.getDimletKey(stack, worldObj);
                descriptors.add(new DimensionDescriptor.DimletDescriptor(key.getType(), key));
                NBTTagCompound tagCompound = stack.getTagCompound();
                if (tagCompound != null && tagCompound.getLong("forcedSeed") != 0) {
                    forcedSeed = tagCompound.getLong("forcedSeed");
                }
            }
            inventoryHelper.getStacks()[i + DimensionEnscriberContainer.SLOT_DIMLETS] = null;
        }
        return new DimensionDescriptor(descriptors, forcedSeed);
    }

    private void extractDimlets() {
        ItemStack realizedTab = inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB];
        NBTTagCompound tagCompound = realizedTab.getTagCompound();
        if (tagCompound != null) {
            int idx = DimensionEnscriberContainer.SLOT_DIMLETS;
            String descriptionString = tagCompound.getString("descriptionString");
            for (DimensionDescriptor.DimletDescriptor descriptor : DimensionDescriptor.parseDescriptionString(descriptionString)) {
                inventoryHelper.getStacks()[idx++] = KnownDimletConfiguration.makeKnownDimlet(descriptor.getKey(), worldObj);
            }
        }

        inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB] = new ItemStack(ModItems.emptyDimensionTab);
        markDirty();
    }

    private void setName(String name) {
        ItemStack realizedTab = inventoryHelper.getStacks()[DimensionEnscriberContainer.SLOT_TAB];
        if (realizedTab != null) {
            NBTTagCompound tagCompound = realizedTab.getTagCompound();
            if (tagCompound == null) {
                tagCompound = new NBTTagCompound();
                realizedTab.setTagCompound(tagCompound);
            }
            tagCompound.setString("name", name);
            markDirty();
        }
    }

    @Override
    public void onSlotChanged(int index, ItemStack stack) {
        if (worldObj.isRemote && index == DimensionEnscriberContainer.SLOT_TAB) {
            tabSlotHasChanged = true;
        }
    }

    public boolean hasTabSlotChangedAndClear() {
        boolean rc = tabSlotHasChanged;
        tabSlotHasChanged = false;
        return rc;
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_STORE.equals(command)) {
            storeDimlets();
            setName(args.get("name").getString());
            return true;
        } else if (CMD_EXTRACT.equals(command)) {
            extractDimlets();
            return true;
        } else if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        }
        return false;
    }
}
