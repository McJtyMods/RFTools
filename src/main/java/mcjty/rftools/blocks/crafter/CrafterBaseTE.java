package mcjty.rftools.blocks.crafter;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.items.storage.StorageFilterCache;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrafterBaseTE extends GenericEnergyReceiverTileEntity implements ITickable, DefaultSidedInventory,
        JEIRecipeAcceptor {
    public static final int SPEED_SLOW = 0;
    public static final int SPEED_FAST = 1;

    public static final String CMD_MODE = "mode";
    public static final String CMD_REMEMBER = "remember";
    public static final String CMD_FORGET = "forget";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CrafterContainer.factory,
            10 + CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE + 1);

    private ItemStack[] ghostSlots = new ItemStack[CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE];

    private CraftingRecipe recipes[];
    private int supportedRecipes;

    private StorageFilterCache filterCache = null;

    private int speedMode = SPEED_SLOW;

    private InventoryCrafting workInventory = new InventoryCrafting(new Container() {
        @Override
        public boolean canInteractWith(EntityPlayer var1) {
            return false;
        }
    }, 3, 3);


    public CrafterBaseTE() {
        super(CrafterConfiguration.MAXENERGY, CrafterConfiguration.RECEIVEPERTICK);
        setSupportedRecipes(8);
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    public ItemStack[] getGhostSlots() {
        return ghostSlots;
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        setInventorySlotContents(CrafterContainer.SLOT_CRAFTOUTPUT, stacks.get(0));
        for (int i = 1 ; i < stacks.size() ; i++) {
            setInventorySlotContents(CrafterContainer.SLOT_CRAFTINPUT + i-1, stacks.get(i));
        }
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

    public int getSpeedMode() {
        return speedMode;
    }

    public void setSpeedMode(int speedMode) {
        this.speedMode = speedMode;
        markDirtyClient();
    }

    public CraftingRecipe getRecipe(int index) {
        return recipes[index];
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == CrafterContainer.SLOT_FILTER_MODULE) {
            filterCache = null;
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }


    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(CrafterContainer.SLOT_FILTER_MODULE));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index >= CrafterContainer.SLOT_BUFFER && index < CrafterContainer.SLOT_BUFFEROUT) {
            ItemStack ghostSlot = ghostSlots[index - CrafterContainer.SLOT_BUFFER];
            if (ghostSlot != null) {
                if (!ghostSlot.isItemEqual(stack)) {
                    return false;
                }
            }
            if (inventoryHelper.containsItem(CrafterContainer.SLOT_FILTER_MODULE)) {
                getFilterCache();
                if (filterCache != null) {
                    return filterCache.match(stack);
                }
            }
        } else if (index >= CrafterContainer.SLOT_BUFFEROUT && index < CrafterContainer.SLOT_FILTER_MODULE) {
            ItemStack ghostSlot = ghostSlots[index - CrafterContainer.SLOT_BUFFEROUT + CrafterContainer.BUFFER_SIZE];
            if (ghostSlot != null) {
                if (!ghostSlot.isItemEqual(stack)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return CrafterContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        if (!isItemValidForSlot(index, stack)) {
            return false;
        }
        return CrafterContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return CrafterContainer.factory.isOutputSlot(index);
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        readGhostBufferFromNBT(tagCompound);
        readRecipesFromNBT(tagCompound);

        speedMode = tagCompound.getByte("speedMode");
    }

    private void readGhostBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("GItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            ghostSlots[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
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
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        writeGhostBufferToNBT(tagCompound);
        writeRecipesToNBT(tagCompound);
        tagCompound.setByte("speedMode", (byte) speedMode);
    }

    private void writeGhostBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < ghostSlots.length ; i++) {
            ItemStack stack = ghostSlots[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("GItems", bufferTagList);
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
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        if (!isMachineEnabled()) {
            return;
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

        if (getEnergyStored(EnumFacing.DOWN) < rf) {
            return;
        }

        boolean energyConsumed = false;

        for (int index = 0 ; index < supportedRecipes ; index++) {
            CraftingRecipe craftingRecipe = recipes[index];

            if (craftingRecipe != null) {
                if (craftOneItemNew(craftingRecipe)) {
                    energyConsumed = true;
                }
            }
        }

        if (energyConsumed) {
            consumeEnergy(rf);
        }
    }

    private boolean craftOneItemNew(CraftingRecipe craftingRecipe) {
        IRecipe recipe = craftingRecipe.getCachedRecipe(worldObj);
        if (recipe == null) {
            return false;
        }

        Map<Integer,ItemStack> undo = new HashMap<Integer, ItemStack>();

        if (!testAndConsumeCraftingItems(craftingRecipe, undo, true)) {
            undo(undo);
            if (!testAndConsumeCraftingItems(craftingRecipe, undo, false)) {
                undo(undo);
                return false;
            }
        }

//        ItemStack result = recipe.getCraftingResult(craftingRecipe.getInventory());
        ItemStack result = null;
        try {
            result = recipe.getCraftingResult(workInventory);
        } catch (Exception e) {
            // Ignore this error for now to make sure we don't crash on bad recipes.
            Logging.log("Problem with recipe!");
        }

        // Try to merge the output. If there is something that doesn't fit we undo everything.
        if (result != null && placeResult(craftingRecipe.isCraftInternal(), result, undo)) {
            ItemStack[] remaining = recipe.getRemainingItems(workInventory);
            if (remaining != null) {
                for (ItemStack s : remaining) {
                    if (s != null) {
                        if (!placeResult(craftingRecipe.isCraftInternal(), s, undo)) {
                            // Not enough room.
                            undo(undo);
                            return false;
                        }
                    }
                }
            }

            return true;
        } else {
            // We don't have place. Undo the operation.
            undo(undo);
            return false;
        }
    }

    private static boolean match(ItemStack target, ItemStack input, boolean strictDamage) {
        if (strictDamage) {
            return OreDictionary.itemMatches(target, input, false);
        } else {
            if ((input == null && target != null) || (input != null && target == null)) {
                return false;
            }
            return target.getItem() == input.getItem();

        }
    }

    private boolean testAndConsumeCraftingItems(CraftingRecipe craftingRecipe, Map<Integer, ItemStack> undo, boolean strictDamage) {
        int keep = craftingRecipe.isKeepOne() ? 1 : 0;
        InventoryCrafting inventory = craftingRecipe.getInventory();

        for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null) {
                int count = stack.stackSize;
                for (int j = 0 ; j < CrafterContainer.BUFFER_SIZE ; j++) {
                    int slotIdx = CrafterContainer.SLOT_BUFFER + j;
                    ItemStack input = inventoryHelper.getStackInSlot(slotIdx);
                    if (input != null && input.stackSize > keep) {
                        if (match(stack, input, strictDamage)) {
                            workInventory.setInventorySlotContents(i, input.copy());
                            int ss = count;
                            if (input.stackSize - ss < keep) {
                                ss = input.stackSize - keep;
                            }
                            count -= ss;
                            if (!undo.containsKey(slotIdx)) {
                                undo.put(slotIdx, input.copy());
                            }
                            input.splitStack(ss);        // This consumes the items
                            if (input.stackSize == 0) {
                                inventoryHelper.setStackInSlot(slotIdx, null);
                            }
                        }
                    }
                    if (count == 0) {
                        break;
                    }
                }
                if (count > 0) {
                    return false;   // Couldn't find all items.
                }
            } else {
                workInventory.setInventorySlotContents(i, null);
            }
        }

        return true;
    }


    private void undo(Map<Integer,ItemStack> undo) {
        for (Map.Entry<Integer, ItemStack> entry : undo.entrySet()) {
            inventoryHelper.setStackInSlot(entry.getKey(), entry.getValue());
        }
    }

    private boolean placeResult(boolean internal, ItemStack result, Map<Integer,ItemStack> undo) {
        int start;
        int stop;
        if (internal) {
            start = CrafterContainer.SLOT_BUFFER;
            stop = CrafterContainer.SLOT_BUFFER + CrafterContainer.BUFFER_SIZE;
        } else {
            start = CrafterContainer.SLOT_BUFFEROUT;
            stop = CrafterContainer.SLOT_BUFFEROUT + CrafterContainer.BUFFEROUT_SIZE;
        }
        return InventoryHelper.mergeItemStack(this, true, result, start, stop, undo) == 0;
    }

    private void rememberItems() {
        for (int i = 0 ; i < ghostSlots.length ; i++) {
            int slotIdx;
            if (i < CrafterContainer.BUFFER_SIZE) {
                slotIdx = i + CrafterContainer.SLOT_BUFFER;
            } else {
                slotIdx = i + CrafterContainer.SLOT_BUFFEROUT - CrafterContainer.BUFFER_SIZE;
            }
            if (inventoryHelper.containsItem(slotIdx)) {
                ItemStack stack = inventoryHelper.getStackInSlot(slotIdx);
                ghostSlots[i] = stack.copy();
                ghostSlots[i].stackSize = 1;
            }
        }
        markDirtyClient();
    }

    private void forgetItems() {
        for (int i = 0 ; i < ghostSlots.length ; i++) {
            ghostSlots[i] = null;
        }
        markDirtyClient();
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            setSpeedMode(args.get("speed").getInteger());
            return true;
        } else if (CMD_REMEMBER.equals(command)) {
            rememberItems();
            return true;
        } else if (CMD_FORGET.equals(command)) {
            forgetItems();
            return true;
        }
        return false;
    }
}
