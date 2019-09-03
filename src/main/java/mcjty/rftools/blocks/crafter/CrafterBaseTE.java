package mcjty.rftools.blocks.crafter;

import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ItemStackList;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.compat.jei.JEIRecipeAcceptor;
import mcjty.rftools.craftinggrid.CraftingRecipe;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static mcjty.rftools.blocks.crafter.CrafterContainer.CONTAINER_FACTORY;
import static mcjty.rftools.craftinggrid.CraftingRecipe.CraftMode.EXTC;
import static mcjty.rftools.craftinggrid.CraftingRecipe.CraftMode.INT;

public class CrafterBaseTE extends GenericTileEntity implements ITickableTileEntity,
        JEIRecipeAcceptor {
    public static final int SPEED_SLOW = 0;
    public static final int SPEED_FAST = 1;

    public static final String CMD_MODE = "crafter.setMode";
    public static final String CMD_RSMODE = "crafter.setRsMode";
    public static final String CMD_REMEMBER = "crafter.remember";
    public static final String CMD_FORGET = "crafter.forget";

//    private InventoryHelper inventoryHelper = new InventoryHelper(this, CrafterContainer.CONTAINER_FACTORY,
//            10 + CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE + 1);
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, CrafterConfiguration.MAXENERGY.get(), CrafterConfiguration.RECEIVEPERTICK.get()));

    private ItemStackList ghostSlots = ItemStackList.create(CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE);

    private final CraftingRecipe[] recipes;

    private StorageFilterCache filterCache = null;

    private int speedMode = SPEED_SLOW;

    // If the crafter tries to craft something, but there's nothing it can make,
    // this gets set to true, preventing further ticking. It gets cleared whenever
    // any of its inventories or recipes change.
    /*package*/ boolean noRecipesWork = false;

    private CraftingInventory workInventory = new CraftingInventory(new Container(null, -1) {
        @SuppressWarnings("NullableProblems")
        @Override
        public boolean canInteractWith(PlayerEntity var1) {
            return false;
        }
    }, 3, 3);

    public CrafterBaseTE(TileEntityType type, int supportedRecipes) {
        super(type);
        recipes = new CraftingRecipe[supportedRecipes];
        for (int i = 0; i < recipes.length; ++i) {
            recipes[i] = new CraftingRecipe();
        }
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    public ItemStackList getGhostSlots() {
        return ghostSlots;
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        itemHandler.ifPresent(h -> {
            h.setStackInSlot(CrafterContainer.SLOT_CRAFTOUTPUT, stacks.get(0));
            for (int i = 1; i < stacks.size(); i++) {
                h.setStackInSlot(CrafterContainer.SLOT_CRAFTINPUT + i - 1, stacks.get(i));
            }
        });
    }

    public void selectRecipe(int index) {
        itemHandler.ifPresent(h -> {
            CraftingRecipe recipe = recipes[index];
            h.setStackInSlot(CrafterContainer.SLOT_CRAFTOUTPUT, recipe.getResult());
            CraftingInventory inv = recipe.getInventory();
            int size = inv.getSizeInventory();
            for (int i = 0; i < size; ++i) {
                h.setStackInSlot(CrafterContainer.SLOT_CRAFTINPUT + i, inv.getStackInSlot(i));
            }
        });
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

    private void getFilterCache() {
        if (filterCache == null) {
            // @todo 1.14
//            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(CrafterContainer.SLOT_FILTER_MODULE));
        }
    }

    // @todo 1.14 loot tables
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        itemHandler.ifPresent(h -> h.deserializeNBT(tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND)));
        energyHandler.ifPresent(h -> h.setEnergy(tagCompound.getLong("Energy")));
        readGhostBufferFromNBT(tagCompound);
        readRecipesFromNBT(tagCompound);

        speedMode = tagCompound.getByte("speedMode");
    }

    private void readGhostBufferFromNBT(CompoundNBT tagCompound) {
        ListNBT bufferTagList = tagCompound.getList("GItems", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.size() ; i++) {
            CompoundNBT CompoundNBT = bufferTagList.getCompound(i);
            ghostSlots.set(i, ItemStack.read(CompoundNBT));
        }
    }

    private void readRecipesFromNBT(CompoundNBT tagCompound) {
        ListNBT recipeTagList = tagCompound.getList("Recipes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < recipeTagList.size() ; i++) {
            CompoundNBT CompoundNBT = recipeTagList.getCompound(i);
            recipes[i].readFromNBT(CompoundNBT);
        }
    }

    // @todo 1.14 loot tables
    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        itemHandler.ifPresent(h -> tagCompound.put("Items", h.serializeNBT()));
        energyHandler.ifPresent(h -> tagCompound.putLong("Energy", h.getEnergy()));
        writeGhostBufferToNBT(tagCompound);
        writeRecipesToNBT(tagCompound);
        tagCompound.putByte("speedMode", (byte) speedMode);
        return tagCompound;
    }

    private void writeGhostBufferToNBT(CompoundNBT tagCompound) {
        ListNBT bufferTagList = new ListNBT();
        for (ItemStack stack : ghostSlots) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            if (!stack.isEmpty()) {
                stack.write(CompoundNBT);
            }
            bufferTagList.add(CompoundNBT);
        }
        tagCompound.put("GItems", bufferTagList);
    }

    private void writeRecipesToNBT(CompoundNBT tagCompound) {
        ListNBT recipeTagList = new ListNBT();
        for (CraftingRecipe recipe : recipes) {
            CompoundNBT CompoundNBT = new CompoundNBT();
            recipe.writeToNBT(CompoundNBT);
            recipeTagList.add(CompoundNBT);
        }
        tagCompound.put("Recipes", recipeTagList);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        if (!isMachineEnabled() || noRecipesWork) {
            return;
        }

        energyHandler.ifPresent(h -> {
            // 0%: rf -> rf
            // 100%: rf -> rf / 2
            int rf = (int) (CrafterConfiguration.rfPerOperation.get() * (2.0f - getInfusedFactor()) / 2.0f);

            int steps = speedMode == SPEED_FAST ? CrafterConfiguration.speedOperations.get() : 1;
            if (rf > 0) {
                steps = (int) Math.min(steps, h.getEnergy() / rf);
            }

            int i;
            for (i = 0; i < steps; ++i) {
                if (!craftOneCycle()) {
                    noRecipesWork = true;
                    break;
                }
            }
            rf *= i;
            if (rf > 0) {
                h.consumeEnergy(rf);
            }
        });
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
        IRecipe recipe = craftingRecipe.getCachedRecipe(world);
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
//            return OreDictionary.itemMatches(target, input, false);
            // @toco 1.14   tags/oredict
            return false;
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
        CraftingInventory inventory = craftingRecipe.getInventory();

        for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                AtomicInteger count = new AtomicInteger(stack.getCount());
                for (int j = 0 ; j < CrafterContainer.BUFFER_SIZE ; j++) {
                    int slotIdx = CrafterContainer.SLOT_BUFFER + j;
                    int finalI = i;
                    itemHandler.ifPresent(h -> {
                        ItemStack input = h.getStackInSlot(slotIdx);
                        if (!input.isEmpty() && input.getCount() > keep) {
                            if (match(stack, input, strictDamage)) {
                                workInventory.setInventorySlotContents(finalI, input.copy());
                                int ss = count.get();
                                if (input.getCount() - ss < keep) {
                                    ss = input.getCount() - keep;
                                }
                                count.addAndGet(-ss);
                                if (!undo.containsKey(slotIdx)) {
                                    undo.put(slotIdx, input.copy());
                                }
                                input.split(ss);        // This consumes the items
                                if (input.isEmpty()) {
                                    h.setStackInSlot(slotIdx, ItemStack.EMPTY);
                                }
                            }
                        }
                    });
                    if (count.get() == 0) {
                        break;
                    }
                }
                if (count.get() > 0) {
                    return false;   // Couldn't find all items.
                }
            } else {
                workInventory.setInventorySlotContents(i, ItemStack.EMPTY);
            }
        }

        IRecipe recipe = craftingRecipe.getCachedRecipe(world);
        return recipe.matches(workInventory, world);
    }


    private void undo(Map<Integer,ItemStack> undo) {
        itemHandler.ifPresent(h -> {
                    for (Map.Entry<Integer, ItemStack> entry : undo.entrySet()) {
                        h.setStackInSlot(entry.getKey(), entry.getValue());
                    }
                });
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
        return false;
        // @todo 1.14 rethink the undo thing
        //return InventoryHelper.mergeItemStack(this, true, result, start, stop, undo) == 0;
    }

    private void rememberItems() {
        itemHandler.ifPresent(h -> {
                    for (int i = 0; i < ghostSlots.size(); i++) {
                        int slotIdx;
                        if (i < CrafterContainer.BUFFER_SIZE) {
                            slotIdx = i + CrafterContainer.SLOT_BUFFER;
                        } else {
                            slotIdx = i + CrafterContainer.SLOT_BUFFEROUT - CrafterContainer.BUFFER_SIZE;
                        }
                        if (!h.getStackInSlot(slotIdx).isEmpty()) {
                            ItemStack stack = h.getStackInSlot(slotIdx).copy();
                            stack.setCount(1);
                            ghostSlots.set(i, stack);
                        }
                    }
                });
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
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
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
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, facing);
    }


    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(CrafterBaseTE.this, CONTAINER_FACTORY, 10 + CrafterContainer.BUFFER_SIZE + CrafterContainer.BUFFEROUT_SIZE + 1) {

            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                noRecipesWork = false;
                if (index == CrafterContainer.SLOT_FILTER_MODULE) {
                    filterCache = null;
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                if (slot >= CrafterContainer.SLOT_CRAFTINPUT && slot <= CrafterContainer.SLOT_CRAFTOUTPUT) {
                    return false;
                }
                if (slot >= CrafterContainer.SLOT_BUFFER && slot < CrafterContainer.SLOT_BUFFEROUT) {
                    ItemStack ghostSlot = ghostSlots.get(slot - CrafterContainer.SLOT_BUFFER);
                    if (!ghostSlot.isEmpty()) {
                        if (!ghostSlot.isItemEqual(stack)) {
                            return false;
                        }
                    }
                    ItemStack filterModule = itemHandler.map(h -> h.getStackInSlot(CrafterContainer.SLOT_FILTER_MODULE)).orElse(ItemStack.EMPTY);
                    if (!filterModule.isEmpty()) {
                        getFilterCache();
                        if (filterCache != null) {
                            return filterCache.match(stack);
                        }
                    }
                } else if (slot >= CrafterContainer.SLOT_BUFFEROUT && slot < CrafterContainer.SLOT_FILTER_MODULE) {
                    ItemStack ghostSlot = ghostSlots.get(slot - CrafterContainer.SLOT_BUFFEROUT + CrafterContainer.BUFFER_SIZE);
                    if (!ghostSlot.isEmpty()) {
                        if (!ghostSlot.isItemEqual(stack)) {
                            return false;
                        }
                    }
                }
                return true;
            }

            @Override
            public boolean isItemInsertable(int slot, @Nonnull ItemStack stack) {
                if (!isItemValid(slot, stack)) {
                    return false;
                }
                return CONTAINER_FACTORY.isInputSlot(slot);
            }
        };
    }


}
