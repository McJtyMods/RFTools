package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemFilterTileEntity extends GenericTileEntity implements DefaultSidedInventory {

    public static final String CMD_SETMODE = "itemfilter.setMode";
    public static final Key<Integer> PARAM_SIDE = new Key<>("side", Type.INTEGER);
    public static final Key<Integer> PARAM_SLOT = new Key<>("slot", Type.INTEGER);
    public static final Key<Boolean> PARAM_INPUT = new Key<>("input", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_OUTPUT = new Key<>("output", Type.BOOLEAN);

    public static final int SLOT_GHOST = 0;

    public static final int BUFFER_SIZE = 9;
    public static final int GHOST_SIZE = 9;
    public static final int SLOT_PLAYERINV = GHOST_SIZE + BUFFER_SIZE;
    public static final int SLOT_BUFFER = 9;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), ContainerFactory.CONTAINER_CONTAINER, SLOT_GHOST, 24, 105, 9, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), ContainerFactory.CONTAINER_CONTAINER, SLOT_BUFFER, 24, 87, 9, 18, 1, 18);
            layoutPlayerInventorySlots(24, 130);
        }
    };
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, GHOST_SIZE + BUFFER_SIZE);

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
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        inputMode = tagCompound.getIntArray("inputs");
        outputMode = tagCompound.getIntArray("outputs");
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setIntArray("inputs", inputMode);
        tagCompound.setIntArray("outputs", outputMode);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETMODE.equals(command)) {
            Integer side = params.get(PARAM_SIDE);
            Integer slot = params.get(PARAM_SLOT);
            Boolean input = params.get(PARAM_INPUT);
            Boolean output = params.get(PARAM_OUTPUT);

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
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index < SLOT_BUFFER) {
            return false;
        }
        ItemStack ghostStack = inventoryHelper.getStackInSlot(index - SLOT_BUFFER);
        return ghostStack.isEmpty() || ghostStack.isItemEqual(stack);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        int v = SLOT_BUFFER;
        return new int[] { v, v+1, v+2, v+3, v+4, v+5, v+6, v+7, v+8 };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, Direction side) {
        if (index < SLOT_BUFFER) {
            return false;
        }
        if (!isInputMode(side, index - SLOT_BUFFER)) {
            return false;
        }

        int ghostIndex = index - SLOT_BUFFER;

        ItemStack ghostStack = inventoryHelper.getStackInSlot(ghostIndex);
        if (ghostStack.isEmpty()) {
            // First check if there are other ghosted items for this side that match.
            // In that case we don't allow input here.
            int im = inputMode[side.ordinal()];
            for (int i = SLOT_GHOST ; i < SLOT_GHOST + GHOST_SIZE ; i++) {
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
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        if (index < SLOT_BUFFER) {
            return false;
        }
        return isOutputMode(direction, index - SLOT_BUFFER);
    }

    private boolean isInputMode(Direction side, int slot) {
        return (inputMode[side.ordinal()] & (1<<slot)) != 0;
    }

    private boolean isOutputMode(Direction side, int slot) {
        return (outputMode[side.ordinal()] & (1<<slot)) != 0;
    }

    private IItemHandler invHandlerN = new ItemFilterInvWrapper(this, Direction.NORTH);
    private IItemHandler invHandlerS = new ItemFilterInvWrapper(this, Direction.SOUTH);
    private IItemHandler invHandlerW = new ItemFilterInvWrapper(this, Direction.WEST);
    private IItemHandler invHandlerE = new ItemFilterInvWrapper(this, Direction.EAST);
    private IItemHandler invHandlerD = new ItemFilterInvWrapper(this, Direction.DOWN);
    private IItemHandler invHandlerU = new ItemFilterInvWrapper(this, Direction.UP);

    @Override
    public boolean hasCapability(Capability<?> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.Direction facing) {
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
