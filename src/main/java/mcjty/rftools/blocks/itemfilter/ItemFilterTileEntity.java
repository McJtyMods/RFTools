package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;

import static mcjty.rftools.blocks.itemfilter.ItemFilterSetup.TYPE_ITEM_FILTER;

public class ItemFilterTileEntity extends GenericTileEntity {

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

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(GHOST_SIZE + BUFFER_SIZE) {
        @Override
        protected void setup() {
            box(SlotDefinition.ghost(), CONTAINER_CONTAINER, SLOT_GHOST, 24, 105, 9, 1);
            box(SlotDefinition.input(), CONTAINER_CONTAINER, SLOT_BUFFER, 24, 87, 9, 1);
            playerSlots(24, 130);
        }
    };
    private NoDirectionItemHander itemHandler = new NoDirectionItemHander(this, CONTAINER_FACTORY);
    private LazyOptional<ItemFilterInvWrapper> invHandlerN = LazyOptional.of(() -> new ItemFilterInvWrapper(this, Direction.NORTH));
    private LazyOptional<ItemFilterInvWrapper> invHandlerS = LazyOptional.of(() -> new ItemFilterInvWrapper(this, Direction.SOUTH));
    private LazyOptional<ItemFilterInvWrapper> invHandlerW = LazyOptional.of(() -> new ItemFilterInvWrapper(this, Direction.WEST));
    private LazyOptional<ItemFilterInvWrapper> invHandlerE = LazyOptional.of(() -> new ItemFilterInvWrapper(this, Direction.EAST));
    private LazyOptional<ItemFilterInvWrapper> invHandlerD = LazyOptional.of(() -> new ItemFilterInvWrapper(this, Direction.DOWN));
    private LazyOptional<ItemFilterInvWrapper> invHandlerU = LazyOptional.of(() -> new ItemFilterInvWrapper(this, Direction.UP));


    private int inputMode[] = new int[6];
    private int outputMode[] = new int[6];

    public int[] getInputMode() {
        return inputMode;
    }

    public int[] getOutputMode() {
        return outputMode;
    }

    public ItemFilterTileEntity() {
        super(TYPE_ITEM_FILTER);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        inputMode = tagCompound.getIntArray("inputs");
        outputMode = tagCompound.getIntArray("outputs");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putIntArray("inputs", inputMode);
        tagCompound.putIntArray("outputs", outputMode);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
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

    private boolean isInputMode(Direction side, int slot) {
        return (inputMode[side.ordinal()] & (1<<slot)) != 0;
    }

    private boolean isOutputMode(Direction side, int slot) {
        return (outputMode[side.ordinal()] & (1<<slot)) != 0;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            switch (facing) {
                case DOWN:  return invHandlerD.cast();
                case UP:    return invHandlerU.cast();
                case NORTH: return invHandlerN.cast();
                case SOUTH: return invHandlerS.cast();
                case WEST:  return invHandlerW.cast();
                case EAST:  return invHandlerE.cast();
            }
        }
        return super.getCapability(capability, facing);
    }

    public int[] getSlotsForFace(Direction side) {
        int v = SLOT_BUFFER;
        return new int[] { v, v+1, v+2, v+3, v+4, v+5, v+6, v+7, v+8 };
    }

    public boolean canInsertItem(int index, ItemStack stack, Direction side) {
        if (index < SLOT_BUFFER) {
            return false;
        }
        if (!isInputMode(side, index - SLOT_BUFFER)) {
            return false;
        }

        int ghostIndex = index - SLOT_BUFFER;

        ItemStack ghostStack = itemHandler.getStackInSlot(ghostIndex);
        if (ghostStack.isEmpty()) {
            // First check if there are other ghosted items for this side that match.
            // In that case we don't allow input here.
            int im = inputMode[side.ordinal()];
            for (int i = SLOT_GHOST ; i < SLOT_GHOST + GHOST_SIZE ; i++) {
                ItemStack g = itemHandler.getStackInSlot(i);
                if (!g.isEmpty() && ((im & (1<<i)) != 0) && g.isItemEqual(stack)) {
                    return false;
                }
            }
            return true;
        }
        return ghostStack.isItemEqual(stack);
    }

    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        if (index < SLOT_BUFFER) {
            return false;
        }
        return isOutputMode(direction, index - SLOT_BUFFER);
    }

    NoDirectionItemHander getItemHandler() {
        return itemHandler;
    }
}
