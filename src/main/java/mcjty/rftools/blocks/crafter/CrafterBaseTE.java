package mcjty.rftools.blocks.crafter;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.RedstoneMode;
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
import java.util.Map;

public class CrafterBaseTE extends GenericEnergyReceiverTileEntity implements ITickable, DefaultSidedInventory {
    public static final int SPEED_SLOW = 0;
    public static final int SPEED_FAST = 1;

    public static final String CMD_MODE = "mode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CrafterContainer.factory,
            10 + CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE + 1);
    private CraftingRecipe recipes[];
    private int supportedRecipes;

    private int powered;

// @todo when storage system is implemented    private StorageFilterCache filterCache = null;

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
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
        markDirtyClient();
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
//        if (index == CrafterContainer.SLOT_FILTER_MODULE) {
//            filterCache = null;
//        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }


    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    private void getFilterCache() {
//        if (filterCache == null) {
//            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(CrafterContainer.SLOT_FILTER_MODULE));
//        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index >= CrafterContainer.SLOT_BUFFER && index < CrafterContainer.SLOT_BUFFEROUT) {
            if (inventoryHelper.containsItem(CrafterContainer.SLOT_FILTER_MODULE)) {
                getFilterCache();
//                if (filterCache != null) {
//                    return filterCache.match(stack);
//                }
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
        powered = tagCompound.getInteger("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        readRecipesFromNBT(tagCompound);

        int m = tagCompound.getInteger("rsMode");
        redstoneMode = RedstoneMode.values()[m];

        speedMode = tagCompound.getByte("speedMode");
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
        tagCompound.setInteger("powered", powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        writeRecipesToNBT(tagCompound);
        tagCompound.setByte("rsMode", (byte)redstoneMode.ordinal());
        tagCompound.setByte("speedMode", (byte) speedMode);
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

    @Override
    public void setPowered(int powered) {
        this.powered = powered;
        markDirty();
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        if (redstoneMode != RedstoneMode.REDSTONE_IGNORED) {

            boolean rs = powered > 0;
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

        if (!testAndConsumeCraftingItems(craftingRecipe, undo)) {
            undo(undo);
            return false;
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
            return true;
        } else {
            // We don't have place. Undo the operation.
            undo(undo);
            return false;
        }
    }

    private boolean testAndConsumeCraftingItems(CraftingRecipe craftingRecipe, Map<Integer,ItemStack> undo) {
        boolean internal = craftingRecipe.isCraftInternal();
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
                        if (OreDictionary.itemMatches(stack, input, false)) {
                            workInventory.setInventorySlotContents(i, input.copy());
                            if (input.getItem().hasContainerItem(input)) {
                                ItemStack containerItem = input.getItem().getContainerItem(input);
                                if (containerItem != null) {
                                    if ((!containerItem.isItemStackDamageable()) || containerItem.getItemDamage() <= containerItem.getMaxDamage()) {
                                        if (!placeResult(internal, containerItem, undo)) {
                                            // Not enough room.
                                            return false;
                                        }
                                    }
                                }
                            }
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
        return InventoryHelper.mergeItemStack(this, result, start, stop, undo) == 0;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
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
