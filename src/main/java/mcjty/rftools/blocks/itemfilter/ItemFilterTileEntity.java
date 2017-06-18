package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;

public class ItemFilterTileEntity extends GenericTileEntity implements DefaultSidedInventory {
    public static final String CMD_SETMODE = "setMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ItemFilterContainer.factory, ItemFilterContainer.GHOST_SIZE + ItemFilterContainer.BUFFER_SIZE);

    private int inputMode[] = new int[6];
    private int outputMode[] = new int[6];

    public int[] getInputMode() {
        return inputMode;
    }

    public int[] getOutputMode() {
        return outputMode;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        inputMode = tagCompound.getIntArray("inputs");
        outputMode = tagCompound.getIntArray("outputs");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setIntArray("inputs", inputMode);
        tagCompound.setIntArray("outputs", outputMode);
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

            markDirtyClient();
            return true;
        }
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        ItemStack ghostStack = inventoryHelper.getStackInSlot(index - ItemFilterContainer.SLOT_BUFFER);
        return ghostStack.isEmpty() || ghostStack.isItemEqual(stack);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        int v = ItemFilterContainer.SLOT_BUFFER;
        return new int[] { v, v+1, v+2, v+3, v+4, v+5, v+6, v+7, v+8 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing side) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        if (!isInputMode(side, index - ItemFilterContainer.SLOT_BUFFER)) {
            return false;
        }

        int ghostIndex = index - ItemFilterContainer.SLOT_BUFFER;

        ItemStack ghostStack = inventoryHelper.getStackInSlot(ghostIndex);
        if (ghostStack.isEmpty()) {
            // First check if there are other ghosted items for this side that match.
            // In that case we don't allow input here.
            int im = inputMode[side.ordinal()];
            for (int i = ItemFilterContainer.SLOT_GHOST ; i < ItemFilterContainer.SLOT_GHOST + ItemFilterContainer.GHOST_SIZE ; i++) {
                ItemStack g = inventoryHelper.getStackInSlot(i);
                if (!g.isEmpty() && ((im & (1<<i)) != 0) && g.isItemEqual(stack)) {
                    return false;
                }
            }
            return true;
        }
        return ghostStack.isItemEqual(stack);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        if (index < ItemFilterContainer.SLOT_BUFFER) {
            return false;
        }
        return isOutputMode(direction, index - ItemFilterContainer.SLOT_BUFFER);
    }

    private boolean isInputMode(EnumFacing side, int slot) {
        return (inputMode[side.ordinal()] & (1<<slot)) != 0;
    }

    private boolean isOutputMode(EnumFacing side, int slot) {
        return (outputMode[side.ordinal()] & (1<<slot)) != 0;
    }

    private IItemHandler invHandlerN = new ItemFilterInvWrapper(this, EnumFacing.NORTH);
    private IItemHandler invHandlerS = new ItemFilterInvWrapper(this, EnumFacing.SOUTH);
    private IItemHandler invHandlerW = new ItemFilterInvWrapper(this, EnumFacing.WEST);
    private IItemHandler invHandlerE = new ItemFilterInvWrapper(this, EnumFacing.EAST);
    private IItemHandler invHandlerD = new ItemFilterInvWrapper(this, EnumFacing.DOWN);
    private IItemHandler invHandlerU = new ItemFilterInvWrapper(this, EnumFacing.UP);

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            switch (facing) {
                case DOWN:
                    return (T) invHandlerD;
                case UP:
                    return (T) invHandlerU;
                case NORTH:
                    return (T) invHandlerN;
                case SOUTH:
                    return (T) invHandlerS;
                case WEST:
                    return (T) invHandlerW;
                case EAST:
                    return (T) invHandlerE;
            }
        }
        return super.getCapability(capability, facing);
    }
}
