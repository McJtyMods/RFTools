package mcjty.rftools.blocks.shaper;

import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IAction;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.RedstoneMode;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.blocks.crafter.StorageFilterCache;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.modifier.ModifierEntry;
import mcjty.rftools.items.modifier.ModifierFilterOperation;
import mcjty.rftools.items.modifier.ModifierItem;
import mcjty.rftools.shapes.ScanDataManager;
import mcjty.rftools.shapes.Shape;
import mcjty.rftools.shapes.StatePalette;
import mcjty.rftools.varia.RLE;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static mcjty.rftools.blocks.builder.BuilderSetup.TYPE_SCANNER;

public class ScannerTileEntity extends GenericTileEntity implements ITickableTileEntity {

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
                    ItemStack.EMPTY /* @todo 1.14 new ItemStack(ModularStorageSetup.storageFilterItem) */), ContainerFactory.CONTAINER_CONTAINER, SLOT_FILTER, 35, 7);
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(ModItems.modifierItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_MODIFIER, 55, 7);
            layoutPlayerInventorySlots(85, 142);
        }
    };

    private StorageFilterCache filterCache = null;

    private int scanId = 0;
    private ItemStack renderStack = ItemStack.EMPTY;
    private BlockPos dataDim;
    private BlockPos dataOffset = new BlockPos(0, 0, 0);

    // Transient data that is used during the scan.
    private ScanProgress progress = null;
    // Client side indication if there is a scan in progress
    private int progressBusy = -1;

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, ScannerConfiguration.SCANNER_MAXENERGY.get(), ScannerConfiguration.SCANNER_RECEIVEPERTICK.get()));

    public ScannerTileEntity() {
        super(TYPE_SCANNER);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            if (progress != null) {
                energyHandler.ifPresent(h -> {
                    if (h.getEnergyStored() >= getEnergyPerTick()) {
                        h.consumeEnergy(getEnergyPerTick());
                        int done = 0;
                        while (progress != null && done < ScannerConfiguration.surfaceAreaPerTick.get()) {
                            progressScan();
                            done += dataDim.getZ() * dataDim.getY();  // We scan planes on the x axis
                        }
                    }
                });
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
    protected boolean needsRedstoneMode() {
        return true;
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
            // @todo 1.14
//            filterCache = StorageFilterItem.getCache(itemHandler.map(h -> h.getStackInSlot(SLOT_FILTER)).orElse(ItemStack.EMPTY));
        }
    }

    // @todo 1.14 loot tables
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        itemHandler.ifPresent(h -> h.deserializeNBT(tagCompound.getList("Items", Constants.NBT.TAG_COMPOUND)));
        energyHandler.ifPresent(h -> h.setEnergy(tagCompound.getLong("Energy")));
        if (tagCompound.contains("render")) {
            renderStack = ItemStack.read(tagCompound.getCompound("render"));
        } else {
            renderStack = ItemStack.EMPTY;
        }

        scanId = tagCompound.getInt("scanid");
        dataDim = new BlockPos(tagCompound.getInt("scandimx"), tagCompound.getInt("scandimy"), tagCompound.getInt("scandimz"));
        dataOffset = new BlockPos(tagCompound.getInt("scanoffx"), tagCompound.getInt("scanoffy"), tagCompound.getInt("scanoffz"));
        progressBusy = tagCompound.getInt("progress");
    }


    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        itemHandler.ifPresent(h -> tagCompound.put("Items", h.serializeNBT()));
        energyHandler.ifPresent(h -> tagCompound.putLong("Energy", h.getEnergy()));
        if (!renderStack.isEmpty()) {
            CompoundNBT tc = new CompoundNBT();
            renderStack.write(tc);
            tagCompound.put("render", tc);
        }
        tagCompound.putInt("scanid", getScanId());
        if (dataDim != null) {
            tagCompound.putInt("scandimx", dataDim.getX());
            tagCompound.putInt("scandimy", dataDim.getY());
            tagCompound.putInt("scandimz", dataDim.getZ());
        }
        if (dataOffset != null) {
            tagCompound.putInt("scanoffx", dataOffset.getX());
            tagCompound.putInt("scanoffy", dataOffset.getY());
            tagCompound.putInt("scanoffz", dataOffset.getZ());
        }
        if (progress == null || progress.dimX == 0) {
            tagCompound.putInt("progress", -1);
        } else {
            tagCompound.putInt("progress", (progress.x-progress.tl.getX()) * 100 / progress.dimX);
        }
        return tagCompound;
    }


    public ItemStack getRenderStack() {
        ItemStack stack = itemHandler.map(h -> h.getStackInSlot(SLOT_OUT)).orElse(ItemStack.EMPTY);
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
        if (itemHandler.map(h -> h.getStackInSlot(SLOT_IN)).filter(s -> !s.isEmpty()).isPresent()) {
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
        return WorldTools.loadWorld(dimension);
    }

    protected BlockPos getScanPos() {
        return getPos();
    }

    public int getScanDimension() {
        return world.getDimension().getType().getId();
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
                        ItemStack inputItem = inState.getBlock().getItem(world, pos, inState);
                        if (false) { // @todo 1.14 !modifier.getIn().isEmpty() && modifier.getIn().getItem() == ModularStorageSetup.storageFilterItem) {
                            StorageFilterCache filter = null; // @todo 1.14 StorageFilterItem.getCache(modifier.getIn());
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
                        ItemStack inputItem = inState.getBlock().getItem(world, pos, inState);
                        if (!inputItem.isEmpty()) {
                            // @todo 1.14 use tags
//                            int[] oreIDs = OreDictionary.getOreIDs(inputItem);
//                            for (int id : oreIDs) {
//                                if (OreDictionary.getOreName(id).startsWith("ore")) {
//                                    outState = getOutput(inState, modifier);
//                                    stop = true;
//                                    break;
//                                }
//                            }
                        }
                        break;
                    }
                    case FILTER_LIQUID:
                        if (inState.getBlock() instanceof FlowingFluidBlock) {
                            outState = getOutput(inState, modifier);
                            stop = true;
                        }
                        break;
                    case FILTER_TILEENTITY:
                        if (world.getTileEntity(pos) != null) {
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
                return block.getDefaultState();
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
        progress.modifiers = ModifierItem.getModifiers(itemHandler.map(h -> h.getStackInSlot(SLOT_MODIFIER)).orElse(ItemStack.EMPTY));
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

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextStyleClass.LABEL + "Scan id: " + TextStyleClass.INFO + getScanId());
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        currenttip.add("Scan id: " + TextFormatting.WHITE + getScanId());
//    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(ScannerTileEntity.this, CONTAINER_FACTORY, 4) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == BuilderSetup.shapeCardItem;
            }

            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                if (index == SLOT_FILTER) {
                    filterCache = null;
                }
                if (index == SLOT_OUT) {
                    ItemStack stack = getStackInSlot(index);
                    if (!stack.isEmpty()) {
                        updateScanCard(stack);
                        markDirty();
                    }
                }
                if (index == SLOT_IN) {
                    ItemStack stack = getStackInSlot(index);
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
        };
    }
}
