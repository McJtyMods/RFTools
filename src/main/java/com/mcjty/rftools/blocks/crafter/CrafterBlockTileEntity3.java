package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.RedstoneMode;
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
    public static final int SPEED_SLOW = 0;
    public static final int SPEED_FAST = 1;

    public static final String CMD_MODE = "mode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CrafterContainer.factory,
            10 + CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE);
    private CraftingRecipe recipes[];
    private int supportedRecipes;

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private int speedMode = SPEED_SLOW;

    public CrafterBlockTileEntity3() {
        super(CrafterConfiguration.MAXENERGY, CrafterConfiguration.RECEIVEPERTICK);
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

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
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
        return CrafterContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return CrafterContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return CrafterContainer.factory.isOutputSlot(index);
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

        int m = tagCompound.getInteger("rsMode");
        redstoneMode = RedstoneMode.values()[m];

        speedMode = tagCompound.getByte("speedMode");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i+CrafterContainer.SLOT_BUFFER] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
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
        tagCompound.setByte("rsMode", (byte)redstoneMode.ordinal());
        tagCompound.setByte("speedMode", (byte)speedMode);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = CrafterContainer.SLOT_BUFFER ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
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

        if (redstoneMode != RedstoneMode.REDSTONE_IGNORED) {
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            boolean rs = BlockTools.getRedstoneSignal(meta);
            if (redstoneMode == RedstoneMode.REDSTONE_OFFREQUIRED && rs) {
                return;
            } else if (redstoneMode == RedstoneMode.REDSTONE_ONREQUIRED && !rs) {
                return;
            }
        }

        int steps = 1;
        if (speedMode == SPEED_FAST) {
            steps = CrafterConfiguration.speedOperations;
        }

        for (int i = 0 ; i < steps ; i++) {
            craftOneCycle();
        }
    }

    private void craftOneCycle() {
        // 0%: rf -> rf
        // 100%: rf -> rf / 2
        int rf = (int) (CrafterConfiguration.rfPerOperation * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
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
            extractEnergy(ForgeDirection.DOWN, rf, false);
        }
    }

    private boolean checkIfRecipeWorks(List<CraftingRecipe.StackWithCount> stackWithCounts, int keep) {
        for (CraftingRecipe.StackWithCount stackWithCount : stackWithCounts) {
            ItemStack stack = stackWithCount.getStack();
            int count = stackWithCount.getCount();
            for (int j = 0 ; j < CrafterContainer.BUFFER_SIZE ; j++) {
                ItemStack input = inventoryHelper.getStacks()[CrafterContainer.SLOT_BUFFER + j];
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
            for (int j = 0 ; j < CrafterContainer.BUFFER_SIZE ; j++) {
                ItemStack input = inventoryHelper.getStacks()[CrafterContainer.SLOT_BUFFER + j];
                if (input != null && input.stackSize > keep) {
                    if (OreDictionary.itemMatches(stack, input, false)) {
                        int ss = count;
                        if (input.stackSize - ss < keep) {
                            ss = input.stackSize - keep;
                        }
                        count -= ss;
                        input.splitStack(ss);        // This consumes the items
                        if (input.stackSize == 0) {
                            inventoryHelper.getStacks()[CrafterContainer.SLOT_BUFFER + j] = null;
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
            start = CrafterContainer.SLOT_BUFFER;
            stop = CrafterContainer.SLOT_BUFFER + CrafterContainer.BUFFER_SIZE;
        } else {
            start = CrafterContainer.SLOT_BUFFEROUT;
            stop = CrafterContainer.SLOT_BUFFEROUT + CrafterContainer.BUFFEROUT_SIZE;
        }
        return InventoryHelper.mergeItemStack(this, result, start, stop);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("rs").getString();
            setRedstoneMode(RedstoneMode.getMode(m));
            setSpeedMode(args.get("speed").getInteger());
            return true;
        }
        return false;
    }
}
