package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.InventoryTools;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Map;

public class CrafterBlockTileEntity3 extends GenericEnergyHandlerTileEntity implements ISidedInventory {
    public static final int REDSTONE_IGNORED = 0;
    public static final int REDSTONE_OFFREQUIRED = 1;
    public static final int REDSTONE_ONREQUIRED = 2;
    public static final int SPEED_SLOW = 0;
    public static final int SPEED_FAST = 1;

    public static final String CMD_MODE = "mode";

    private ItemStack stacks[] = new ItemStack[10 + CrafterContainerFactory.BUFFER_SIZE + CrafterContainerFactory.BUFFEROUT_SIZE];
    private CraftingRecipe recipes[];
    private int supportedRecipes;

    private int redstoneMode = REDSTONE_IGNORED;
    private int speedMode = SPEED_SLOW;

    public static int MAXENERGY = 32000;
    public static int RECEIVEPERTICK = 80;
    public static int rfPerOperation = 100;
    public static int speedOperations = 5;

    public CrafterBlockTileEntity3() {
        super(MAXENERGY, RECEIVEPERTICK);
        setSupportedRecipes(8);
    }

    public void setSupportedRecipes(int supportedRecipes) {
        this.supportedRecipes = supportedRecipes;
        recipes =  new CraftingRecipe[supportedRecipes];
        for (int i = 0 ; i < recipes.length ; i++) {
            recipes[i] = new CraftingRecipe();
        }
    }

    public int getSupportedRecipes() {
        return supportedRecipes;
    }

