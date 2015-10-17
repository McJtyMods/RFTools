package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

public class ItemFilterTileEntity extends GenericTileEntity implements ISidedInventory {
    public static final String CMD_SETMODE = "setMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ItemFilterContainer.factory, ItemFilterContainer.GHOST_SIZE + ItemFilterContainer.BUFFER_SIZE);

    public static final byte OLDMODE_INPUT_EXACT = 2;
    public static final byte OLDMODE_INPUT = 1;
    public static final byte OLDMODE_DISABLED = 0;
    public static final byte OLDMODE_OUTPUT_EXACT = -1;
    public static final byte OLDMODE_OUTPUT = -2;

    private int inputMode[] = new int[6];
    private int outputMode[] = new int[6];

    public int[] getInputMode() {
        return inputMode;
    }

    public int[] getOutputMode() {
        return outputMode;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        if (tagCompound.hasKey("inputMode")) {
            // This is an old item filter that still uses input mode. We need
            // to convert it.

            // First move the six items from the old positions to the new positions.
            for (int i = ItemFilterContainer.SLOT_BUFFER+5 ; i >= ItemFilterContainer.SLOT_BUFFER ; i--) {
                inventoryHelper.setStackInSlot(i, inventoryHelper.getStackInSlot(i-3));
                inventoryHelper.setStackInSlot(i-3, null);
            }

            // Now convert the modes to the new system.
            byte[] oldInputMode = tagCompound.getByteArray("inputMode");
            for (int i = 0 ; i < 6 ; i++) {
                byte im = oldInputMode[i];
                switch (im) {
                    case OLDMODE_INPUT_EXACT:  inputMode[i] = 1 << i; outputMode[i] = 0; break;
                    case OLDMODE_INPUT:        inputMode[i] = 0x3f; outputMode[i] = 0; break;
                    case OLDMODE_OUTPUT_EXACT: inputMode[i] = 0; outputMode[i] = 1 << i; break;
                    case OLDMODE_OUTPUT:       inputMode[i] = 0; outputMode[i] = 0x3f; break;
                    case OLDMODE_DISABLED:     inputMode[i] = 0; outputMode[i] = 0; break;
                }
            }

        } else {
            inputMode = tagCompound.getIntArray("inputs");
            outputMode = tagCompound.getIntArray("outputs");
        }
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
        tagCompound.setIntArray("inputs", inputMode);
        tagCompound.setIntArray("outputs", outputMode);
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

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETMODE.equals(command)) {
            Integer side = args.get("side").getInteger();
            Integer slot = args.get("slot").getInteger();
            Boolean input = args.get("input").getBoolean();
            Boolean output = args.get("output").getBoolean();

            inputMode[side] &= ~(1 << slot);
            if (input) {
                inputMode[side] |= 1 << slot;
            }
            outputMode[side] &= ~(1 << slot);
            if (output) {
                outputMode[side] |= 1 << slot;
            }

            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }
        return false;
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
        return "Item Filter Inventory";
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
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return true;
        }
        ItemStack ghostStack = inventoryHelper.getStackInSlot(index - ItemFilterContainer.SLOT_BUFFER);
        return ghostStack == null || ghostStack.isItemEqual(stack);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        int v = ItemFilterContainer.SLOT_BUFFER;
        return new int[] { v, v+1, v+2, v+3, v+4, v+5, v+6, v+7, v+8 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        if (!isInputMode(side, index - ItemFilterContainer.SLOT_BUFFER)) {
            return false;
        }

        int ghostIndex = index - ItemFilterContainer.SLOT_BUFFER;

        ItemStack ghostStack = inventoryHelper.getStackInSlot(ghostIndex);
        if (ghostStack == null) {
            // First check if there are other ghosted items for this side that match.
            // In that case we don't allow input here.
            int im = inputMode[side];
            for (int i = ItemFilterContainer.SLOT_GHOST ; i < ItemFilterContainer.SLOT_GHOST + ItemFilterContainer.GHOST_SIZE ; i++) {
                ItemStack g = inventoryHelper.getStackInSlot(i);
                if (g != null && ((im & (1<<i)) != 0) && g.isItemEqual(stack)) {
                    return false;
                }
            }
            return true;
        }
        return ghostStack.isItemEqual(stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        return isOutputMode(side, index - ItemFilterContainer.SLOT_BUFFER);
    }

    private boolean isInputMode(int side, int slot) {
        return (inputMode[side] & (1<<slot)) != 0;
    }

    private boolean isOutputMode(int side, int slot) {
        return (outputMode[side] & (1<<slot)) != 0;
    }
}
