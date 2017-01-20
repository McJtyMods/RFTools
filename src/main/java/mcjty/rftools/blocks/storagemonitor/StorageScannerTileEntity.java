package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ChatTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.SoundTools;
import mcjty.rftools.api.general.IInventoryTracker;
import mcjty.rftools.api.storage.IStorageScanner;
import mcjty.rftools.blocks.crafter.CraftingRecipe;
import mcjty.rftools.craftinggrid.CraftingGrid;
import mcjty.rftools.craftinggrid.CraftingGridProvider;
import mcjty.rftools.craftinggrid.StorageCraftingTools;
import mcjty.rftools.craftinggrid.TileEntityItemSource;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.*;

public class StorageScannerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable,
        CraftingGridProvider, JEIRecipeAcceptor, IStorageScanner {

    public static final String CMD_SETRADIUS = "setRadius";
    public static final String CMD_UP = "up";
    public static final String CMD_TOP = "top";
    public static final String CMD_DOWN = "down";
    public static final String CMD_BOTTOM = "bottom";
    public static final String CMD_REMOVE = "remove";
    public static final String CMD_TOGGLEROUTABLE = "toggleRoutable";
    public static final String CMD_TOGGLEEXPORT = "toggleExport";
    public static final String CMD_SETVIEW = "setView";

    private static final int[] SLOTS = new int[]{0, 1, 2};

    private List<BlockPos> inventories = new ArrayList<>();
    private Map<CachedItemKey, CachedItemCount> cachedCounts = new HashMap<>();
    private Set<BlockPos> routable = new HashSet<>();
    private int radius = 1;

    // This is set on a client-side dummy tile entity for a tablet
    private Integer monitorDim;

    private boolean exportToCurrent = false;
    private BlockPos lastSelectedInventory = null;

    // Indicates if for this storage scanner the inventories should be shown wide
    private boolean openWideView = true;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, StorageScannerContainer.factory, 3);    // 1 extra slot for automation is at index 2
    private CraftingGrid craftingGrid = new CraftingGrid();

    public StorageScannerTileEntity() {
        super(StorageScannerConfiguration.MAXENERGY, StorageScannerConfiguration.RECEIVEPERTICK);
        monitorDim = null;
    }

    // This constructor is used for constructing a dummy client-side tile entity when
    // accessing the storage scanner remotely
    public StorageScannerTileEntity(EntityPlayer entityPlayer, int monitordim) {
        super(StorageScannerConfiguration.MAXENERGY, StorageScannerConfiguration.RECEIVEPERTICK);
//        this.entityPlayer = entityPlayer;
        this.monitorDim = monitordim;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public void storeRecipe(int index) {
        getCraftingGrid().storeRecipe(index);
    }

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
        markDirty();
    }

    @Override
    public CraftingGrid getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public void markInventoryDirty() {
        markDirty();
    }

    @Override
    @Nonnull
    public int[] craft(EntityPlayerMP player, int n, boolean test) {
        CraftingRecipe activeRecipe = craftingGrid.getActiveRecipe();
        return craft(player, n, test, activeRecipe);
    }

    @Nonnull
    public int[] craft(EntityPlayerMP player, int n, boolean test, CraftingRecipe activeRecipe) {
        TileEntityItemSource itemSource = new TileEntityItemSource()
                .addInventory(player.inventory, 0);
        for (BlockPos p : getInventories()) {
            if (isRoutable(p)) {
                TileEntity tileEntity = getWorld().getTileEntity(p);
                if (!(tileEntity instanceof StorageScannerTileEntity)) {
                    itemSource.add(tileEntity, 0);
                }
            }
        }

        if (test) {
            return StorageCraftingTools.testCraftItems(player, n, activeRecipe, itemSource);
        } else {
            StorageCraftingTools.craftItems(player, n, activeRecipe, itemSource);
            return new int[0];
        }
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0; i < stacks.size(); i++) {
            craftingGrid.getCraftingGridInventory().setInventorySlotContents(i, stacks.get(i));
        }
        markDirty();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            if (inventoryHelper.containsItem(StorageScannerContainer.SLOT_IN)) {
                if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerInsert) {
                    return;
                }

                ItemStack stack = inventoryHelper.getStackInSlot(StorageScannerContainer.SLOT_IN);
                stack = injectStackInternal(stack, exportToCurrent);
                inventoryHelper.setInventorySlotContents(64, StorageScannerContainer.SLOT_IN, stack);

                consumeEnergy(StorageScannerConfiguration.rfPerInsert);
            }
            if (inventoryHelper.containsItem(StorageScannerContainer.SLOT_IN_AUTO)) {
                if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerInsert) {
                    return;
                }

                ItemStack stack = inventoryHelper.getStackInSlot(StorageScannerContainer.SLOT_IN_AUTO);
                stack = injectStackInternal(stack, false);
                inventoryHelper.setInventorySlotContents(64, StorageScannerContainer.SLOT_IN_AUTO, stack);

                consumeEnergy(StorageScannerConfiguration.rfPerInsert);
            }
        }
    }


    public ItemStack injectStack(ItemStack stack, EntityPlayer player) {
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerInsert) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Not enough power to insert items!"));
            return stack;
        }
        if (!checkForRoutableInventories()) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "There are no routable inventories!"));
            return stack;
        }
        stack = injectStackInternal(stack, false);
        if (ItemStackTools.isEmpty(stack)) {
            consumeEnergy(StorageScannerConfiguration.rfPerInsert);
            SoundTools.playSound(getWorld(), SoundEvents.ENTITY_ITEM_PICKUP, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0f, 3.0f);
        }
        return stack;
    }

    private boolean checkForRoutableInventories() {
        for (BlockPos blockPos : inventories) {
            if (!blockPos.equals(getPos()) && routable.contains(blockPos)) {
                TileEntity te = getWorld().getTileEntity(blockPos);
                if (te != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private ItemStack injectStackInternal(ItemStack stack, boolean toSelected) {
        if (toSelected && lastSelectedInventory != null && lastSelectedInventory.getY() != -1) {
            // Try to insert into the selected inventory
            TileEntity te = getWorld().getTileEntity(lastSelectedInventory);
            if (te != null && !(te instanceof StorageScannerTileEntity)) {
                stack = InventoryHelper.insertItem(getWorld(), lastSelectedInventory, null, stack);
                if (ItemStackTools.isEmpty(stack)) {
                    return stack;
                }
            }
            return stack;
        }
        for (BlockPos blockPos : inventories) {
            if (!blockPos.equals(getPos()) && routable.contains(blockPos)) {
                TileEntity te = getWorld().getTileEntity(blockPos);
                if (te != null && !(te instanceof StorageScannerTileEntity)) {
                    stack = InventoryHelper.insertItem(getWorld(), blockPos, null, stack);
                    if (ItemStackTools.isEmpty(stack)) {
                        return stack;
                    }
                }
            }
        }
        return stack;
    }

    /**
     * Give a stack matching the input stack to the player containing either a single
     * item or else a full stack
     *
     * @param stack
     * @param single
     * @param player
     */
    public void giveToPlayer(ItemStack stack, boolean single, EntityPlayer player, boolean oredict) {
        if (ItemStackTools.isEmpty(stack)) {
            return;
        }
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerRequest) {
            ChatTools.addChatMessage(player, new TextComponentString(TextFormatting.RED + "Not enough power to request items!"));
            return;
        }

        Set<Integer> oredictMatches = getOredictMatchers(stack, oredict);
        List<BlockPos> inventories = getInventories();
        final int[] cnt = {single ? 1 : stack.getMaxStackSize()};
        boolean given = false;
        for (BlockPos c : inventories) {
            TileEntity tileEntity = getWorld().getTileEntity(c);
            if (tileEntity instanceof StorageScannerTileEntity) {
                continue;
            }
            if (RFToolsTools.hasItemCapabilitySafe(tileEntity)) {
                IItemHandler capability = RFToolsTools.getItemCapabilitySafe(tileEntity);
                for (int i = 0; i < capability.getSlots(); i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    if (isItemEqual(stack, itemStack, oredictMatches)) {
                        ItemStack received = capability.extractItem(i, cnt[0], false);
                        if (giveItemToPlayer(player, cnt, received)) {
                            given = true;
                        }
                    }
                }
            } else if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (isItemEqual(stack, itemStack, oredictMatches)) {
                        ItemStack received = inventory.decrStackSize(i, cnt[0]);
                        if (giveItemToPlayer(player, cnt, received)) {
                            given = true;
                        }
                    }
                }
            }
        }
        if (given) {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest);
            SoundTools.playSound(getWorld(), SoundEvents.ENTITY_ITEM_PICKUP, getPos().getX(), getPos().getY(), getPos().getZ(), 1.0f, 1.0f);
        }
    }

    private boolean giveItemToPlayer(EntityPlayer player, int[] cnt, ItemStack received) {
        if (ItemStackTools.isValid(received) && cnt[0] > 0) {
            cnt[0] -= ItemStackTools.getStackSize(received);
            giveToPlayer(received, player);
            return true;
        }
        return false;
    }

    private boolean giveToPlayer(ItemStack stack, EntityPlayer player) {
        if (ItemStackTools.isEmpty(stack)) {
            return false;
        }
        if (!player.inventory.addItemStackToInventory(stack)) {
            player.entityDropItem(stack, 1.05f);
        }
        return true;
    }

    @Override
    public int countItems(ItemStack stack, boolean starred, boolean oredict) {
        if (ItemStackTools.isEmpty(stack)) {
            return 0;
        }
        int cnt = 0;
        Set<Integer> oredictMatches = getOredictMatchers(stack, oredict);
        List<BlockPos> inventories = getInventories();
        for (BlockPos c : inventories) {
            if ((!starred) || routable.contains(c)) {
                TileEntity tileEntity = getWorld().getTileEntity(c);
                if (tileEntity instanceof StorageScannerTileEntity) {
                    continue;
                }
                Integer cachedCount = null;
                if (tileEntity instanceof IInventoryTracker) {
                    IInventoryTracker tracker = (IInventoryTracker) tileEntity;
                    CachedItemCount itemCount = cachedCounts.get(new CachedItemKey(c, stack.getItem(), stack.getItemDamage()));
                    if (itemCount != null) {
                        Integer oldVersion = itemCount.getVersion();
                        if (oldVersion == tracker.getVersion()) {
                            cachedCount = itemCount.getCount();
                        }
                    }
                }
                if (cachedCount != null) {
                    cnt += cachedCount;
                } else {
                    final int[] cc = {0};
                    InventoryHelper.getItems(tileEntity, s -> isItemEqual(stack, s, oredictMatches))
                            .forEach(s -> cc[0] += ItemStackTools.getStackSize(s));
                    cnt += cc[0];
                    if (tileEntity instanceof IInventoryTracker) {
                        IInventoryTracker tracker = (IInventoryTracker) tileEntity;
                        cachedCounts.put(new CachedItemKey(c, stack.getItem(), stack.getItemDamage()), new CachedItemCount(tracker.getVersion(), cc[0]));
                    }
                }
            }
        }
        return cnt;
    }

    private static Set<Integer> getOredictMatchers(ItemStack stack, boolean oredict) {
        Set<Integer> oredictMatches = new HashSet<>();
        if (oredict) {
            for (int id : OreDictionary.getOreIDs(stack)) {
                oredictMatches.add(id);
            }
        }
        return oredictMatches;
    }

    public static boolean isItemEqual(ItemStack thisItem, ItemStack other, boolean oredict) {
        return isItemEqual(thisItem, other, getOredictMatchers(thisItem, oredict));
    }

    public static boolean isItemEqual(ItemStack thisItem, ItemStack other, Set<Integer> oreDictMatchers) {
        if (ItemStackTools.isEmpty(other)) {
            return false;
        }
        if (oreDictMatchers.isEmpty()) {
            return thisItem.isItemEqual(other);
        } else {
            int[] oreIDs = OreDictionary.getOreIDs(other);
            for (int id : oreIDs) {
                if (oreDictMatchers.contains(id)) {
                    return true;
                }
            }
        }
        return false;
    }


    public Set<BlockPos> performSearch(String search) {
        List<BlockPos> inventories = getInventories();
        search = search.toLowerCase();
        Set<BlockPos> output = new HashSet<>();
        for (BlockPos c : inventories) {
            TileEntity tileEntity = getWorld().getTileEntity(c);
            if (!(tileEntity instanceof StorageScannerTileEntity)) {
                final String finalSearch = search;
                InventoryHelper.getItems(tileEntity, s -> s.getDisplayName().toLowerCase().contains(finalSearch)).forEach(s -> output.add(c));
            }
        }
        return output;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int v) {
        radius = v;
        markDirtyClient();
    }

    public boolean isOpenWideView() {
        return openWideView;
    }

    public void setOpenWideView(boolean openWideView) {
        this.openWideView = openWideView;
        markDirtyClient();
    }

    public boolean isExportToCurrent() {
        return exportToCurrent;
    }

    public void setExportToCurrent(boolean exportToCurrent) {
        this.exportToCurrent = exportToCurrent;
        markDirty();
    }

    private void toggleExportRoutable() {
        exportToCurrent = !exportToCurrent;
        markDirtyClient();
    }

    public boolean isRoutable(BlockPos p) {
        return routable.contains(p);
    }

    public void toggleRoutable(BlockPos p) {
        if (routable.contains(p)) {
            routable.remove(p);
        } else {
            routable.add(p);
        }
        markDirtyClient();
    }

    public List<BlockPos> getInventories() {
        return inventories;
    }

    private void moveUp(int index) {
        if (index <= 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p1 = inventories.get(index - 1);
        BlockPos p2 = inventories.get(index);
        inventories.set(index - 1, p2);
        inventories.set(index, p1);
        markDirty();
    }

    private void moveTop(int index) {
        if (index <= 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p = inventories.get(index);
        inventories.remove(index);
        inventories.add(0, p);
        markDirty();
    }

    private void moveDown(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size() - 1) {
            return;
        }
        BlockPos p1 = inventories.get(index);
        BlockPos p2 = inventories.get(index + 1);
        inventories.set(index, p2);
        inventories.set(index + 1, p1);
        markDirty();
    }

    private void moveBottom(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size() - 1) {
            return;
        }
        BlockPos p = inventories.get(index);
        inventories.remove(index);
        inventories.add(p);
        markDirty();
    }

    private void removeInventory(int index) {
        if (index < 0) {
            return;
        }
        if (index >= inventories.size()) {
            return;
        }
        BlockPos p = inventories.get(index);
        inventories.remove(index);
        markDirty();
    }

    public void clearCachedCounts() {
        cachedCounts.clear();
    }

    public List<BlockPos> findInventories() {
        // Clear the caches
        cachedCounts.clear();

        // First remove all inventories that are either out of range or no longer an inventory:
        List<BlockPos> old = inventories;
        Set<BlockPos> oldAdded = new HashSet<>();
        inventories = new ArrayList<>();


        for (BlockPos p : old) {
            if (p.getX() >= getPos().getX() - radius && p.getX() <= getPos().getX() + radius) {
                if (p.getY() >= getPos().getY() - radius && p.getY() <= getPos().getY() + radius) {
                    if (p.getZ() >= getPos().getZ() - radius && p.getZ() <= getPos().getZ() + radius) {
                        TileEntity te = getWorld().getTileEntity(p);
                        if (InventoryHelper.isInventory(te) && !(te instanceof StorageScannerTileEntity)) {
                            inventories.add(p);
                            oldAdded.add(p);
                        }
                    }
                }
            }
        }

        // Now append all inventories that are new.
        for (int x = getPos().getX() - radius; x <= getPos().getX() + radius; x++) {
            for (int z = getPos().getZ() - radius; z <= getPos().getZ() + radius; z++) {
                for (int y = getPos().getY() - radius; y <= getPos().getY() + radius; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    if (!oldAdded.contains(p)) {
                        TileEntity te = getWorld().getTileEntity(p);
                        if (InventoryHelper.isInventory(te) && !(te instanceof StorageScannerTileEntity)) {
                            inventories.add(p);
                        }
                    }
                }
            }
        }
        return inventories;
    }

    @Override
    public ItemStack requestItem(ItemStack match, int amount, boolean doRoutable, boolean oredict) {
        if (ItemStackTools.isEmpty(match)) {
            return ItemStackTools.getEmptyStack();
        }
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerRequest) {
            return ItemStackTools.getEmptyStack();
        }

        Set<Integer> oredictMatches = getOredictMatchers(match, oredict);
        List<BlockPos> inventories = getInventories();
        ItemStack result = ItemStackTools.getEmptyStack();
        final int[] cnt = {match.getMaxStackSize() < amount ? match.getMaxStackSize() : amount};
        for (BlockPos c : inventories) {
            if (cnt[0] <= 0) {
                break;
            }
            if (doRoutable && !routable.contains(c)) {
                continue;
            }
            TileEntity tileEntity = getWorld().getTileEntity(c);
            if (tileEntity instanceof StorageScannerTileEntity) {
                continue;
            }
            if (RFToolsTools.hasItemCapabilitySafe(tileEntity)) {
                IItemHandler capability = RFToolsTools.getItemCapabilitySafe(tileEntity);
                for (int i = 0; i < capability.getSlots(); i++) {
                    ItemStack itemStack = capability.getStackInSlot(i);
                    if (isItemEqual(match, itemStack, oredictMatches)) {
                        ItemStack received = capability.extractItem(i, cnt[0], false);
                        if (ItemStackTools.isValid(received)) {
                            if (ItemStackTools.isEmpty(result)) {
                                result = received;
                            } else {
                                ItemStackTools.incStackSize(result, ItemStackTools.getStackSize(received));
                            }
                            cnt[0] -= ItemStackTools.getStackSize(received);
                        }
                    }
                }
            } else if (tileEntity instanceof IInventory) {
                IInventory inventory = (IInventory) tileEntity;
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack itemStack = inventory.getStackInSlot(i);
                    if (isItemEqual(match, itemStack, oredictMatches)) {
                        ItemStack received = inventory.decrStackSize(i, cnt[0]);
                        if (ItemStackTools.isValid(received)) {
                            if (ItemStackTools.isEmpty(result)) {
                                result = received;
                            } else {
                                ItemStackTools.incStackSize(result, ItemStackTools.getStackSize(received));
                            }
                            cnt[0] -= ItemStackTools.getStackSize(received);
                        }
                    }
                }
            }
        }
        if (ItemStackTools.isValid(result)) {
            consumeEnergy(StorageScannerConfiguration.rfPerRequest);
        }
        return result;
    }

    @Override
    public int insertItem(ItemStack stack) {
        if (getEnergyStored(EnumFacing.DOWN) < StorageScannerConfiguration.rfPerInsert) {
            return ItemStackTools.getStackSize(stack);
        }

        ItemStack toInsert = stack.copy();

        for (BlockPos blockPos : inventories) {
            if (!blockPos.equals(getPos()) && routable.contains(blockPos)) {
                TileEntity te = getWorld().getTileEntity(blockPos);
                if (te != null && !(te instanceof StorageScannerTileEntity)) {
                    toInsert = InventoryHelper.insertItem(getWorld(), blockPos, null, toInsert);
                    if (ItemStackTools.isEmpty(toInsert)) {
                        return 0;
                    }
                }
            }
        }

        consumeEnergy(StorageScannerConfiguration.rfPerInsert);
        return ItemStackTools.getStackSize(toInsert);
    }

    private ItemStack requestStackFromInv(BlockPos invPos, ItemStack requested, Integer[] todo, ItemStack outSlot) {
        TileEntity tileEntity = getWorld().getTileEntity(invPos);
        if (tileEntity instanceof StorageScannerTileEntity) {
            return outSlot;
        }

        int size = InventoryHelper.getInventorySize(tileEntity);

        for (int i = 0 ; i < size ; i++) {
            ItemStack stack = ItemStackTools.getStack(tileEntity, i);
            if (ItemHandlerHelper.canItemStacksStack(requested, stack)) {
                ItemStack extracted = ItemStackTools.extractItem(tileEntity, i, todo[0]);
                todo[0] -= ItemStackTools.getStackSize(extracted);
                if (ItemStackTools.isEmpty(outSlot)) {
                    outSlot = extracted;
                } else {
                    ItemStackTools.incStackSize(outSlot, ItemStackTools.getStackSize(extracted));
                }
                if (todo[0] == 0) {
                    break;
                }
            }
        }
        return outSlot;
    }

    public void requestStack(BlockPos invPos, ItemStack requested, int amount, EntityPlayer player) {
        int rf = StorageScannerConfiguration.rfPerRequest;
        if (amount >= 0) {
            rf /= 10;       // Less RF usage for requesting less items
        }
        if (amount == -1) {
            amount = requested.getMaxStackSize();
        }
        if (getEnergyStored(EnumFacing.DOWN) < rf) {
            return;
        }

        Integer[] todo = new Integer[] { amount };

        ItemStack outSlot = inventoryHelper.getStackInSlot(StorageScannerContainer.SLOT_OUT);
        if (ItemStackTools.isValid(outSlot)) {
            // Check if the items are the same and there is room
            if (!ItemHandlerHelper.canItemStacksStack(outSlot, requested)) {
                return;
            }
            if (ItemStackTools.getStackSize(outSlot) >= requested.getMaxStackSize()) {
                return;
            }
            todo[0] = Math.min(todo[0], requested.getMaxStackSize() - ItemStackTools.getStackSize(outSlot));
        }

        if (invPos.getY() == -1) {
            for (BlockPos blockPos : inventories) {
                if (routable.contains(blockPos)) {
                    outSlot = requestStackFromInv(blockPos, requested, todo, outSlot);
                    if (todo[0] == 0) {
                        break;
                    }
                }
            }
        } else {
            outSlot = requestStackFromInv(invPos, requested, todo, outSlot);
        }

        if (todo[0] == amount) {
            // Nothing happened
            return;
        }

        consumeEnergy(rf);
        setInventorySlotContents(StorageScannerContainer.SLOT_OUT, outSlot);

        if (StorageScannerConfiguration.requestStraightToInventory) {
            if (player.inventory.addItemStackToInventory(outSlot)) {
                setInventorySlotContents(StorageScannerContainer.SLOT_OUT, ItemStackTools.getEmptyStack());
            }
        }
    }

    private void addItemStack(List<ItemStack> stacks, Set<Item> foundItems, ItemStack stack) {
        if (ItemStackTools.isEmpty(stack)) {
            return;
        }
        if (foundItems.contains(stack.getItem())) {
            for (ItemStack s : stacks) {
                if (ItemHandlerHelper.canItemStacksStack(s, stack)) {
                    ItemStackTools.incStackSize(s, ItemStackTools.getStackSize(stack));
                    return;
                }
            }
        }
        // If we come here we need to append a new stack
        stacks.add(stack.copy());
        foundItems.add(stack.getItem());
    }


    public List<ItemStack> getInventoryForBlock(BlockPos cpos) {
        Set<Item> foundItems = new HashSet<>();
        List<ItemStack> stacks = new ArrayList<>();

        lastSelectedInventory = cpos;

        if (cpos.getY() == -1) {
            // Get all starred inventories
            for (BlockPos blockPos : inventories) {
                if (routable.contains(blockPos)) {
                    addItemsFromInventory(blockPos, foundItems, stacks);
                }
            }
        } else {
            addItemsFromInventory(cpos, foundItems, stacks);
        }

        return stacks;
    }

    private void addItemsFromInventory(BlockPos cpos, Set<Item> foundItems, List<ItemStack> stacks) {
        TileEntity tileEntity = getWorld().getTileEntity(cpos);

        if (RFToolsTools.hasItemCapabilitySafe(tileEntity)) {
            IItemHandler capability = RFToolsTools.getItemCapabilitySafe(tileEntity);
            for (int i = 0; i < capability.getSlots(); i++) {
                addItemStack(stacks, foundItems, capability.getStackInSlot(i));
            }
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                addItemStack(stacks, foundItems, inventory.getStackInSlot(i));
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        NBTTagList list = tagCompound.getTagList("inventories", Constants.NBT.TAG_COMPOUND);
        inventories.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = (NBTTagCompound) list.get(i);
            BlockPos c = BlockPosTools.readFromNBT(tag, "c");
            inventories.add(c);
        }
        list = tagCompound.getTagList("routable", Constants.NBT.TAG_COMPOUND);
        routable.clear();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = (NBTTagCompound) list.get(i);
            BlockPos c = BlockPosTools.readFromNBT(tag, "c");
            routable.add(c);
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        radius = tagCompound.getInteger("radius");
        exportToCurrent = tagCompound.getBoolean("exportC");
        if (tagCompound.hasKey("wideview")) {
            openWideView = tagCompound.getBoolean("wideview");
        } else {
            openWideView = true;
        }
        craftingGrid.readFromNBT(tagCompound.getCompoundTag("grid"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        NBTTagList list = new NBTTagList();
        for (BlockPos c : inventories) {
            NBTTagCompound tag = BlockPosTools.writeToNBT(c);
            list.appendTag(tag);
        }
        tagCompound.setTag("inventories", list);
        list = new NBTTagList();
        for (BlockPos c : routable) {
            NBTTagCompound tag = BlockPosTools.writeToNBT(c);
            list.appendTag(tag);
        }
        tagCompound.setTag("routable", list);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("radius", radius);
        tagCompound.setBoolean("exportC", exportToCurrent);
        tagCompound.setBoolean("wideview", openWideView);
        tagCompound.setTag("grid", craftingGrid.writeToNBT());
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETRADIUS.equals(command)) {
            setRadius(args.get("r").getInteger());
            return true;
        } else if (CMD_UP.equals(command)) {
            moveUp(args.get("index").getInteger());
            return true;
        } else if (CMD_TOP.equals(command)) {
            moveTop(args.get("index").getInteger());
            return true;
        } else if (CMD_DOWN.equals(command)) {
            moveDown(args.get("index").getInteger());
            return true;
        } else if (CMD_BOTTOM.equals(command)) {
            moveBottom(args.get("index").getInteger());
            return true;
        } else if (CMD_REMOVE.equals(command)) {
            removeInventory(args.get("index").getInteger());
            return true;
        } else if (CMD_TOGGLEROUTABLE.equals(command)) {
            toggleRoutable(args.get("pos").getCoordinate());
            return true;
        } else if (CMD_TOGGLEEXPORT.equals(command)) {
            toggleExportRoutable();
            return true;
        } else if (CMD_SETVIEW.equals(command)) {
            setOpenWideView(args.get("b").getBoolean());
            return true;
        }
        return false;
    }

    /**
     * Return true if this is a dummy TE. i.e. this happens only when accessing a
     * storage scanner in a tablet on the client side.
     */
    public boolean isDummy() {
        return monitorDim != null;
    }

    /**
     * This is used client side only for the GUI.
     * Return the position of the crafting grid container. This
     * is either the position of this tile entity (in case we are just looking
     * directly at the storage scanner), the position of a 'watching' tile
     * entity (in case we are a dummy for the storage terminal) or else null
     * in case we're using a handheld item.
     *
     * @return
     */
    public BlockPos getCraftingGridContainerPos() {
        return getPos();
    }

    /**
     * This is used client side only for the GUI.
     * Get the real crafting grid provider
     */
    public CraftingGridProvider getCraftingGridProvider() {
        return this;
    }

    /**
     * This is used client side only for the GUI.
     * Return the position of the actual storage scanner
     *
     * @return
     */
    public BlockPos getStorageScannerPos() {
        return getPos();
    }

    public int getDimension() {
        if (isDummy()) {
            return monitorDim;
        } else {
            return getWorld().provider.getDimension();
        }
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        // We only allow insertion in the auto slot for the storage scanner (for automation)
        return index == StorageScannerContainer.SLOT_IN_AUTO;
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        // @todo
        return true;
//        return canPlayerAccess(player);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return StorageScannerContainer.factory.isOutputSlot(index);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return SLOTS;
    }
}
