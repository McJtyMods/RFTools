package mcjty.rftools.blocks.shaper;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.modifier.ModifierEntry;
import mcjty.rftools.items.modifier.ModifierFilterOperation;
import mcjty.rftools.items.modifier.ModifierItem;
import mcjty.rftools.items.storage.StorageFilterCache;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.shapes.ScanDataManager;
import mcjty.rftools.shapes.Shape;
import mcjty.rftools.shapes.StatePalette;
import mcjty.rftools.varia.RLE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_SCAN = "scan";
    public static final String CMD_SETOFFSET = "setOffset";
    public static final String CMD_MODE = "setMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, ScannerContainer.factory, 4);
    private StorageFilterCache filterCache = null;

    private int scanId = 0;
    private ItemStack renderStack = ItemStackTools.getEmptyStack();
    private BlockPos dataDim;
    private BlockPos dataOffset = new BlockPos(0, 0, 0);

    // Transient data that is used during the scan.
    private ScanProgress progress = null;
    // Client side indication if there is a scan in progress
    private int progressBusy = -1;

    public ScannerTileEntity() {
        super(ScannerConfiguration.SCANNER_MAXENERGY, ScannerConfiguration.SCANNER_RECEIVEPERTICK);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            if (progress != null) {
                if (getEnergyStored() >= ScannerConfiguration.SCANNER_PERTICK) {
                    consumeEnergy(ScannerConfiguration.SCANNER_PERTICK);
                    int done = 0;
                    while (progress != null && done < ScannerConfiguration.surfaceAreaPerTick) {
                        progressScan();
                        done += dataDim.getZ() * dataDim.getY();  // We scan planes on the x axis
                    }
                }
            } else if (isMachineEnabled()) {
                scan();
            }
        }
    }

    public int getScanProgress() {
        return progressBusy;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == ScannerContainer.SLOT_FILTER) {
            filterCache = null;
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index == ScannerContainer.SLOT_OUT) {
            if (ItemStackTools.isValid(stack)) {
                updateScanCard(stack);
                markDirty();
            }
        }
        if (index == ScannerContainer.SLOT_IN) {
            if (ItemStackTools.isValid(stack)) {
                dataDim = ShapeCardItem.getDimension(stack);
                if (ItemStackTools.isEmpty(renderStack)) {
                    renderStack = new ItemStack(BuilderSetup.shapeCardItem);
                }
                updateScanCard(renderStack);
                markDirty();
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == ScannerContainer.SLOT_FILTER) {
            filterCache = null;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    public int getScanId() {
        if (scanId == 0) {
            scanId = ScanDataManager.getScans().newScan(getWorld());
            markDirtyQuick();
        }
        return scanId;
    }

    public List<IBlockState> getMaterialPalette() {
        ScanDataManager.Scan scan = ScanDataManager.getScans().loadScan(getScanId());
        return scan.getMaterialPalette();
    }

    public BlockPos getDataDim() {
        return dataDim;
    }

    public BlockPos getDataOffset() {
        return dataOffset;
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(ScannerContainer.SLOT_FILTER));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        if (tagCompound.hasKey("render")) {
            renderStack = ItemStackTools.loadFromNBT(tagCompound.getCompoundTag("render"));
        } else {
            renderStack = ItemStackTools.getEmptyStack();
        }

        scanId = tagCompound.getInteger("scanid");
        dataDim = new BlockPos(tagCompound.getInteger("scandimx"), tagCompound.getInteger("scandimy"), tagCompound.getInteger("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInteger("scanoffx"), tagCompound.getInteger("scanoffy"), tagCompound.getInteger("scanoffz"));
        progressBusy = tagCompound.getInteger("progress");
    }


    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        if (ItemStackTools.isValid(renderStack)) {
            NBTTagCompound tc = new NBTTagCompound();
            renderStack.writeToNBT(tc);
            tagCompound.setTag("render", tc);
        }
        tagCompound.setInteger("scanid", getScanId());
        if (dataDim != null) {
            tagCompound.setInteger("scandimx", dataDim.getX());
            tagCompound.setInteger("scandimy", dataDim.getY());
            tagCompound.setInteger("scandimz", dataDim.getZ());
        }
        if (dataOffset != null) {
            tagCompound.setInteger("scanoffx", dataOffset.getX());
            tagCompound.setInteger("scanoffy", dataOffset.getY());
            tagCompound.setInteger("scanoffz", dataOffset.getZ());
        }
        if (progress == null) {
            tagCompound.setInteger("progress", -1);
        } else {
            tagCompound.setInteger("progress", (progress.x-progress.tl.getX()) * 100 / progress.dimX);
        }
    }


    public ItemStack getRenderStack() {
        ItemStack stack = inventoryHelper.getStackInSlot(ScannerContainer.SLOT_OUT);
        if (ItemStackTools.isValid(stack)) {
            return stack;
        }
        if (ItemStackTools.isEmpty(renderStack)) {
            renderStack = new ItemStack(BuilderSetup.shapeCardItem);
            updateScanCard(renderStack);
        }
        return renderStack;
    }

    private void setOffset(int offsetX, int offsetY, int offsetZ) {
        dataOffset = new BlockPos(offsetX, offsetY, offsetZ);
        markDirtyClient();
    }

    private void updateScanCard(ItemStack cardOut) {
        if (ItemStackTools.isValid(cardOut)) {
            if (!ShapeCardItem.getShape(cardOut).isScan()) {
                boolean solid = ShapeCardItem.isSolid(cardOut);
                ShapeCardItem.setShape(cardOut, Shape.SHAPE_SCAN, solid);
            }
            NBTTagCompound tagOut = cardOut.getTagCompound();
            if (dataDim != null) {
                ShapeCardItem.setDimension(cardOut, dataDim.getX(), dataDim.getY(), dataDim.getZ());
                ShapeCardItem.setData(tagOut, getScanId());
            }
        }
    }

    private void scan() {
        if (progress != null) {
            return;
        }
        if (ItemStackTools.isEmpty(getStackInSlot(ScannerContainer.SLOT_IN))) {
            // Cannot scan. No input card
            return;
        }

        int dimX = dataDim.getX();
        int dimY = dataDim.getY();
        int dimZ = dataDim.getZ();
        startScanArea(getPos().add(dataOffset.getX(), dataOffset.getY(), dataOffset.getZ()), dimX, dimY, dimZ);
//        scanArea(getPos().add(dataOffset.getX(), dataOffset.getY(), dataOffset.getZ()), dimX, dimY, dimZ);
    }

    private IBlockState mapState(List<ModifierEntry> modifiers, Map<IBlockState, IBlockState> modifierMapping, BlockPos pos, IBlockState inState) {
        if (modifiers.isEmpty()) {
            return inState;
        }
        if (!modifierMapping.containsKey(inState)) {
            IBlockState outState = inState;
            boolean stop = false;
            for (ModifierEntry modifier : modifiers) {
                if (stop) {
                    break;
                }
                switch (modifier.getType()) {
                    case FILTER_SLOT: {
                        ItemStack inputItem = inState.getBlock().getItem(getWorld(), pos, inState);
                        if (ItemStackTools.isValid(modifier.getIn()) && modifier.getIn().getItem() == ModularStorageSetup.storageFilterItem) {
                            StorageFilterCache filter = StorageFilterItem.getCache(modifier.getIn());
                            if (filter.match(inputItem)) {
                                outState = getOutput(inState, modifier);
                                stop = true;
                            }
                        } else {
                            // Empty input stack in modifier also matches
                            if (ItemStackTools.isEmpty(modifier.getIn()) || ItemStack.areItemsEqual(inputItem, modifier.getIn())) {
                                outState = getOutput(inState, modifier);
                                stop = true;
                            }
                        }
                        break;
                    }
                    case FILTER_ORE: {
                        ItemStack inputItem = inState.getBlock().getItem(getWorld(), pos, inState);
                        int[] oreIDs = OreDictionary.getOreIDs(inputItem);
                        for (int id : oreIDs) {
                            if (OreDictionary.getOreName(id).startsWith("ore")) {
                                outState = getOutput(inState, modifier);
                                stop = true;
                                break;
                            }
                        }
                        break;
                    }
                    case FILTER_LIQUID:
                        if (inState.getBlock() instanceof BlockLiquid) {
                            outState = getOutput(inState, modifier);
                            stop = true;
                        }
                        break;
                    case FILTER_TILEENTITY:
                        if (getWorld().getTileEntity(pos) != null) {
                            outState = getOutput(inState, modifier);
                            stop = true;
                        }
                        break;
                }
            }
            modifierMapping.put(inState, outState);
        }
        return modifierMapping.get(inState);
    }

    private IBlockState getOutput(IBlockState input, ModifierEntry modifier) {
        if (modifier.getOp() == ModifierFilterOperation.OPERATION_VOID) {
            return Blocks.AIR.getDefaultState();
        }
        ItemStack outputItem = modifier.getOut();
        if (ItemStackTools.isEmpty(outputItem)) {
            return input;
        } else {
            Block block = ForgeRegistries.BLOCKS.getValue(outputItem.getItem().getRegistryName());
            if (block == null) {
                return Blocks.AIR.getDefaultState();
            } else {
                return block.getStateFromMeta(outputItem.getMetadata());
            }
        }
    }

    private static class ScanProgress {
        List<ModifierEntry> modifiers;
        Map<IBlockState, IBlockState> modifierMapping;
        RLE rle;
        BlockPos tl;
        StatePalette materialPalette;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int dimX;
        int dimY;
        int dimZ;
        int x;
    }

    private void startScanArea(BlockPos center, int dimX, int dimY, int dimZ) {
        progress = new ScanProgress();
        progress.modifiers = ModifierItem.getModifiers(getStackInSlot(ScannerContainer.SLOT_MODIFIER));
        progress.modifierMapping = new HashMap<>();
        progress.rle = new RLE();
        progress.tl = new BlockPos(center.getX() - dimX/2, center.getY() - dimY/2, center.getZ() - dimZ/2);
        progress.materialPalette = new StatePalette();
        progress.x = progress.tl.getX();
        progress.dimX = dimX;
        progress.dimY = dimY;
        progress.dimZ = dimZ;
        markDirtyClient();
    }

    private void progressScan() {
        if (progress == null) {
            return;
        }
        BlockPos tl = progress.tl;
        int dimX = progress.dimX;
        int dimY = progress.dimY;
        int dimZ = progress.dimZ;
        BlockPos.MutableBlockPos mpos = progress.mpos;
        for (int z = tl.getZ() ; z < tl.getZ() + dimZ ; z++) {
            for (int y = tl.getY() ; y < tl.getY() + dimY ; y++) {
                mpos.setPos(progress.x, y, z);
                int c;
                if (getWorld().isAirBlock(mpos)) {
                    c = 0;
                } else {
                    IBlockState state = getWorld().getBlockState(mpos);
                    getFilterCache();
                    if (filterCache != null) {
                        ItemStack item = state.getBlock().getItem(getWorld(), mpos, state);
                        if (!filterCache.match(item)) {
                            state = null;
                        }
                    }
                    if (state != null && state != Blocks.AIR.getDefaultState()) {
                        state = mapState(progress.modifiers, progress.modifierMapping, mpos, state);
                    }
                    if (state != null && state != Blocks.AIR.getDefaultState()) {
                        c = progress.materialPalette.alloc(state, 0) + 1;
                    } else {
                        c = 0;
                    }
                }
                progress.rle.add(c);
            }
        }
        progress.x++;
        if (progress.x >= tl.getX() + dimX) {
            stopScanArea();
        } else {
            markDirtyClient();
        }
    }

    private void stopScanArea() {
        this.dataDim = new BlockPos(progress.dimX, progress.dimY, progress.dimZ);
        ScanDataManager scan = ScanDataManager.getScans();
        scan.getOrCreateScan(getScanId()).setData(progress.rle.getData(), progress.materialPalette.getPalette(), dataDim, dataOffset);
        scan.save(getWorld(), getScanId());
        if (ItemStackTools.isEmpty(renderStack)) {
            renderStack = new ItemStack(BuilderSetup.shapeCardItem);
        }
        updateScanCard(renderStack);
        markDirtyClient();
        progress = null;
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
            return true;
        } else if (CMD_SCAN.equals(command)) {
            scan();
            return true;
        } else if (CMD_SETOFFSET.equals(command)) {
            setOffset(args.get("offsetX").getInteger(), args.get("offsetY").getInteger(), args.get("offsetZ").getInteger());
            return true;
        }
        return false;
    }

}