    public int getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(int redstoneMode) {
        this.redstoneMode = redstoneMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getSpeedMode() {
        return speedMode;
    }

    public void setSpeedMode(int speedMode) {
        this.speedMode = speedMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public CraftingRecipe getRecipe(int index) {
        return recipes[index];
    }

    @Override
    public int getSizeInventory() {
        return stacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (CrafterContainerFactory.getInstance().isGhostSlot(index) || CrafterContainerFactory.getInstance().isGhostOutputSlot(index)) {
            ItemStack old = stacks[index];
            stacks[index] = null;
            if (old == null) {
                return null;
            }
            old.stackSize = 0;
            return old;
        } else {
            if (stacks[index] != null) {
                if (stacks[index].stackSize <= amount) {
                    ItemStack old = stacks[index];
                    stacks[index] = null;
                    markDirty();
                    return old;
                }
                ItemStack its = stacks[index].splitStack(amount);
                if (stacks[index].stackSize == 0) {
                    stacks[index] = null;
                }
                markDirty();
                return its;
            }
            return null;
        }
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (CrafterContainerFactory.getInstance().isGhostSlot(index)) {
            if (stack != null) {
                stacks[index] = stack.copy();
                if (index < 9) {
                    stacks[index].stackSize = 1;
                }
            } else {
                stacks[index] = null;
            }
        } else if (CrafterContainerFactory.getInstance().isGhostOutputSlot(index)) {
            if (stack != null) {
                stacks[index] = stack.copy();
            } else {
                stacks[index] = null;
            }
        } else {
            stacks[index] = stack;
            if (stack != null && stack.stackSize > getInventoryStackLimit()) {
                stack.stackSize = getInventoryStackLimit();
            }
            markDirty();
        }
    }

    @Override
    public String getInventoryName() {
        return "Crafter Inventory";
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
    public int[] getAccessibleSlotsFromSide(int side) {
        return CrafterContainerFactory.getInstance().getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return CrafterContainerFactory.getInstance().isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return CrafterContainerFactory.getInstance().isOutputSlot(index);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        readRecipesFromNBT(tagCompound);
        redstoneMode = tagCompound.getByte("rsMode");
        speedMode = tagCompound.getByte("speedMode");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            stacks[i+CrafterContainerFactory.SLOT_BUFFER] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    private void readRecipesFromNBT(NBTTagCompound tagCompound) {
        NBTTagList recipeTagList = tagCompound.getTagList("Recipes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < recipeTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = recipeTagList.getCompoundTagAt(i);
            recipes[i].readFromNBT(nbtTagCompound);
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
        writeRecipesToNBT(tagCompound);
        tagCompound.setByte("rsMode", (byte)redstoneMode);
        tagCompound.setByte("speedMode", (byte)speedMode);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = CrafterContainerFactory.SLOT_BUFFER ; i < stacks.length ; i++) {
            ItemStack stack = stacks[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    private void writeRecipesToNBT(NBTTagCompound tagCompound) {
        NBTTagList recipeTagList = new NBTTagList();
        for (CraftingRecipe recipe : recipes) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            recipe.writeToNBT(nbtTagCompound);
            recipeTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Recipes", recipeTagList);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        if (redstoneMode != REDSTONE_IGNORED) {
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            boolean rs = BlockTools.getRedstoneSignal(meta);
            if (redstoneMode == REDSTONE_OFFREQUIRED && rs) {
                return;
            } else if (redstoneMode == REDSTONE_ONREQUIRED && !rs) {
                return;
            }
        }

        int steps = 1;
        if (speedMode == SPEED_FAST) {
            steps = speedOperations;
        }

        for (int i = 0 ; i < steps ; i++) {
            craftOneCycle();
        }
    }

    private void craftOneCycle() {
        if (getEnergyStored(ForgeDirection.DOWN) < rfPerOperation) {
            return;
        }

        boolean energyConsumed = false;

        for (int index = 0 ; index < supportedRecipes ; index++) {
            CraftingRecipe craftingRecipe = recipes[index];

            if (craftingRecipe != null) {
                IRecipe recipe = craftingRecipe.getCachedRecipe(worldObj);
                if (recipe != null) {
                    List<CraftingRecipe.StackWithCount> stackWithCounts = craftingRecipe.getStacksWithCount();
                    int keep = craftingRecipe.isKeepOne() ? 1 : 0;
                    if (checkIfRecipeWorks(stackWithCounts, keep)) {
                        ItemStack result = craftingRecipe.getResult();
                        // First check if we have room for the result. If yes we can actually craft.
                        boolean internal = craftingRecipe.isCraftInternal();
                        if (placeResult(internal, result)) {
                            consumeCraftingItems(stackWithCounts, keep);
                            energyConsumed = true;
                        }
                    }
                }
            }
        }

        if (energyConsumed) {
            extractEnergy(ForgeDirection.DOWN, rfPerOperation, false);
        } else {
            return;
        }
    }

    private boolean checkIfRecipeWorks(List<CraftingRecipe.StackWithCount> stackWithCounts, int keep) {
        for (CraftingRecipe.StackWithCount stackWithCount : stackWithCounts) {
            ItemStack stack = stackWithCount.getStack();
            int count = stackWithCount.getCount();
            for (int j = 0 ; j < CrafterContainerFactory.BUFFER_SIZE ; j++) {
                ItemStack input = stacks[CrafterContainerFactory.SLOT_BUFFER + j];
                if (input != null && input.stackSize > keep) {
                    if (OreDictionary.itemMatches(stack, input, false)) {
                        int ss = count;
                        if (input.stackSize - ss < keep) {
                            ss = input.stackSize - keep;
                        }
                        count -= ss;
                    }
                }
            }
            if (count > 0) {
                return false;       // We couldn't find all needed items
            }
        }
        return true;
    }

    private void consumeCraftingItems(List<CraftingRecipe.StackWithCount> stackWithCounts, int keep) {
        for (CraftingRecipe.StackWithCount stackWithCount : stackWithCounts) {
            ItemStack stack = stackWithCount.getStack();
            int count = stackWithCount.getCount();
            for (int j = 0 ; j < CrafterContainerFactory.BUFFER_SIZE ; j++) {
                ItemStack input = stacks[CrafterContainerFactory.SLOT_BUFFER + j];
                if (input != null && input.stackSize > keep) {
                    if (OreDictionary.itemMatches(stack, input, false)) {
                        int ss = count;
                        if (input.stackSize - ss < keep) {
                            ss = input.stackSize - keep;
                        }
                        count -= ss;
                        input.splitStack(ss);        // This consumes the items
                        if (input.stackSize == 0) {
                            stacks[CrafterContainerFactory.SLOT_BUFFER + j] = null;
                        }
                    }
                }
            }
        }
    }

    private boolean placeResult(boolean internal, ItemStack result) {
        int start;
        int stop;
        if (internal) {
            start = CrafterContainerFactory.SLOT_BUFFER;
            stop = CrafterContainerFactory.SLOT_BUFFER + CrafterContainerFactory.BUFFER_SIZE;
        } else {
            start = CrafterContainerFactory.SLOT_BUFFEROUT;
            stop = CrafterContainerFactory.SLOT_BUFFEROUT + CrafterContainerFactory.BUFFEROUT_SIZE;
        }
        return InventoryTools.mergeItemStack(this, result, start, stop);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            setRedstoneMode(args.get("rs").getInteger());
            setSpeedMode(args.get("speed").getInteger());
            return true;
        }
        return false;
    }
}
