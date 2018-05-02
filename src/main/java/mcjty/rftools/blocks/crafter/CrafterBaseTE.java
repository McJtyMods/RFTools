package mcjty.rftools.blocks.crafter;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.NullSidedInvWrapper;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.craftinggrid.CraftingRecipe;
import mcjty.rftools.items.storage.StorageFilterCache;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import mcjty.typed.TypedMap;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.rftools.craftinggrid.CraftingRecipe.CraftMode.EXTC;
import static mcjty.rftools.craftinggrid.CraftingRecipe.CraftMode.INT;

public class CrafterBaseTE extends GenericEnergyReceiverTileEntity implements ITickable, DefaultSidedInventory,
        JEIRecipeAcceptor {
    public static final int SPEED_SLOW = 0;
    public static final int SPEED_FAST = 1;

    public static final String CMD_MODE = "crafter.setMode";
    public static final String CMD_RSMODE = "crafter.setRsMode";
    public static final String CMD_REMEMBER = "crafter.remember";
    public static final String CMD_FORGET = "crafter.forget";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CrafterContainer.CONTAINER_FACTORY,
            10 + CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE + 1);

    private ItemStackList ghostSlots = ItemStackList.create(CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE);

    private final CraftingRecipe[] recipes;

    private StorageFilterCache filterCache = null;

    private int speedMode = SPEED_SLOW;

    // If the crafter tries to craft something, but there's nothing it can make,
    // this gets set to true, preventing further ticking. It gets cleared whenever
    // any of its inventories or recipes change.
    /*package*/ boolean noRecipesWork = false;

    private InventoryCrafting workInventory = new InventoryCrafting(new Container() {
        @SuppressWarnings("NullableProblems")
        @Override
        public boolean canInteractWith(EntityPlayer var1) {
            return false;
        }
    }, 3, 3);

    public CrafterBaseTE(int supportedRecipes) {
        super(CrafterConfiguration.MAXENERGY, CrafterConfiguration.RECEIVEPERTICK);
        recipes = new CraftingRecipe[supportedRecipes];
        for (int i = 0; i < recipes.length; ++i) {
            recipes[i] = new CraftingRecipe();
        }
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    public ItemStackList getGhostSlots() {
        return ghostSlots;
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        setInventorySlotContents(CrafterContainer.SLOT_CRAFTOUTPUT, stacks.get(0));
        for (int i = 1 ; i < stacks.size() ; i++) {
            setInventorySlotContents(CrafterContainer.SLOT_CRAFTINPUT + i-1, stacks.get(i));
        }
    }

    public void selectRecipe(int index) {
        CraftingRecipe recipe = recipes[index];
        setInventorySlotContents(CrafterContainer.SLOT_CRAFTOUTPUT, recipe.getResult());
        InventoryCrafting inv = recipe.getInventory();
        int size = inv.getSizeInventory();
        for (int i = 0 ; i < size ; ++i) {
            setInventorySlotContents(CrafterContainer.SLOT_CRAFTINPUT + i, inv.getStackInSlot(i));
        }
    }

    public int getSupportedRecipes() {
        return recipes.length;
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
        noRecipesWork = false;
        if (index == CrafterContainer.SLOT_FILTER_MODULE) {
            filterCache = null;
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        noRecipesWork = false;
        if (index == CrafterContainer.SLOT_FILTER_MODULE) {
            filterCache = null;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        noRecipesWork = false;
        if (index == CrafterContainer.SLOT_FILTER_MODULE) {
            filterCache = null;
        }
        return inventoryHelper.removeStackFromSlot(index);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(CrafterContainer.SLOT_FILTER_MODULE));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index >= CrafterContainer.SLOT_CRAFTINPUT && index <= CrafterContainer.SLOT_CRAFTOUTPUT) {
            return false;
        }
        if (index >= CrafterContainer.SLOT_BUFFER && index < CrafterContainer.SLOT_BUFFEROUT) {
            ItemStack ghostSlot = ghostSlots.get(index - CrafterContainer.SLOT_BUFFER);
            if (!ghostSlot.isEmpty()) {
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
            ItemStack ghostSlot = ghostSlots.get(index - CrafterContainer.SLOT_BUFFEROUT + CrafterContainer.BUFFER_SIZE);
            if (!ghostSlot.isEmpty()) {
                if (!ghostSlot.isItemEqual(stack)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return CrafterContainer.CONTAINER_FACTORY.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        if (!isItemValidForSlot(index, stack)) {
            return false;
        }
        return CrafterContainer.CONTAINER_FACTORY.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return CrafterContainer.CONTAINER_FACTORY.isOutputSlot(index);
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
            ghostSlots.set(i, new ItemStack(nbtTagCompound));
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
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        writeGhostBufferToNBT(tagCompound);
        writeRecipesToNBT(tagCompound);
        tagCompound.setByte("speedMode", (byte) speedMode);
    }

    private void writeGhostBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (ItemStack stack : ghostSlots) {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (!stack.isEmpty()) {
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
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        if (!isMachineEnabled() || noRecipesWork) {
            return;
        }

        // 0%: rf -> rf
        // 100%: rf -> rf / 2
        int rf = (int) (CrafterConfiguration.rfPerOperation * (2.0f - getInfusedFactor()) / 2.0f);

        int steps = Math.min(rf == 0 ? Integer.MAX_VALUE : getEnergyStored() / rf, speedMode == SPEED_FAST ? CrafterConfiguration.speedOperations : 1);

        int i;
        for (i = 0; i < steps; ++i) {
            if(!craftOneCycle()) {
                noRecipesWork = true;
                break;
            }
        }
        rf *= i;
        if(rf > 0) {
            consumeEnergy(rf);
        }
    }

    private boolean craftOneCycle() {
        boolean craftedAtLeastOneThing = false;

        for (CraftingRecipe craftingRecipe : recipes) {
            if (craftOneItemNew(craftingRecipe)) {
                craftedAtLeastOneThing = true;
            }
        }

        return craftedAtLeastOneThing;
    }

    private boolean craftOneItemNew(CraftingRecipe craftingRecipe) {
        IRecipe recipe = craftingRecipe.getCachedRecipe(getWorld());
        if (recipe == null) {
            return false;
        }

        Map<Integer,ItemStack> undo = new HashMap<>();

        if (!testAndConsumeCraftingItems(craftingRecipe, undo, true)) {
            undo(undo);
            if (!testAndConsumeCraftingItems(craftingRecipe, undo, false)) {
                undo(undo);
                return false;
            }
        }

//        ItemStack result = recipe.getCraftingResult(craftingRecipe.getInventory());
        ItemStack result = ItemStack.EMPTY;
        try {
            result = recipe.getCraftingResult(workInventory);
        } catch (RuntimeException e) {
            // Ignore this error for now to make sure we don't crash on bad recipes.
            Logging.logError("Problem with recipe!", e);
        }

        // Try to merge the output. If there is something that doesn't fit we undo everything.
        CraftingRecipe.CraftMode mode = craftingRecipe.getCraftMode();
        if (!result.isEmpty() && placeResult(mode, result, undo)) {
            List<ItemStack> remaining = recipe.getRemainingItems(workInventory);
            if (remaining != null) {
                CraftingRecipe.CraftMode remainingMode = mode == EXTC ? INT : mode;
                for (ItemStack s : remaining) {
                    if (!s.isEmpty()) {
                        if (!placeResult(remainingMode, s, undo)) {
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
            if ((input.isEmpty() && !target.isEmpty())
                    || (!input.isEmpty() && target.isEmpty())) {
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
            if (!stack.isEmpty()) {
                int count = stack.getCount();
                for (int j = 0 ; j < CrafterContainer.BUFFER_SIZE ; j++) {
                    int slotIdx = CrafterContainer.SLOT_BUFFER + j;
                    ItemStack input = inventoryHelper.getStackInSlot(slotIdx);
                    if (!input.isEmpty() && input.getCount() > keep) {
                        if (match(stack, input, strictDamage)) {
                            workInventory.setInventorySlotContents(i, input.copy());
                            int ss = count;
                            if (input.getCount() - ss < keep) {
                                ss = input.getCount() - keep;
                            }
                            count -= ss;
                            if (!undo.containsKey(slotIdx)) {
                                undo.put(slotIdx, input.copy());
                            }
                            input.splitStack(ss);        // This consumes the items
                            if (input.isEmpty()) {
                                inventoryHelper.setStackInSlot(slotIdx, ItemStack.EMPTY);
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
                workInventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }

        IRecipe recipe = craftingRecipe.getCachedRecipe(getWorld());
        return recipe.matches(workInventory, getWorld());
    }


    private void undo(Map<Integer,ItemStack> undo) {
        for (Map.Entry<Integer, ItemStack> entry : undo.entrySet()) {
            inventoryHelper.setStackInSlot(entry.getKey(), entry.getValue());
        }
        undo.clear();
    }

    private boolean placeResult(CraftingRecipe.CraftMode mode, ItemStack result, Map<Integer,ItemStack> undo) {
        int start;
        int stop;
        if (mode == INT) {
            start = CrafterContainer.SLOT_BUFFER;
            stop = CrafterContainer.SLOT_BUFFER + CrafterContainer.BUFFER_SIZE;
        } else {
            // EXT and EXTC are handled the same here
            start = CrafterContainer.SLOT_BUFFEROUT;
            stop = CrafterContainer.SLOT_BUFFEROUT + CrafterContainer.BUFFEROUT_SIZE;
        }
        return InventoryHelper.mergeItemStack(this, true, result, start, stop, undo) == 0;
    }

    private void rememberItems() {
        for (int i = 0 ; i < ghostSlots.size() ; i++) {
            int slotIdx;
            if (i < CrafterContainer.BUFFER_SIZE) {
                slotIdx = i + CrafterContainer.SLOT_BUFFER;
            } else {
                slotIdx = i + CrafterContainer.SLOT_BUFFEROUT - CrafterContainer.BUFFER_SIZE;
            }
            if (inventoryHelper.containsItem(slotIdx)) {
                ItemStack stack = inventoryHelper.getStackInSlot(slotIdx).copy();
                stack.setCount(1);
                ghostSlots.set(i, stack);
            }
        }
        noRecipesWork = false;
        markDirtyClient();
    }

    private void forgetItems() {
        for (int i = 0 ; i < ghostSlots.size() ; i++) {
            ghostSlots.set(i, ItemStack.EMPTY);
        }
        noRecipesWork = false;
        markDirtyClient();
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)]);
            return true;
        } else if (CMD_MODE.equals(command)) {
            setSpeedMode(params.get(ImageChoiceLabel.PARAM_CHOICE_IDX));
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

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            // We always use NullSidedInvWrapper because we don't want automation
            // to access the storage slots
            if (invHandlerNull == null) {
                invHandlerNull = new NullSidedInvWrapper(this);
            }
            return (T) invHandlerNull;
        }
        return super.getCapability(capability, facing);
    }


}
