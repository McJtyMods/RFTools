package mcjty.rftools.blocks.shaper;

import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IAction;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.items.ModItems;
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
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScannerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickableTileEntity {

    public static final String ACTION_SCAN = "scan";

    public static Key<BlockPos> VALUE_OFFSET = new Key<>("offset", Type.BLOCKPOS);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_RSMODE, this::getRSModeInt, this::setRSModeInt),
                new DefaultValue<>(VALUE_OFFSET, this::getDataOffset, this::setDataOffset),
        };
    }

    @Override
    public IAction[] getActions() {
        return new IAction[] {
                new DefaultAction(ACTION_SCAN, this::scan)
        };
    }

    public static final int SLOT_IN = 0;
    public static final int SLOT_OUT = 1;
    public static final int SLOT_FILTER = 2;
    public static final int SLOT_MODIFIER = 3;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(BuilderSetup.shapeCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_IN, 15, 7);
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(BuilderSetup.shapeCardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_OUT, 15, 200);
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(ModularStorageSetup.storageFilterItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_FILTER, 35, 7);
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(ModItems.modifierItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_MODIFIER, 55, 7);
            layoutPlayerInventorySlots(85, 142);
        }
    };

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 4);
    private StorageFilterCache filterCache = null;

    private int scanId = 0;
    private ItemStack renderStack = ItemStack.EMPTY;
    private BlockPos dataDim;
    private BlockPos dataOffset = new BlockPos(0, 0, 0);

    // Transient data that is used during the scan.
    private ScanProgress progress = null;
    // Client side indication if there is a scan in progress
    private int progressBusy = -1;

    public ScannerTileEntity() {
        super(ScannerConfiguration.SCANNER_MAXENERGY.get(), ScannerConfiguration.SCANNER_RECEIVEPERTICK.get());
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            if (progress != null) {
                if (getStoredPower() >= getEnergyPerTick()) {
                    consumeEnergy(getEnergyPerTick());
                    int done = 0;
                    while (progress != null && done < ScannerConfiguration.surfaceAreaPerTick.get()) {
                        progressScan();
                        done += dataDim.getZ() * dataDim.getY();  // We scan planes on the x axis
                    }
                }
            } else if (isMachineEnabled()) {
                scan();
            }
        }
    }

    protected long getEnergyPerTick() {
        return ScannerConfiguration.SCANNER_PERTICK.get();
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
        if (index == SLOT_FILTER) {
            filterCache = null;
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index == SLOT_OUT) {
            if (!stack.isEmpty()) {
                updateScanCard(stack);
                markDirty();
            }
        }
        if (index == SLOT_IN) {
            if (!stack.isEmpty()) {
                dataDim = ShapeCardItem.getDimension(stack);
                if (renderStack.isEmpty()) {
                    renderStack = new ItemStack(BuilderSetup.shapeCardItem);
                }
                updateScanCard(renderStack);
                markDirty();
            }
        }
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == SLOT_FILTER) {
            filterCache = null;
        }
        return getInventoryHelper().decrStackSize(index, count);
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public int getScanId() {
        if (scanId == 0) {
            if (!world.isRemote) {
                scanId = ScanDataManager.getScans().newScan(world);
                markDirtyQuick();
            }
        }
        return scanId;
    }

    public BlockPos getDataDim() {
        return dataDim;
    }

    public BlockPos getDataOffset() {
        return dataOffset;
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(SLOT_FILTER));
        }
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        if (tagCompound.hasKey("render")) {
            renderStack = new ItemStack(tagCompound.getCompoundTag("render"));
        } else {
            renderStack = ItemStack.EMPTY;
        }

        scanId = tagCompound.getInteger("scanid");
        dataDim = new BlockPos(tagCompound.getInteger("scandimx"), tagCompound.getInteger("scandimy"), tagCompound.getInteger("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInteger("scanoffx"), tagCompound.getInteger("scanoffy"), tagCompound.getInteger("scanoffz"));
        progressBusy = tagCompound.getInteger("progress");
    }


    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        if (!renderStack.isEmpty()) {
            CompoundNBT tc = new CompoundNBT();
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
        if (progress == null || progress.dimX == 0) {
            tagCompound.setInteger("progress", -1);
        } else {
            tagCompound.setInteger("progress", (progress.x-progress.tl.getX()) * 100 / progress.dimX);
        }
    }


    public ItemStack getRenderStack() {
        ItemStack stack = inventoryHelper.getStackInSlot(SLOT_OUT);
        if (!stack.isEmpty()) {
            return stack;
        }
        if (renderStack.isEmpty()) {
            renderStack = new ItemStack(BuilderSetup.shapeCardItem);
            updateScanCard(renderStack);
        }
        return renderStack;
    }

    private void setOffset(int offsetX, int offsetY, int offsetZ) {
        dataOffset = new BlockPos(offsetX, offsetY, offsetZ);
        markDirtyClient();
    }

    public void setDataOffset(BlockPos dataOffset) {
        this.dataOffset = dataOffset;
    }

    private void updateScanCard(ItemStack cardOut) {
        if (!cardOut.isEmpty()) {
            if (!ShapeCardItem.getShape(cardOut).isScan()) {
                boolean solid = ShapeCardItem.isSolid(cardOut);
                ShapeCardItem.setShape(cardOut, Shape.SHAPE_SCAN, solid);
            }
            CompoundNBT tagOut = cardOut.getTag();
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
        if (getStackInSlot(SLOT_IN).isEmpty()) {
            // Cannot scan. No input card
            return;
        }

        BlockPos machinePos = getScanPos();
        if (machinePos == null) {
            // No valid destination. We cannot scan
            return;
        }

        int dimX = dataDim.getX();
        int dimY = dataDim.getY();
        int dimZ = dataDim.getZ();
        startScanArea(getScanCenter(), getScanDimension(), dimX, dimY, dimZ);
    }

    public World getScanWorld(int dimension) {
        World w = DimensionManager.getWorld(dimension);
        if (w == null) {
            w = getWorld().getMinecraftServer().getWorld(dimension);
        }
        return w;
    }

    protected BlockPos getScanPos() {
        return getPos();
    }

    public int getScanDimension() {
        return getWorld().provider.getDimension();
    }

    public BlockPos getScanCenter() {
        if (getScanPos() == null) {
            return null;
        }
        return getScanPos().add(dataOffset.getX(), dataOffset.getY(), dataOffset.getZ());
    }

    public BlockPos getFirstCorner() {
        if (getScanPos() == null) {
            return null;
        }
        return getScanPos().add(dataOffset.getX()-dataDim.getX()/2,
                dataOffset.getY()-dataDim.getY()/2,
                dataOffset.getZ()-dataDim.getZ()/2);
    }

    public BlockPos getLastCorner() {
        if (getScanPos() == null) {
            return null;
        }
        return getScanPos().add(dataOffset.getX()+dataDim.getX()/2,
                dataOffset.getY()+dataDim.getY()/2,
                dataOffset.getZ()+dataDim.getZ()/2);
    }

    private BlockState mapState(List<ModifierEntry> modifiers, Map<BlockState, BlockState> modifierMapping, BlockPos pos, BlockState inState) {
        if (modifiers.isEmpty()) {
            return inState;
        }
        if (!modifierMapping.containsKey(inState)) {
            BlockState outState = inState;
            boolean stop = false;
            for (ModifierEntry modifier : modifiers) {
                if (stop) {
                    break;
                }
                switch (modifier.getType()) {
                    case FILTER_SLOT: {
                        ItemStack inputItem = inState.getBlock().getItem(getWorld(), pos, inState);
                        if (!modifier.getIn().isEmpty() && modifier.getIn().getItem() == ModularStorageSetup.storageFilterItem) {
                            StorageFilterCache filter = StorageFilterItem.getCache(modifier.getIn());
                            if (filter.match(inputItem)) {
                                outState = getOutput(inState, modifier);
                                stop = true;
                            }
                        } else {
                            // Empty input stack in modifier also matches
                            if (modifier.getIn().isEmpty() || ItemStack.areItemsEqual(inputItem, modifier.getIn())) {
                                outState = getOutput(inState, modifier);
                                stop = true;
                            }
                        }
                        break;
                    }
                    case FILTER_ORE: {
                        ItemStack inputItem = inState.getBlock().getItem(getWorld(), pos, inState);
                        if (!inputItem.isEmpty()) {
                            int[] oreIDs = OreDictionary.getOreIDs(inputItem);
                            for (int id : oreIDs) {
                                if (OreDictionary.getOreName(id).startsWith("ore")) {
                                    outState = getOutput(inState, modifier);
                                    stop = true;
                                    break;
                                }
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

    private BlockState getOutput(BlockState input, ModifierEntry modifier) {
        if (modifier.getOp() == ModifierFilterOperation.OPERATION_VOID) {
            return Blocks.AIR.getDefaultState();
        }
        ItemStack outputItem = modifier.getOut();
        if (outputItem.isEmpty()) {
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
        Map<BlockState, BlockState> modifierMapping;
        RLE rle;
        BlockPos tl;
        StatePalette materialPalette;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int dimX;
        int dimY;
        int dimZ;
        int x;
        int dimension;
    }

    private void startScanArea(BlockPos center, int dimension, int dimX, int dimY, int dimZ) {
        progress = new ScanProgress();
        progress.modifiers = ModifierItem.getModifiers(getStackInSlot(SLOT_MODIFIER));
        progress.modifierMapping = new HashMap<>();
        progress.rle = new RLE();
        progress.tl = new BlockPos(center.getX() - dimX/2, center.getY() - dimY/2, center.getZ() - dimZ/2);
        progress.materialPalette = new StatePalette();
        progress.materialPalette.alloc(BuilderSetup.supportBlock.getDefaultState(), 0);
        progress.x = progress.tl.getX();
        progress.dimX = dimX;
        progress.dimY = dimY;
        progress.dimZ = dimZ;
        progress.dimension = dimension;
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
        World world = getScanWorld(progress.dimension);
        BlockPos.MutableBlockPos mpos = progress.mpos;
        for (int z = tl.getZ() ; z < tl.getZ() + dimZ ; z++) {
            for (int y = tl.getY() ; y < tl.getY() + dimY ; y++) {
                mpos.setPos(progress.x, y, z);
                int c;
                if (world.isAirBlock(mpos)) {
                    c = 0;
                } else {
                    BlockState state = world.getBlockState(mpos);
                    getFilterCache();
                    if (filterCache != null) {
                        ItemStack item = state.getBlock().getItem(world, mpos, state);
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
        scan.save(getScanId());
        if (renderStack.isEmpty()) {
            renderStack = new ItemStack(BuilderSetup.shapeCardItem);
        }
        updateScanCard(renderStack);
        markDirtyClient();
        progress = null;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextStyleClass.LABEL + "Scan id: " + TextStyleClass.INFO + getScanId());
    }

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        currenttip.add("Scan id: " + TextFormatting.WHITE + getScanId());
    }

}
