package mcjty.rftools.blocks.builder;

import com.mojang.authlib.GameProfile;
import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IAction;
import mcjty.lib.bindings.IValue;
import mcjty.lib.container.*;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.*;
import mcjty.rftools.ClientCommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.hud.IHudSupport;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.items.builder.ShapeCardType;
import mcjty.rftools.items.storage.StorageFilterCache;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.network.PacketGetHudLog;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.shapes.Shape;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.ShulkerBoxTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

public class BuilderTileEntity extends GenericTileEntity implements ITickableTileEntity, IHudSupport {

    public static final String CMD_SETMODE = "builder.setMode";
    public static final String CMD_SETROTATE = "builder.setRotate";

    public static final String CMD_SETANCHOR = "builder.setAnchor";
    public static final Key<Integer> PARAM_ANCHOR_INDEX = new Key<>("anchorIndex", Type.INTEGER);

    public static final String CMD_GETLEVEL = "getLevel";
    public static final Key<Integer> PARAM_LEVEL = new Key<>("level", Type.INTEGER);

    public static final int SLOT_TAB = 0;
    public static final int SLOT_FILTER = 1;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(BuilderSetup.shapeCardItem), new ItemStack(BuilderSetup.spaceChamberCardItem)),
                    ContainerFactory.CONTAINER_CONTAINER, SLOT_TAB, 100, 10, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, ItemStack.EMPTY /* @todo 1.14 should be filter item from rftools storage */, new ItemStack(BuilderSetup.spaceChamberCardItem)),
                    ContainerFactory.CONTAINER_CONTAINER, SLOT_TAB, 84, 46, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public static final ModuleSupport MODULE_SUPPORT = new ModuleSupport(SLOT_TAB) {
        @Override
        public boolean isModule(ItemStack itemStack) {
            return (itemStack.getItem() == BuilderSetup.shapeCardItem || itemStack.getItem() == BuilderSetup.spaceChamberCardItem);
        }
    };

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 2);

    public static final int MODE_COPY = 0;
    public static final int MODE_MOVE = 1;
    public static final int MODE_SWAP = 2;
    public static final int MODE_BACK = 3;
    public static final int MODE_COLLECT = 4;

    public static final String[] MODES = new String[]{"Copy", "Move", "Swap", "Back", "Collect"};

    public static final String ROTATE_0 = "0";
    public static final String ROTATE_90 = "90";
    public static final String ROTATE_180 = "180";
    public static final String ROTATE_270 = "270";

    public static final int ANCHOR_SW = 0;
    public static final int ANCHOR_SE = 1;
    public static final int ANCHOR_NW = 2;
    public static final int ANCHOR_NE = 3;

    private String lastError = null;
    private int mode = MODE_COPY;
    private int rotate = 0;
    private int anchor = ANCHOR_SW;
    private boolean silent = false;
    private boolean supportMode = false;
    private boolean entityMode = false;
    private boolean loopMode = false;
    private boolean waitMode = true;
    private boolean hilightMode = false;

    // For usage in the gui
    private static int currentLevel = 0;

    // Client-side
    private int scanLocCnt = 0;
    private static Map<BlockPos, Pair<Long, BlockPos>> scanLocClient = new HashMap<>();

    private int collectCounter = BuilderConfiguration.collectTimer.get();
    private int collectXP = 0;

    private boolean boxValid = false;
    private BlockPos minBox = null;
    private BlockPos maxBox = null;
    private BlockPos scan = null;
    private int projDx;
    private int projDy;
    private int projDz;

    private long lastHudTime = 0;
    private List<String> clientHudLog = new ArrayList<>();

    private ShapeCardType cardType = ShapeCardType.CARD_UNKNOWN;

    private StorageFilterCache filterCache = null;

    // For chunkloading with the quarry.
    // @todo 1.14
//    private ForgeChunkManager.Ticket ticket = null;
    // The currently forced chunk.
    private ChunkPos forcedChunk = null;

    // Cached set of blocks that we need to build in shaped mode
    private Map<BlockPos, BlockState> cachedBlocks = null;
    private ChunkPos cachedChunk = null;       // For which chunk are the cachedBlocks valid

    // Cached set of blocks that we want to void with the quarry.
    private Set<Block> cachedVoidableBlocks = null;

    // Drops from a block that we broke but couldn't fit in an inventory
    private List<ItemStack> overflowItems = null;

    private FakePlayer harvester = null;

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(
            this, true, BuilderConfiguration.BUILDER_MAXENERGY.get(), BuilderConfiguration.BUILDER_RECEIVEPERTICK.get()));

    public BuilderTileEntity() {
        super(BuilderSetup.TYPE_BUILDER);
        setRSMode(RedstoneMode.REDSTONE_ONREQUIRED);
    }


    public static final Key<Boolean> VALUE_WAIT = new Key<>("wait", Type.BOOLEAN);
    public static final Key<Boolean> VALUE_LOOP = new Key<>("loop", Type.BOOLEAN);
    public static final Key<Boolean> VALUE_HILIGHT = new Key<>("hilight", Type.BOOLEAN);
    public static final Key<Boolean> VALUE_SUPPORT = new Key<>("support", Type.BOOLEAN);
    public static final Key<Boolean> VALUE_SILENT = new Key<>("silent", Type.BOOLEAN);
    public static final Key<Boolean> VALUE_ENTITIES = new Key<>("entities", Type.BOOLEAN);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_RSMODE, this::getRSModeInt, this::setRSModeInt),
                new DefaultValue<>(VALUE_WAIT, this::isWaitMode, this::setWaitMode),
                new DefaultValue<>(VALUE_LOOP, this::hasLoopMode, this::setLoopMode),
                new DefaultValue<>(VALUE_HILIGHT, this::isHilightMode, this::setHilightMode),
                new DefaultValue<>(VALUE_SUPPORT, this::hasSupportMode, this::setSupportMode),
                new DefaultValue<>(VALUE_SILENT, this::isSilent, this::setSilent),
                new DefaultValue<>(VALUE_ENTITIES, this::hasEntityMode, this::setEntityMode),
        };
    }

    @Override
    public IAction[] getActions() {
        return new IAction[] {
                new DefaultAction("restart", this::restartScan)
        };
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    private FakePlayer getHarvester() {
        if (harvester == null) {
            harvester = FakePlayerFactory.get((ServerWorld) world,  new GameProfile(UUID.nameUUIDFromBytes("rftools_builder".getBytes()), "rftools_builder"));
        }
        harvester.setWorld(world);
        harvester.setPosition(pos.getX(), pos.getY(), pos.getZ());
        return harvester;
    }

    @Override
    public Direction getBlockOrientation() {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == BuilderSetup.builderBlock) {
            return OrientationTools.getOrientationHoriz(state);
        } else {
            return null;
        }
    }

    @Override
    public boolean isBlockAboveAir() {
        return world.isAirBlock(pos.up());
    }

    @Override
    public List<String> getClientLog() {
        return clientHudLog;
    }

    public List<String> getHudLog() {
        List<String> list = new ArrayList<>();
        list.add(TextFormatting.BLUE + "Mode:");
        if (isShapeCard()) {
            getCardType().addHudLog(list, inventoryHelper);
        } else {
            list.add("    Space card: " + new String[]{"copy", "move", "swap", "back", "collect"}[mode]);
        }
        if (scan != null) {
            list.add(TextFormatting.BLUE + "Progress:");
            list.add("    Y level: " + scan.getY());
            int minChunkX = minBox.getX() >> 4;
            int minChunkZ = minBox.getZ() >> 4;
            int maxChunkX = maxBox.getX() >> 4;
            int maxChunkZ = maxBox.getZ() >> 4;
            int curX = scan.getX() >> 4;
            int curZ = scan.getZ() >> 4;
            int totChunks = (maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1);
            int curChunk = (curZ - minChunkZ) * (maxChunkX - minChunkX) + curX - minChunkX;
            list.add("    Chunk:  " + curChunk + " of " + totChunks);
        }
        if (lastError != null && !lastError.isEmpty()) {
            String[] errors = StringUtils.split(lastError, "\n");
            for (String error : errors) {
                list.add(TextFormatting.RED + error);
            }
        }
        return list;
    }

    @Override
    public BlockPos getBlockPos() {
        return getPos();
    }

    @Override
    public long getLastUpdateTime() {
        return lastHudTime;
    }

    @Override
    public void setLastUpdateTime(long t) {
        lastHudTime = t;
    }

    private boolean isShapeCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(SLOT_TAB);
        if (itemStack.isEmpty()) {
            return false;
        }
        return itemStack.getItem() == BuilderSetup.shapeCardItem;
    }

    private CompoundNBT hasCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(SLOT_TAB);
        if (itemStack.isEmpty()) {
            return null;
        }

        return itemStack.getTag();
    }

    private void makeSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());
        Shape shape = ShapeCardItem.getShape(shapeCard);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), world, getPos(), dimension, offset, blocks, BuilderConfiguration.maxBuilderDimension.get() * 256 * BuilderConfiguration.maxBuilderDimension.get(), false, false, null);
        BlockState state = BuilderSetup.supportBlock.getDefaultState().with(SupportBlock.STATUS, SupportBlock.STATUS_OK);
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos p = entry.getKey();
            if (world.isAirBlock(p)) {
                world.setBlockState(p, state, 2);
            }
        }
    }

    private void makeSupportBlocks() {
        if (isShapeCard()) {
            makeSupportBlocksShaped();
            return;
        }

        SpaceChamberRepository.SpaceChamberChannel chamberChannel = calculateBox();
        if (chamberChannel != null) {
            int dimension = chamberChannel.getDimension();
            World world = DimensionManager.getWorld(dimension);
            if (world == null) {
                return;
            }

            BlockPos.MutableBlockPos src = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos dest = new BlockPos.MutableBlockPos();
            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
                for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                    for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                        src.setPos(x, y, z);
                        sourceToDest(src, dest);
                        BlockState srcState = world.getBlockState(src);
                        Block srcBlock = srcState.getBlock();
                        BlockState dstState = world.getBlockState(dest);
                        Block dstBlock = dstState.getBlock();
                        int error = SupportBlock.STATUS_OK;
                        if (mode != MODE_COPY) {
                            TileEntity srcTileEntity = world.getTileEntity(src);
                            TileEntity dstTileEntity = world.getTileEntity(dest);

                            int error1 = isMovable(world, src, srcBlock, srcTileEntity);
                            int error2 = isMovable(world, dest, dstBlock, dstTileEntity);
                            error = Math.max(error1, error2);
                        }
                        if (isEmpty(srcState, srcBlock) && !isEmpty(dstState, dstBlock)) {
                            world.setBlockState(src, BuilderSetup.supportBlock.getDefaultState().with(SupportBlock.STATUS, error), 3);
                        }
                        if (isEmpty(dstState, dstBlock) && !isEmpty(srcState, srcBlock)) {
                            world.setBlockState(dest, BuilderSetup.supportBlock.getDefaultState().with(SupportBlock.STATUS, error), 3);
                        }
                    }
                }
            }
        }
    }

    private void clearSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());
        Shape shape = ShapeCardItem.getShape(shapeCard);
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), world, getPos(), dimension, offset, blocks, BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get(), false, false, null);
        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos p = entry.getKey();
            if (world.getBlockState(p).getBlock() == BuilderSetup.supportBlock) {
                world.setBlockState(p, Blocks.AIR.getDefaultState());
            }
        }
    }

    public void clearSupportBlocks() {
        if (world.isRemote) {
            // Don't do anything on the client.
            return;
        }

        if (isShapeCard()) {
            clearSupportBlocksShaped();
            return;
        }

        SpaceChamberRepository.SpaceChamberChannel chamberChannel = calculateBox();
        if (chamberChannel != null) {
            int dimension = chamberChannel.getDimension();
            World world = WorldTools.getWorld(dimension);

            BlockPos.MutableBlockPos src = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos dest = new BlockPos.MutableBlockPos();
            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
                for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                    for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                        src.setPos(x, y, z);
                        if (world != null) {
                            Block srcBlock = world.getBlockState(src).getBlock();
                            if (srcBlock == BuilderSetup.supportBlock) {
                                world.setBlockState(src, Blocks.AIR.getDefaultState());
                            }
                        }
                        sourceToDest(src, dest);
                        Block dstBlock = world.getBlockState(dest).getBlock();
                        if (dstBlock == BuilderSetup.supportBlock) {
                            world.setBlockState(dest, Blocks.AIR.getDefaultState());
                        }
                    }
                }
            }
        }
    }

    public boolean isHilightMode() {
        return hilightMode;
    }

    public void setHilightMode(boolean hilightMode) {
        this.hilightMode = hilightMode;
    }

    public boolean isWaitMode() {
        return waitMode;
    }

    public void setWaitMode(boolean waitMode) {
        this.waitMode = waitMode;
        markDirtyClient();
    }

    private boolean waitOrSkip(String error) {
        if (waitMode) {
            lastError = error;
        }
        return waitMode;
    }

    private boolean skip() {
        lastError = null;
        return false;
    }

    public boolean suspend(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        lastError = null;
        return true;
    }

    private boolean suspend(String error) {
        lastError = error;
        return true;
    }

    public boolean hasLoopMode() {
        return loopMode;
    }

    public void setLoopMode(boolean loopMode) {
        this.loopMode = loopMode;
        markDirtyClient();
    }

    public boolean hasEntityMode() {
        return entityMode;
    }

    public void setEntityMode(boolean entityMode) {
        this.entityMode = entityMode;
        markDirtyClient();
    }

    public boolean hasSupportMode() {
        return supportMode;
    }

    public void setSupportMode(boolean supportMode) {
        this.supportMode = supportMode;
        if (supportMode) {
            makeSupportBlocks();
        } else {
            clearSupportBlocks();
        }
        markDirtyClient();
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
        markDirtyClient();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode != this.mode) {
            this.mode = mode;
            restartScan();
            markDirtyClient();
        }
    }

    public void resetBox() {
        boxValid = false;
    }

    public int getAnchor() {
        return anchor;
    }

    public void setAnchor(int anchor) {
        if (supportMode) {
            clearSupportBlocks();
        }
        boxValid = false;

        this.anchor = anchor;

        if (isShapeCard()) {
            // If there is a shape card we modify it for the new settings.
            ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
            BlockPos dimension = ShapeCardItem.getDimension(shapeCard);
            BlockPos minBox = positionBox(dimension);
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();

            BlockPos offset = new BlockPos(minBox.getX() + (int) Math.ceil(dx / 2), minBox.getY() + (int) Math.ceil(dy / 2), minBox.getZ() + (int) Math.ceil(dz / 2));
            ShapeCardItem.setOffset(shapeCard, offset.getX(), offset.getY(), offset.getZ());
        }

        if (supportMode) {
            makeSupportBlocks();
        }
        markDirtyClient();
    }

    // Give a dimension, return a min coordinate of the box right in front of the builder
    private BlockPos positionBox(BlockPos dimension) {
        BlockState state = world.getBlockState(getPos());
        Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
        int spanX = dimension.getX();
        int spanY = dimension.getY();
        int spanZ = dimension.getZ();
        int x = 0;
        int y;
        int z = 0;
        y = -((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY - 1 : 0);
        switch (direction) {
            case SOUTH:
                x = -((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX - 1 : 0);
                z = -spanZ;
                break;
            case NORTH:
                x = 1 - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX - 1 : 0);
                z = 1;
                break;
            case WEST:
                x = 1;
                z = -((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ - 1 : 0);
                break;
            case EAST:
                x = -spanX;
                z = -((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? 0 : spanZ - 1);
                break;
            case DOWN:
            case UP:
            default:
                break;
        }
        return new BlockPos(x, y, z);
    }


    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        if (supportMode) {
            clearSupportBlocks();
        }
        boxValid = false;
        this.rotate = rotate;
        if (supportMode) {
            makeSupportBlocks();
        }
        markDirtyClient();
    }

    @Override
    public void setPowerInput(int powered) {
        boolean o = isMachineEnabled();
        super.setPowerInput(powered);
        boolean n = isMachineEnabled();
        if (o != n) {
            if (loopMode || (n && scan == null)) {
                restartScan();
            }
        }
    }

    private void createProjection(SpaceChamberRepository.SpaceChamberChannel chamberChannel) {
        BlockPos minC = rotate(chamberChannel.getMinCorner());
        BlockPos maxC = rotate(chamberChannel.getMaxCorner());
        BlockPos minCorner = new BlockPos(Math.min(minC.getX(), maxC.getX()), Math.min(minC.getY(), maxC.getY()), Math.min(minC.getZ(), maxC.getZ()));
        BlockPos maxCorner = new BlockPos(Math.max(minC.getX(), maxC.getX()), Math.max(minC.getY(), maxC.getY()), Math.max(minC.getZ(), maxC.getZ()));

        BlockState state = world.getBlockState(getPos());
        Direction direction = state.get(BlockStateProperties.HORIZONTAL_FACING);
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        int spanX = maxCorner.getX() - minCorner.getX();
        int spanY = maxCorner.getY() - minCorner.getY();
        int spanZ = maxCorner.getZ() - minCorner.getZ();
        switch (direction) {
            case SOUTH:
                projDx = xCoord + Direction.NORTH.getDirectionVec().getX() - minCorner.getX() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + Direction.NORTH.getDirectionVec().getZ() - minCorner.getZ() - spanZ;
                break;
            case NORTH:
                projDx = xCoord + Direction.SOUTH.getDirectionVec().getX() - minCorner.getX() - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + Direction.SOUTH.getDirectionVec().getZ() - minCorner.getZ();
                break;
            case WEST:
                projDx = xCoord + Direction.EAST.getDirectionVec().getX() - minCorner.getX();
                projDz = zCoord + Direction.EAST.getDirectionVec().getZ() - minCorner.getZ() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case EAST:
                projDx = xCoord + Direction.WEST.getDirectionVec().getX() - minCorner.getX() - spanX;
                projDz = zCoord + Direction.WEST.getDirectionVec().getZ() - minCorner.getZ() - spanZ + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case DOWN:
            case UP:
            default:
                break;
        }
        projDy = yCoord - minCorner.getY() - ((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY : 0);
    }

    private void calculateBox(CompoundNBT cardCompound) {
        int channel = cardCompound.getInt("channel");

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(world);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        BlockPos minCorner = chamberChannel.getMinCorner();
        BlockPos maxCorner = chamberChannel.getMaxCorner();
        if (minCorner == null || maxCorner == null) {
            return;
        }

        if (boxValid) {
            // Double check if the box is indeed still valid.
            if (minCorner.equals(minBox) && maxCorner.equals(maxBox)) {
                return;
            }
        }

        boxValid = true;
        cardType = ShapeCardType.CARD_SPACE;

        createProjection(chamberChannel);

        minBox = minCorner;
        maxBox = maxCorner;
        restartScan();
    }

    private void checkStateServerShaped() {
        float factor = getInfusedFactor();
        for (int i = 0; i < BuilderConfiguration.quarryBaseSpeed.get() + (factor * BuilderConfiguration.quarryInfusionSpeedFactor.get()); i++) {
            if (scan != null) {
                handleBlockShaped();
            }
        }
    }


    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (overflowItems != null && insertItems(overflowItems)) {
            overflowItems = null;
        }

        if (!isMachineEnabled() && loopMode) {
            return;
        }

        if (scan == null) {
            return;
        }

        if (isHilightMode()) {
            updateHilight();
        }

        if (isShapeCard()) {
            if (!isMachineEnabled()) {
                chunkUnload();
                return;
            }
            checkStateServerShaped();
            return;
        }

        SpaceChamberRepository.SpaceChamberChannel chamberChannel = calculateBox();
        if (chamberChannel == null) {
            scan = null;
            markDirty();
            return;
        }

        int dimension = chamberChannel.getDimension();
        World world = WorldTools.getWorld(dimension);
        if (world == null) {
            // The other location must be loaded.
            return;
        }

        if (mode == MODE_COLLECT) {
            collectItems(world);
        } else {
            float factor = getInfusedFactor();
            for (int i = 0; i < 2 + (factor * 40); i++) {
                if (scan != null) {
                    handleBlock(world);
                }
            }
        }
    }

    public List<ItemStack> getOverflowItems() {
        return overflowItems;
    }

    private void updateHilight() {
        scanLocCnt--;
        if (scanLocCnt <= 0) {
            scanLocCnt = 5;
            int x = scan.getX();
            int y = scan.getY();
            int z = scan.getZ();
            double sqradius = 30 * 30;
            for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if (player.dimension.equals(world.getDimension())) {
                    double d0 = x - player.posX;
                    double d1 = y - player.posY;
                    double d2 = z - player.posZ;
                    if (d0 * d0 + d1 * d1 + d2 * d2 < sqradius) {
                        RFToolsMessages.sendToClient(player, ClientCommandHandler.CMD_POSITION_TO_CLIENT,
                                TypedMap.builder().put(ClientCommandHandler.PARAM_POS, getPos()).put(ClientCommandHandler.PARAM_SCAN, scan));
                    }
                }
            }
        }
    }

    private void collectItems(World world) {
        // Collect item mode
        collectCounter--;
        if (collectCounter > 0) {
            return;
        }
        collectCounter = BuilderConfiguration.collectTimer.get();
        if (!loopMode) {
            scan = null;
        }

        if (!energyHandler.map(h -> {
                    long rf = h.getEnergyStored();
                    float area = (maxBox.getX() - minBox.getX() + 1) * (maxBox.getY() - minBox.getY() + 1) * (maxBox.getZ() - minBox.getZ() + 1);
                    float infusedFactor = (4.0f - getInfusedFactor()) / 4.0f;
                    int rfNeeded = (int) (BuilderConfiguration.collectRFPerTickPerArea.get() * area * infusedFactor) * BuilderConfiguration.collectTimer.get();
                    if (rfNeeded > rf) {
                        // Not enough energy.
                        return false;
                    }
                    h.consumeEnergy(rfNeeded);
                    return true;
                }).orElse(false)) {
            return;
        }

        AxisAlignedBB bb = new AxisAlignedBB(minBox.getX() - .8, minBox.getY() - .8, minBox.getZ() - .8, maxBox.getX() + .8, maxBox.getY() + .8, maxBox.getZ() + .8);
        List<Entity> items = world.getEntitiesWithinAABB(Entity.class, bb);
        for (Entity entity : items) {
            if (entity instanceof ItemEntity) {
                if (collectItem(world, getInfusedFactor(), (ItemEntity) entity)) {
                    return;
                }
            } else if (entity instanceof ExperienceOrbEntity) {
                if (collectXP(world, getInfusedFactor(), (ExperienceOrbEntity) entity)) {
                    return;
                }
            }
        }
    }

    private boolean collectXP(World world, float infusedFactor, ExperienceOrbEntity orb) {
        return energyHandler.map(h -> {
            int xp = orb.getXpValue();
            long rf = h.getEnergyStored();
            int rfNeeded = (int) (BuilderConfiguration.collectRFPerXP.get() * infusedFactor * xp);
            if (rfNeeded > rf) {
                // Not enough energy.
                return true;
            }

            collectXP += xp;

            int bottles = collectXP / 7;
            if (bottles > 0) {
                if (insertItem(new ItemStack(Items.EXPERIENCE_BOTTLE, bottles)).isEmpty()) {
                    collectXP = collectXP % 7;
                    ((ServerWorld)world).removeEntity(orb);
                    h.consumeEnergy(rfNeeded);
                } else {
                    collectXP = 0;
                }
            }

            return false;
        }).orElse(false);
    }

    private boolean collectItem(World world, float infusedFactor, ItemEntity item) {
        return energyHandler.map(h -> {
            ItemStack stack = item.getItem();
            long rf = h.getEnergyStored();
            int rfNeeded = (int) (BuilderConfiguration.collectRFPerItem.get() * infusedFactor) * stack.getCount();
            if (rfNeeded > rf) {
                // Not enough energy.
                return true;
            }
            h.consumeEnergy(rfNeeded);

            ((ServerWorld) world).removeEntity(item);
            stack = insertItem(stack);
            if (!stack.isEmpty()) {
                BlockPos position = item.getPosition();
                ItemEntity entityItem = new ItemEntity(world, position.getX(), position.getY(), position.getZ(), stack);
                world.addEntity(entityItem);
            }
            return false;
        }).orElse(false);
    }

    private void calculateBoxShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
        if (shapeCard.isEmpty()) {
            return;
        }
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());

        BlockPos minCorner = ShapeCardItem.getMinCorner(getPos(), dimension, offset);
        BlockPos maxCorner = ShapeCardItem.getMaxCorner(getPos(), dimension, offset);
        if (minCorner.getY() < 0) {
            minCorner = new BlockPos(minCorner.getX(), 0, minCorner.getZ());
        } else if (minCorner.getY() > 255) {
            minCorner = new BlockPos(minCorner.getX(), 255, minCorner.getZ());
        }
        if (maxCorner.getY() < 0) {
            maxCorner = new BlockPos(maxCorner.getX(), 0, maxCorner.getZ());
        } else if (maxCorner.getY() > 255) {
            maxCorner = new BlockPos(maxCorner.getX(), 255, maxCorner.getZ());
        }

        if (boxValid) {
            // Double check if the box is indeed still valid.
            if (minCorner.equals(minBox) && maxCorner.equals(maxBox)) {
                return;
            }
        }

        boxValid = true;
        cardType = ShapeCardType.fromDamage(shapeCard.getDamage()); // @todo 1.14 don't use damage!

        cachedBlocks = null;
        cachedChunk = null;
        cachedVoidableBlocks = null;
        minBox = minCorner;
        maxBox = maxCorner;
        restartScan();
    }

    private SpaceChamberRepository.SpaceChamberChannel calculateBox() {
        CompoundNBT tc = hasCard();
        if (tc == null) {
            return null;
        }

        int channel = tc.getInt("channel");
        if (channel == -1) {
            return null;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(world);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        if (chamberChannel == null) {
            return null;
        }

        calculateBox(tc);

        if (!boxValid) {
            return null;
        }
        return chamberChannel;
    }

    private Map<BlockPos, BlockState> getCachedBlocks(ChunkPos chunk) {
        if ((chunk != null && !chunk.equals(cachedChunk)) || (chunk == null && cachedChunk != null)) {
            cachedBlocks = null;
        }

        if (cachedBlocks == null) {
            cachedBlocks = new HashMap<>();
            ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
            Shape shape = ShapeCardItem.getShape(shapeCard);
            boolean solid = ShapeCardItem.isSolid(shapeCard);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension.get());
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset.get());
            boolean forquarry = !ShapeCardItem.isNormalShapeCard(shapeCard);
            ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), world, getPos(), dimension, offset, cachedBlocks, BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get() * BuilderConfiguration.maxSpaceChamberDimension.get(), solid, forquarry, chunk);
            cachedChunk = chunk;
        }
        return cachedBlocks;
    }

    private void handleBlockShaped() {
        for (int i = 0; i < 100; i++) {
            if (scan == null) {
                return;
            }
            Map<BlockPos, BlockState> blocks = getCachedBlocks(new ChunkPos(scan.getX() >> 4, scan.getZ() >> 4));
            if (blocks.containsKey(scan)) {
                BlockState state = blocks.get(scan);
                if (!handleSingleBlock(state)) {
                    nextLocation();
                }
                return;
            } else {
                nextLocation();
            }
        }
    }

    private ShapeCardType getCardType() {
        if (cardType == ShapeCardType.CARD_UNKNOWN) {
            ItemStack card = inventoryHelper.getStackInSlot(SLOT_TAB);
            if (!card.isEmpty()) {
                cardType = ShapeCardType.fromDamage(card.getDamage());  // @todo 1.14 don't use damage!
            }
        }
        return cardType;
    }

    // Return true if we have to wait at this spot.
    private boolean handleSingleBlock(BlockState pickState) {
        BlockPos srcPos = scan;
        int sx = scan.getX();
        int sy = scan.getY();
        int sz = scan.getZ();
        if (!chunkLoad(sx, sz)) {
            // The chunk is not available and we could not chunkload it. We have to wait.
            return suspend("Chunk not available!");
        }

        int rfNeeded = getCardType().getRfNeeded();

        BlockState state = null;
        if (getCardType() != ShapeCardType.CARD_SHAPE && getCardType() != ShapeCardType.CARD_PUMP_LIQUID) {
            state = world.getBlockState(srcPos);
            Block block = state.getBlock();
            if (!isEmpty(state, block)) {
                float hardness;
                if (isFluidBlock(block)) {
                    hardness = 1.0f;
                } else {
                    if (getCachedVoidableBlocks().contains(block)) {
                        rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry.get() * BuilderConfiguration.voidShapeCardFactor.get());
                    }
                    hardness = block.getBlockHardness(state, world, srcPos);
                }
                rfNeeded *= (int) ((hardness + 1) * 2);
            }
        }

        rfNeeded = (int) (rfNeeded * (3.0f - getInfusedFactor()) / 3.0f);

        if (rfNeeded > energyHandler.map(h -> h.getEnergyStored()).orElse(0)) {
            // Not enough energy.
            return suspend("Not enough power!");
        }

        return getCardType().handleSingleBlock(this, rfNeeded, srcPos, state, pickState);
    }

    public boolean buildBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        if (isEmptyOrReplacable(world, srcPos)) {
            TakeableItem item = createTakeableItem(world, srcPos, pickState);
            ItemStack stack = item.peek();
            if (stack.isEmpty()) {
                return waitOrSkip("Cannot find block!\nor missing inventory\non top or below");    // We could not find a block. Wait
            }

            FakePlayer fakePlayer = getHarvester();
            BlockState newState = BlockTools.placeStackAt(fakePlayer, stack, world, srcPos, pickState);
            if (!ItemStack.areItemStacksEqual(stack, item.peek())) { // Did we actually use up whatever we were holding?
                if(!stack.isEmpty()) { // Are we holding something else that we should put back?
                    stack = item.takeAndReplace(stack); // First try to put our new item where we got what we placed
                    if(!stack.isEmpty()) { // If that didn't work, then try to put it anywhere it will fit
                        stack = insertItem(stack);
                        if(!stack.isEmpty()) { // If that still didn't work, then just drop whatever we're holding
                            world.addEntity(new ItemEntity(world, getPos().getX(), getPos().getY(), getPos().getZ(), stack));
                        }
                    }
                } else {
                    item.take(); // If we aren't holding anything, then just consume what we placed
                }
            }

            if (!silent) {
                SoundTools.playSound(world, newState.getBlock().getSoundType(newState, world, srcPos, fakePlayer).getPlaceSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
            }

            energyHandler.ifPresent(h -> h.consumeEnergy(rfNeeded));
        }
        return skip();
    }

    private Set<Block> getCachedVoidableBlocks() {
        if (cachedVoidableBlocks == null) {
            ItemStack card = inventoryHelper.getStackInSlot(SLOT_TAB);
            if (!card.isEmpty() && card.getItem() == BuilderSetup.shapeCardItem) {
                cachedVoidableBlocks = ShapeCardItem.getVoidedBlocks(card);
            } else {
                cachedVoidableBlocks = Collections.emptySet();
            }
        }
        return cachedVoidableBlocks;
    }

    private void clearOrDirtBlock(int rfNeeded, BlockPos spos, BlockState srcState, boolean clear) {
        // @todo 1.14 is this hack still needed in 1.14?
        if (srcState.getBlock() instanceof ShulkerBoxBlock) {
            TileEntity te = world.getTileEntity(spos);
            if(te instanceof ShulkerBoxTileEntity) {
                ((ShulkerBoxTileEntity)te).clear(); // We already collected a drop before we called this. Clear to make sure setBlockState doesn't spawn another.
            }
        }

        if (clear) {
            world.setBlockState(spos, Blocks.AIR.getDefaultState());
        } else {
            world.setBlockState(spos, getReplacementBlock(), 2);       // No block update!
        }
        energyHandler.ifPresent(h -> h.consumeEnergy(rfNeeded));
        if (!silent) {
            SoundTools.playSound(world, srcState.getBlock().getSoundType(srcState, world, spos, null).getBreakSound(), spos.getX(), spos.getY(), spos.getZ(), 1.0f, 1.0f);
        }
    }

    private BlockState getReplacementBlock() {
        return BuilderConfiguration.getQuarryReplace();
    }

    public boolean silkQuarryBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        return commonQuarryBlock(true, rfNeeded, srcPos, srcState);
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(SLOT_FILTER));
        }
    }

    public static boolean allowedToBreak(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, player)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean quarryBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        return commonQuarryBlock(false, rfNeeded, srcPos, srcState);
    }

    private boolean commonQuarryBlock(boolean silk, int rfNeeded, BlockPos srcPos, BlockState srcState) {
        Block block = srcState.getBlock();
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        int sx = srcPos.getX();
        int sy = srcPos.getY();
        int sz = srcPos.getZ();
        if (sx >= xCoord - 1 && sx <= xCoord + 1 && sy >= yCoord - 1 && sy <= yCoord + 1 && sz >= zCoord - 1 && sz <= zCoord + 1) {
            // Skip a 3x3x3 block around the builder.
            return skip();
        }
        if (isEmpty(srcState, block)) {
            return skip();
        }
        if (block.getBlockHardness(srcState, world, srcPos) >= 0) {
            boolean clear = getCardType().isClearing();
            if ((!clear) && srcState == getReplacementBlock()) {
                // We can skip dirt if we are not clearing.
                return skip();
            }
            if ((!BuilderConfiguration.quarryTileEntities.get()) && world.getTileEntity(srcPos) != null) {
                // Skip tile entities
                return skip();
            }

            FakePlayer fakePlayer = getHarvester();
            if (allowedToBreak(srcState, world, srcPos, fakePlayer)) {
                ItemStack filter = itemHandler.map(h -> h.getStackInSlot(SLOT_FILTER)).orElse(ItemStack.EMPTY);
                if (!filter.isEmpty()) {
                    getFilterCache();
                    if (filterCache != null) {
                        boolean match = filterCache.match(block.getItem(world, srcPos, srcState));
                        if (!match) {
                            energyHandler.ifPresent(h -> h.consumeEnergy(Math.min(rfNeeded, BuilderConfiguration.builderRfPerSkipped.get())));
                            return skip();   // Skip this
                        }
                    }
                }
                if (!getCachedVoidableBlocks().contains(block)) {
                    if (overflowItems != null) {
                        // Don't harvest any new blocks if we're still overflowing with the drops from a previous block
                        return waitOrSkip("Not enough room!\nor no usable storage\non top or below!");
                    }

                    List<ItemStack> drops;

                    // @todo 1.14 needs to be totally rewritten!
//                    if(silk && block.canSilkHarvest(world, srcPos, srcState, fakePlayer)) {
//                        ItemStack drop;
//                        try {
//                            drop = (ItemStack) ModSetup.Block_getSilkTouch.invoke(block, srcState);
//                        } catch (IllegalAccessException|InvocationTargetException e) {
//                            throw new RuntimeException(e);
//                        }
//                        drops = new ArrayList<>();
//                        if (!drop.isEmpty()) {
//                            drops.add(drop);
//                        }
//                        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, srcPos, srcState, 0, 1.0f, true, fakePlayer);
//                    } else {
//                        int fortune = getCardType().isFortune() ? 3 : 0;
//                        if (block instanceof BlockShulkerBox) {
//                            // Shulker boxes drop in setBlockState, rather than anywhere sensible. Work around this.
//                            drops = new ArrayList<>();
//                            TileEntity te = world.getTileEntity(srcPos);
//                            if (te instanceof TileEntityShulkerBox) {
//                                TileEntityShulkerBox teShulkerBox = (TileEntityShulkerBox)te;
//                                ItemStack stack = new ItemStack(Item.getItemFromBlock(block));
//                                teShulkerBox.saveToNbt(stack.getOrCreateSubCompound("BlockEntityTag"));
//                                if (teShulkerBox.hasCustomName()) {
//                                    stack.setStackDisplayName(teShulkerBox.getName());
//                                }
//                                drops.add(stack);
//                            }
//                        } else {
//                            drops = block.getDrops(world, srcPos, srcState, fortune);
//                        }
//                        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, world, srcPos, srcState, fortune, 1.0f, false, fakePlayer);
//                    }
//                    if (checkValidItems(block, drops) && !insertItems(drops)) {
//                        overflowItems = drops;
//                        clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
//                        return waitOrSkip("Not enough room!\nor no usable storage\non top or below!");    // Not enough room. Wait
//                    }
                }
                clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
            }
        }
        return silk ? skip() : false;
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof FlowingFluidBlock;
    }

    private static int getFluidLevel(BlockState srcState) {
        if (srcState.getBlock() instanceof FlowingFluidBlock) {
            return srcState.get(FlowingFluidBlock.LEVEL);
        }
        return -1;
    }

    public boolean placeLiquidBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {

        if (isEmptyOrReplacable(world, srcPos)) {
            FluidStack stack = consumeLiquid(world, srcPos);
            if (stack == null) {
                return waitOrSkip("Cannot find liquid!\nor no usable tank\nabove or below");    // We could not find a block. Wait
            }

            Fluid fluid = stack.getFluid();
            if (fluid.doesVaporize(stack) && world.getDimension().doesWaterVaporize()) {
                fluid.vaporize(null, world, srcPos, stack);
            } else {
                // We assume here the liquid is placable.
                Block block = fluid.getBlock();
                FakePlayer fakePlayer = getHarvester();
                world.setBlockState(srcPos, block.getDefaultState(), 11);

                if (!silent) {
                    SoundTools.playSound(world, block.getSoundType(block.getDefaultState(), world, srcPos, fakePlayer).getPlaceSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
                }
            }

            energyHandler.ifPresent(h -> h.consumeEnergy(rfNeeded));
        }
        return skip();
    }

    public boolean pumpBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        Block block = srcState.getBlock();
        // @todo 1.14
//        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
//        if (fluid == null) {
//            return skip();
//        }
//        if (!isFluidBlock(block)) {
//            return skip();
//        }
//
//        if (getFluidLevel(srcState) != 0) {
//            return skip();
//        }
//
//        if (block.getBlockHardness(srcState, world, srcPos) >= 0) {
//            FakePlayer fakePlayer = getHarvester();
//            if (allowedToBreak(srcState, world, srcPos, fakePlayer)) {
//                if (checkAndInsertFluids(fluid)) {
//                    consumeEnergy(rfNeeded);
//                    boolean clear = getCardType().isClearing();
//                    if (clear) {
//                        world.setBlockToAir(srcPos);
//                    } else {
//                        world.setBlockState(srcPos, getReplacementBlock(), 2);       // No block update!
//                    }
//                    if (!silent) {
//                        SoundTools.playSound(world, block.getSoundType(srcState, world, srcPos, fakePlayer).getBreakSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
//                    }
//                    return skip();
//                }
//                return waitOrSkip("No room for liquid\nor no usable tank\nabove or below!");    // No room in tanks or not a valid tank: wait
//            }
//        }
        return skip();
    }

    public boolean voidBlock(int rfNeeded, BlockPos srcPos, BlockState srcState, BlockState pickState) {
        Block block = srcState.getBlock();
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        int sx = srcPos.getX();
        int sy = srcPos.getY();
        int sz = srcPos.getZ();
        if (sx >= xCoord - 1 && sx <= xCoord + 1 && sy >= yCoord - 1 && sy <= yCoord + 1 && sz >= zCoord - 1 && sz <= zCoord + 1) {
            // Skip a 3x3x3 block around the builder.
            return skip();
        }
        FakePlayer fakePlayer = getHarvester();
        if (allowedToBreak(srcState, world, srcPos, fakePlayer)) {
            if (block.getBlockHardness(srcState, world, srcPos) >= 0) {
                ItemStack filter = itemHandler.map(h -> h.getStackInSlot(SLOT_FILTER)).orElse(ItemStack.EMPTY);
                if (!filter.isEmpty()) {
                    getFilterCache();
                    if (filterCache != null) {
                        boolean match = filterCache.match(block.getItem(world, srcPos, srcState));
                        if (!match) {
                            energyHandler.ifPresent(h -> h.consumeEnergy(Math.min(rfNeeded, BuilderConfiguration.builderRfPerSkipped.get())));
                            return skip();   // Skip this
                        }
                    }
                }

                if (!silent) {
                    SoundTools.playSound(world, block.getSoundType(srcState, world, srcPos, fakePlayer).getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
                }
                world.setBlockState(srcPos, Blocks.AIR.getDefaultState());
                energyHandler.ifPresent(h -> h.consumeEnergy(rfNeeded));
            }
        }
        return skip();
    }

    private void handleBlock(World world) {
        BlockPos srcPos = scan;
        BlockPos destPos = sourceToDest(scan);
        int x = scan.getX();
        int y = scan.getY();
        int z = scan.getZ();
        int destX = destPos.getX();
        int destY = destPos.getY();
        int destZ = destPos.getZ();

        switch (mode) {
            case MODE_COPY:
                copyBlock(world, srcPos, world, destPos);
                break;
            case MODE_MOVE:
                if (entityMode) {
                    moveEntities(world, x, y, z, world, destX, destY, destZ);
                }
                moveBlock(world, srcPos, world, destPos, rotate);
                break;
            case MODE_BACK:
                if (entityMode) {
                    moveEntities(world, destX, destY, destZ, world, x, y, z);
                }
                moveBlock(world, destPos, world, srcPos, oppositeRotate());
                break;
            case MODE_SWAP:
                if (entityMode) {
                    swapEntities(world, x, y, z, world, destX, destY, destZ);
                }
                swapBlock(world, srcPos, world, destPos);
                break;
        }

        nextLocation();
    }

    private static Random random = new Random();

    // Also works if block is null and just picks the first available block.
    private TakeableItem findBlockTakeableItem(IItemHandler inventory, World srcWorld, BlockPos srcPos, BlockState state) {
        if (state == null) {
            // We are not looking for a specific block. Pick a random one out of the chest.
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (isPlacable(inventory.getStackInSlot(i))) {
                    slots.add(i);
                }
            }
            if (!slots.isEmpty()) {
                return new TakeableItem(inventory, slots.get(random.nextInt(slots.size())));
            }
        } else {
            Block block = state.getBlock();
            ItemStack srcItem = block.getItem(srcWorld, srcPos, state);
            if (isPlacable(srcItem)) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.isItemEqual(srcItem)) {
                        return new TakeableItem(inventory, i);
                    }
                }
            }
        }
        return TakeableItem.EMPTY;
    }

    private boolean isPlacable(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof BlockItem || item instanceof IPlantable;
    }

    // the items that we try to insert.
    private boolean checkValidItems(Block block, List<ItemStack> items) {
        for (ItemStack stack : items) {
            if ((!stack.isEmpty()) && stack.getItem() == null) {
                Logging.logError("Builder tried to quarry " + block.getRegistryName().toString() + " and it returned null item!");
                Broadcaster.broadcast(world, pos.getX(), pos.getY(), pos.getZ(), "Builder tried to quarry "
                                + block.getRegistryName().toString() + " and it returned null item!\nPlease report to mod author!",
                        10);
                return false; // We don't wait for this. Just skip the item
            }
        }
        return true;
    }

    private boolean checkAndInsertFluids(Fluid fluid) {
        if (checkFluidTank(fluid, getPos().up(), Direction.DOWN)) {
            return true;
        }
        if (checkFluidTank(fluid, getPos().down(), Direction.UP)) {
            return true;
        }
        return false;
    }

    private boolean checkFluidTank(Fluid fluid, BlockPos up, Direction side) {
        TileEntity te = world.getTileEntity(up);
        if (te != null) {
            return te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side).map(h -> {
                FluidStack fluidStack = new FluidStack(fluid, 1000);
                int amount = h.fill(fluidStack, false);
                if (amount == 1000) {
                    h.fill(fluidStack, true);
                    return true;
                }
                return false;
            }).orElse(false);
        }
        return false;
    }

    private boolean insertItems(List<ItemStack> items) {
        TileEntity te = world.getTileEntity(getPos().up());
        boolean ok = InventoryHelper.insertItemsAtomic(items, te, Direction.DOWN);
        if (!ok) {
            te = world.getTileEntity(getPos().down());
            ok = InventoryHelper.insertItemsAtomic(items, te, Direction.UP);
        }
        return ok;
    }

    // Return what could not be inserted
    private ItemStack insertItem(ItemStack s) {
        s = InventoryHelper.insertItem(world, getPos(), Direction.UP, s);
        if (!s.isEmpty()) {
            s = InventoryHelper.insertItem(world, getPos(), Direction.DOWN, s);
        }
        return s;
    }

    private static class TakeableItem {
        private final IItemHandler itemHandler;
        private final IInventory inventory;
        private final int slot;
        private final ItemStack peekStack;

        public static final TakeableItem EMPTY = new TakeableItem();
        private TakeableItem() {
            this.itemHandler = null;
            this.inventory = null;
            this.slot = -1;
            this.peekStack = ItemStack.EMPTY;
        }

        public TakeableItem(IItemHandler itemHandler, int slot) {
            Validate.inclusiveBetween(0, itemHandler.getSlots() - 1, slot);
            this.itemHandler = itemHandler;
            this.inventory = null;
            this.slot = slot;
            this.peekStack = itemHandler.extractItem(slot, 1, true);
        }

        public TakeableItem(IInventory inventory, int slot) {
            Validate.inclusiveBetween(0, inventory.getSizeInventory() - 1, slot);
            this.itemHandler = null;
            this.inventory = inventory;
            this.slot = slot;
            this.peekStack = inventory.getStackInSlot(slot).copy();
            this.peekStack.setCount(1);
        }

        public ItemStack peek() {
            return peekStack.copy();
        }

        public void take() {
            if(itemHandler != null) {
                itemHandler.extractItem(slot, 1, false);
            } else if(slot != -1) {
                inventory.decrStackSize(slot, 1);
            }
        }

        public ItemStack takeAndReplace(ItemStack replacement) {
            if(itemHandler != null) {
                itemHandler.extractItem(slot, 1, false);
                return itemHandler.insertItem(slot, replacement, false);
            } else if(slot != -1) {
                inventory.decrStackSize(slot, 1);
                if(inventory.isItemValidForSlot(slot, replacement) && inventory.getStackInSlot(slot).isEmpty()) {
                    inventory.setInventorySlotContents(slot, replacement);
                    return ItemStack.EMPTY;
                }
            }
            return replacement;
        }
    }

    /**
     * Create a way to let you consume a block out of an inventory. Returns a blockstate
     * from that inventory or else null if nothing could be found.
     * If the given blockstate parameter is null then a random block will be
     * returned. Otherwise the returned block has to match.
     */
    private TakeableItem createTakeableItem(Direction direction, World srcWorld, BlockPos srcPos, BlockState state) {
        TileEntity te = world.getTileEntity(getPos().offset(direction));
        if (te != null) {
            return te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).map(h -> {
                return findBlockTakeableItem(h, srcWorld, srcPos, state);
            }).orElse(TakeableItem.EMPTY);
        }
        return TakeableItem.EMPTY;
    }

    private FluidStack consumeLiquid(World srcWorld, BlockPos srcPos) {
        FluidStack b = consumeLiquid(Direction.UP, srcWorld, srcPos);
        if (b == null) {
            b = consumeLiquid(Direction.DOWN, srcWorld, srcPos);
        }
        return b;
    }

    private FluidStack consumeLiquid(Direction direction, World srcWorld, BlockPos srcPos) {
        TileEntity te = world.getTileEntity(getPos().offset(direction));
        if (te != null) {
            LazyOptional<IFluidHandler> fluid = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
            if (!fluid.isPresent()) {
                fluid = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
            }
            if (fluid.isPresent()) {
                return fluid.map(h -> findAndConsumeLiquid(h, srcWorld, srcPos)).orElse(null);
            }
        }
        return null;
    }

    private FluidStack findAndConsumeLiquid(IFluidHandler tank, World srcWorld, BlockPos srcPos) {
        for (IFluidTankProperties properties : tank.getTankProperties()) {
            FluidStack contents = properties.getContents();
            if (contents != null) {
                if (contents.getFluid() != null) {
                    if (contents.amount >= 1000) {
                        FluidStack drained = tank.drain(new FluidStack(contents.getFluid(), 1000, contents.tag), true);
//                        System.out.println("drained = " + drained);
                        return drained;
                    }
                }
            }
        }
        return null;
    }

    private TakeableItem createTakeableItem(World srcWorld, BlockPos srcPos, BlockState state) {
        TakeableItem b = createTakeableItem(Direction.UP, srcWorld, srcPos, state);
        if (b.peek().isEmpty()) {
            b = createTakeableItem(Direction.DOWN, srcWorld, srcPos, state);
        }
        return b;
    }

    public static BuilderSetup.BlockInformation getBlockInformation(PlayerEntity fakePlayer, World world, BlockPos pos, Block block, TileEntity tileEntity) {
        BlockState state = world.getBlockState(pos);
        if (isEmpty(state, block)) {
            return BuilderSetup.BlockInformation.FREE;
        }

        if (!allowedToBreak(state, world, pos, fakePlayer)) {
            return BuilderSetup.BlockInformation.INVALID;
        }

        BuilderSetup.BlockInformation blockInformation = BuilderSetup.getBlockInformation(block);
        if (tileEntity != null) {
            switch (BuilderConfiguration.teMode.get()) {
                case MOVE_FORBIDDEN:
                    return BuilderSetup.BlockInformation.INVALID;
                case MOVE_WHITELIST:
                    if (blockInformation == null || blockInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                        return BuilderSetup.BlockInformation.INVALID;
                    }
                    break;
                case MOVE_BLACKLIST:
                    if (blockInformation != null && blockInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                        return BuilderSetup.BlockInformation.INVALID;
                    }
                    break;
                case MOVE_ALLOWED:
                    break;
            }
        }
        if (blockInformation != null) {
            return blockInformation;
        }
        return BuilderSetup.BlockInformation.OK;
    }

    private int isMovable(World world, BlockPos pos, Block block, TileEntity tileEntity) {
        return getBlockInformation(getHarvester(), world, pos, block, tileEntity).getBlockLevel();
    }

    public static boolean isEmptyOrReplacable(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        // @todo 1.14
//        if (block.isReplaceable(world, pos)) {
//            return true;
//        }
        return isEmpty(state, block);
    }

    // True if this block can just be overwritten (i.e. are or support block)
    public static boolean isEmpty(BlockState state, Block block) {
        if (block == null) {
            return true;
        }
        if (block.getMaterial(state) == Material.AIR) {
            return true;
        }
        if (block == BuilderSetup.supportBlock) {
            return true;
        }
        return false;
    }

    private void clearBlock(World world, BlockPos pos) {
        if (supportMode) {
            world.setBlockState(pos, BuilderSetup.supportBlock.getDefaultState(), 3);
        } else {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
        }
    }

    private int oppositeRotate() {
        switch (rotate) {
            case 1:
                return 3;
            case 3:
                return 1;
        }
        return rotate;
    }

    private void copyBlock(World srcWorld, BlockPos srcPos, World destWorld, BlockPos destPos) {
        energyHandler.ifPresent(h -> {
            long rf = h.getEnergy();
            int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
            if (rfNeeded > rf) {
                // Not enough energy.
                return;
            }

            if (isEmptyOrReplacable(destWorld, destPos)) {
                if (srcWorld.isAirBlock(srcPos)) {
                    return;
                }
                BlockState srcState = srcWorld.getBlockState(srcPos);
                TakeableItem takeableItem = createTakeableItem(srcWorld, srcPos, srcState);
                ItemStack consumedStack = takeableItem.peek();
                if (consumedStack.isEmpty()) {
                    return;
                }

                FakePlayer fakePlayer = getHarvester();
                BlockState newState = BlockTools.placeStackAt(fakePlayer, consumedStack, destWorld, destPos, srcState);
                destWorld.setBlockState(destPos, newState, 3);  // placeBlockAt can reset the orientation. Restore it here

                if (!ItemStack.areItemStacksEqual(consumedStack, takeableItem.peek())) { // Did we actually use up whatever we were holding?
                    if (!consumedStack.isEmpty()) { // Are we holding something else that we should put back?
                        consumedStack = takeableItem.takeAndReplace(consumedStack); // First try to put our new item where we got what we placed
                        if (!consumedStack.isEmpty()) { // If that didn't work, then try to put it anywhere it will fit
                            consumedStack = insertItem(consumedStack);
                            if (!consumedStack.isEmpty()) { // If that still didn't work, then just drop whatever we're holding
                                world.addEntity(new ItemEntity(world, getPos().getX(), getPos().getY(), getPos().getZ(), consumedStack));
                            }
                        }
                    } else {
                        takeableItem.take(); // If we aren't holding anything, then just consume what we placed
                    }
                }

                if (!silent) {
                    SoundTools.playSound(destWorld, newState.getBlock().getSoundType(newState, destWorld, destPos, fakePlayer).getPlaceSound(), destPos.getX(), destPos.getY(), destPos.getZ(), 1.0f, 1.0f);
                }

                h.consumeEnergy(rfNeeded);
            }
        });
    }

    private double getDimensionCostFactor(World world, World destWorld) {
        return destWorld.getDimension().getType().getId() == world.getDimension().getType().getId() ? 1.0 : BuilderConfiguration.dimensionCostFactor.get();
    }

    private boolean consumeEntityEnergy(int rfNeeded, int rfNeededPlayer, Entity entity) {
        return energyHandler.map(h -> {
            long rf = h.getEnergy();
            int rfn;
            if (entity instanceof PlayerEntity) {
                rfn = rfNeededPlayer;
            } else {
                rfn = rfNeeded;
            }
            if (rfn > rf) {
                // Not enough energy.
                return true;
            } else {
                h.consumeEnergy(rfn);
            }
            return false;
        }).orElse(false);
    }

    private void moveEntities(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerEntity.get() * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        int rfNeededPlayer = (int) (BuilderConfiguration.builderRfPerPlayer.get() * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);

        // Check for entities.
        List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(x - .1, y - .1, z - .1, x + 1.1, y + 1.1, z + 1.1));
        for (Entity entity : entities) {

            if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
                return;
            }

            double newX = destX + (entity.posX - x);
            double newY = destY + (entity.posY - y);
            double newZ = destZ + (entity.posZ - z);

            teleportEntity(world, destWorld, entity, newX, newY, newZ);
        }
    }

    private void swapEntities(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerEntity.get() * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        int rfNeededPlayer = (int) (BuilderConfiguration.builderRfPerPlayer.get() * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);

        // Check for entities.
        List<Entity> entitiesSrc = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1));
        List<Entity> entitiesDst = destWorld.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(destX, destY, destZ, destX + 1, destY + 1, destZ + 1));
        for (Entity entity : entitiesSrc) {
            if (isEntityInBlock(x, y, z, entity)) {
                if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
                    return;
                }

                double newX = destX + (entity.posX - x);
                double newY = destY + (entity.posY - y);
                double newZ = destZ + (entity.posZ - z);
                teleportEntity(world, destWorld, entity, newX, newY, newZ);
            }
        }
        for (Entity entity : entitiesDst) {
            if (isEntityInBlock(destX, destY, destZ, entity)) {
                if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) {
                    return;
                }

                double newX = x + (entity.posX - destX);
                double newY = y + (entity.posY - destY);
                double newZ = z + (entity.posZ - destZ);
                teleportEntity(destWorld, world, entity, newX, newY, newZ);
            }
        }
    }

    private void teleportEntity(World world, World destWorld, Entity entity, double newX, double newY, double newZ) {
        if (!TeleportationTools.allowTeleport(entity, world.getDimension().getType().getId(), entity.getPosition(), destWorld.getDimension().getType().getId(), new BlockPos(newX, newY, newZ))) {
            return;
        }
        mcjty.lib.varia.TeleportationTools.teleportEntity(entity, destWorld, newX, newY, newZ, null);
    }


    private boolean isEntityInBlock(int x, int y, int z, Entity entity) {
        if (entity.posX >= x && entity.posX < x + 1 && entity.posY >= y && entity.posY < y + 1 && entity.posZ >= z && entity.posZ < z + 1) {
            return true;
        }
        return false;
    }

    private void moveBlock(World srcWorld, BlockPos srcPos, World destWorld, BlockPos destPos, int rotMode) {
        BlockState oldDestState = destWorld.getBlockState(destPos);
        Block oldDestBlock = oldDestState.getBlock();
        if (isEmpty(oldDestState, oldDestBlock)) {
            BlockState srcState = srcWorld.getBlockState(srcPos);
            Block srcBlock = srcState.getBlock();
            if (isEmpty(srcState, srcBlock)) {
                return;
            }
            TileEntity srcTileEntity = srcWorld.getTileEntity(srcPos);
            BuilderSetup.BlockInformation srcInformation = getBlockInformation(getHarvester(), srcWorld, srcPos, srcBlock, srcTileEntity);
            if (srcInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                return;
            }

            if (!energyHandler.map(h -> {
                long rf = h.getEnergy();
                int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * srcInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
                if (rfNeeded > rf) {
                    // Not enough energy.
                    return false;
                } else {
                    h.consumeEnergy(rfNeeded);
                    return true;
                }
            }).orElse(false)) {
                return;
            }

            CompoundNBT tc = null;
            if (srcTileEntity != null) {
                tc = new CompoundNBT();
                srcTileEntity.write(tc);
                srcWorld.removeTileEntity(srcPos);
            }
            clearBlock(srcWorld, srcPos);

            destWorld.setBlockState(destPos, srcState, 3);
            if (srcTileEntity != null && tc != null) {
                setTileEntityNBT(destWorld, tc, destPos, srcState);
            }
            if (!silent) {
                SoundTools.playSound(srcWorld, srcBlock.getSoundType(srcState, srcWorld, srcPos, null).getBreakSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
                SoundTools.playSound(destWorld, srcBlock.getSoundType(srcState, destWorld, destPos, null).getPlaceSound(), destPos.getX(), destPos.getY(), destPos.getZ(), 1.0f, 1.0f);
            }
        }
    }

    private void setTileEntityNBT(World destWorld, CompoundNBT tc, BlockPos destpos, BlockState newDestState) {
        tc.putInt("x", destpos.getX());
        tc.putInt("y", destpos.getY());
        tc.putInt("z", destpos.getZ());
// @todo 1.14
        //        TileEntity tileEntity = TileEntity.create(destWorld, tc);
//        if (tileEntity != null) {
//            destWorld.getChunkFromBlockCoords(destpos).addTileEntity(tileEntity);
//            tileEntity.markDirty();
//            destWorld.notifyBlockUpdate(destpos, newDestState, newDestState, 3);
//        }
    }

    private void swapBlock(World srcWorld, BlockPos srcPos, World destWorld, BlockPos dstPos) {
        BlockState oldSrcState = srcWorld.getBlockState(srcPos);
        Block srcBlock = oldSrcState.getBlock();
        TileEntity srcTileEntity = srcWorld.getTileEntity(srcPos);

        BlockState oldDstState = destWorld.getBlockState(dstPos);
        Block dstBlock = oldDstState.getBlock();
        TileEntity dstTileEntity = destWorld.getTileEntity(dstPos);

        if (isEmpty(oldSrcState, srcBlock) && isEmpty(oldDstState, dstBlock)) {
            return;
        }

        BuilderSetup.BlockInformation srcInformation = getBlockInformation(getHarvester(), srcWorld, srcPos, srcBlock, srcTileEntity);
        if (srcInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
            return;
        }

        BuilderSetup.BlockInformation dstInformation = getBlockInformation(getHarvester(), destWorld, dstPos, dstBlock, dstTileEntity);
        if (dstInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
            return;
        }

        if (!energyHandler.map(h -> {
            long rf = h.getEnergy();
            int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * srcInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
            rfNeeded += (int) (BuilderConfiguration.builderRfPerOperation.get() * getDimensionCostFactor(srcWorld, destWorld) * dstInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
            if (rfNeeded > rf) {
                // Not enough energy.
                return false;
            } else {
                h.consumeEnergy(rfNeeded);
                return true;
            }
        }).orElse(false)) {
            return;
        }

        srcWorld.removeTileEntity(srcPos);
        srcWorld.setBlockState(srcPos, Blocks.AIR.getDefaultState());
        destWorld.removeTileEntity(dstPos);
        destWorld.setBlockState(dstPos, Blocks.AIR.getDefaultState());

        BlockState newDstState = oldSrcState;
        destWorld.setBlockState(dstPos, newDstState, 3);
//        destWorld.setBlockMetadataWithNotify(destX, destY, destZ, srcMeta, 3);
        if (srcTileEntity != null) {
            srcTileEntity.validate();
            destWorld.setTileEntity(dstPos, srcTileEntity);
            srcTileEntity.markDirty();
            destWorld.notifyBlockUpdate(dstPos, newDstState, newDstState, 3);
        }

        BlockState newSrcState = oldDstState;
        srcWorld.setBlockState(srcPos, newSrcState, 3);
//        world.setBlockMetadataWithNotify(x, y, z, dstMeta, 3);
        if (dstTileEntity != null) {
            dstTileEntity.validate();
            srcWorld.setTileEntity(srcPos, dstTileEntity);
            dstTileEntity.markDirty();
            srcWorld.notifyBlockUpdate(srcPos, newSrcState, newSrcState, 3);
        }

        if (!silent) {
            if (!isEmpty(oldSrcState, srcBlock)) {
                SoundTools.playSound(srcWorld, srcBlock.getSoundType(oldSrcState, srcWorld, srcPos, null).getBreakSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
                SoundTools.playSound(destWorld, srcBlock.getSoundType(oldSrcState, destWorld, dstPos, null).getPlaceSound(), dstPos.getX(), dstPos.getY(), dstPos.getZ(), 1.0f, 1.0f);
            }
            if (!isEmpty(oldDstState, dstBlock)) {
                SoundTools.playSound(destWorld, dstBlock.getSoundType(oldDstState, destWorld, dstPos, null).getBreakSound(), dstPos.getX(), dstPos.getY(), dstPos.getZ(), 1.0f, 1.0f);
                SoundTools.playSound(srcWorld, dstBlock.getSoundType(oldDstState, srcWorld, srcPos, null).getPlaceSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
            }
        }
    }

    private BlockPos sourceToDest(BlockPos source) {
        return rotate(source).add(projDx, projDy, projDz);
    }

    private BlockPos rotate(BlockPos c) {
        switch (rotate) {
            case 0:
                return c;
            case 1:
                return new BlockPos(-c.getZ(), c.getY(), c.getX());
            case 2:
                return new BlockPos(-c.getX(), c.getY(), -c.getZ());
            case 3:
                return new BlockPos(c.getZ(), c.getY(), -c.getX());
        }
        return c;
    }

    private void sourceToDest(BlockPos source, BlockPos.MutableBlockPos dest) {
        rotate(source, dest);
        dest.setPos(dest.getX() + projDx, dest.getY() + projDy, dest.getZ() + projDz);
    }


    private void rotate(BlockPos c, BlockPos.MutableBlockPos dest) {
        switch (rotate) {
            case 0:
                dest.setPos(c);
                break;
            case 1:
                dest.setPos(-c.getZ(), c.getY(), c.getX());
                break;
            case 2:
                dest.setPos(-c.getX(), c.getY(), -c.getZ());
                break;
            case 3:
                dest.setPos(c.getZ(), c.getY(), -c.getX());
                break;
        }
    }

    private void restartScan() {
        lastError = null;
        chunkUnload();
        if (loopMode || (isMachineEnabled() && scan == null)) {
            if (getCardType() == ShapeCardType.CARD_SPACE) {
                calculateBox();
                scan = minBox;
            } else if (getCardType() != ShapeCardType.CARD_UNKNOWN) {
                calculateBoxShaped();
                // We start at the top for a quarry or shape building
                scan = new BlockPos(minBox.getX(), maxBox.getY(), minBox.getZ());
            }
            cachedBlocks = null;
            cachedChunk = null;
            cachedVoidableBlocks = null;
        } else {
            scan = null;
        }
    }

    @Override
    public void remove() {
        super.remove();
        chunkUnload();
    }

    private void chunkUnload() {
        // @todo 1.14
//        if (forcedChunk != null && ticket != null) {
//            ForgeChunkManager.unforceChunk(ticket, forcedChunk);
//            forcedChunk = null;
//        }
    }

    private boolean chunkLoad(int x, int z) {
        int cx = x >> 4;
        int cz = z >> 4;

        if (WorldTools.chunkLoaded(world, new BlockPos(x, 0, z))) {
            return true;
        }

        if (BuilderConfiguration.quarryChunkloads.get()) {
            // @todo 1.14
//            if (ticket == null) {
//                ticket = ForgeChunkManager.requestTicket(RFTools.instance, world, ForgeChunkManager.Type.NORMAL);
//                if (ticket == null) {
//                    // Chunk is not loaded and we can't get a ticket.
//                    return false;
//                }
//            }

            ChunkPos pair = new ChunkPos(cx, cz);
            if (pair.equals(forcedChunk)) {
                return true;
            }
            // @todo 1.14
//            if (forcedChunk != null) {
//                ForgeChunkManager.unforceChunk(ticket, forcedChunk);
//            }
//            forcedChunk = pair;
//            ForgeChunkManager.forceChunk(ticket, forcedChunk);
            return true;
        }
        // Chunk is not loaded and we don't do chunk loading so we cannot proceed.
        return false;
    }


    public static void setScanLocationClient(BlockPos tePos, BlockPos scanPos) {
        scanLocClient.put(tePos, Pair.of(System.currentTimeMillis(), scanPos));
    }

    public static Map<BlockPos, Pair<Long, BlockPos>> getScanLocClient() {
        if (scanLocClient.isEmpty()) {
            return scanLocClient;
        }
        Map<BlockPos, Pair<Long, BlockPos>> scans = new HashMap<>();
        long time = System.currentTimeMillis();
        for (Map.Entry<BlockPos, Pair<Long, BlockPos>> entry : scanLocClient.entrySet()) {
            if (entry.getValue().getKey()+10000 > time) {
                scans.put(entry.getKey(), entry.getValue());
            }
        }
        scanLocClient = scans;
        return scanLocClient;
    }

    private void nextLocation() {
        if (scan != null) {
            int x = scan.getX();
            int y = scan.getY();
            int z = scan.getZ();

            if (getCardType() == ShapeCardType.CARD_SPACE) {
                nextLocationNormal(x, y, z);
            } else {
                nextLocationQuarry(x, y, z);
            }
        }
    }

    private void nextLocationQuarry(int x, int y, int z) {
        if (x >= maxBox.getX() || ((x + 1) % 16 == 0)) {
            if (z >= maxBox.getZ() || ((z + 1) % 16 == 0)) {
                if (y <= minBox.getY()) {
                    if (x < maxBox.getX()) {
                        x++;
                        z = (z >> 4) << 4;
                        y = maxBox.getY();
                        scan = new BlockPos(x, y, z);
                    } else if (z < maxBox.getZ()) {
                        x = minBox.getX();
                        z++;
                        y = maxBox.getY();
                        scan = new BlockPos(x, y, z);
                    } else {
                        restartScan();
                    }
                } else {
                    scan = new BlockPos((x >> 4) << 4, y - 1, (z >> 4) << 4);
                }
            } else {
                scan = new BlockPos((x >> 4) << 4, y, z + 1);
            }
        } else {
            scan = new BlockPos(x + 1, y, z);
        }
    }

    private void nextLocationNormal(int x, int y, int z) {
        if (x >= maxBox.getX()) {
            if (z >= maxBox.getZ()) {
                if (y >= maxBox.getY()) {
                    if (mode != MODE_SWAP || isShapeCard()) {
                        restartScan();
                    } else {
                        // We don't restart in swap mode.
                        scan = null;
                    }
                } else {
                    scan = new BlockPos(minBox.getX(), y + 1, minBox.getZ());
                }
            } else {
                scan = new BlockPos(minBox.getX(), y, z + 1);
            }
        } else {
            scan = new BlockPos(x + 1, y, z);
        }
    }

    private void refreshSettings() {
        clearSupportBlocks();
        cachedBlocks = null;
        cachedChunk = null;
        cachedVoidableBlocks = null;
        boxValid = false;
        scan = null;
        cardType = ShapeCardType.CARD_UNKNOWN;
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        if(tagCompound.contains("overflowItems")) {
            ListNBT overflowItemsNbt = tagCompound.getList("overflowItems", Constants.NBT.TAG_COMPOUND);
            overflowItems = new ArrayList<>(overflowItemsNbt.size());
            for(INBT overflowNbt : overflowItemsNbt) {
                overflowItems.add(ItemStack.read((CompoundNBT)overflowNbt));
            }
        }
        readRestorableFromNBT(tagCompound);
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        if(overflowItems != null) {
            ListNBT overflowItemsNbt = new ListNBT();
            for(ItemStack overflow : overflowItems) {
                overflowItemsNbt.add(overflow.write(new CompoundNBT()));
            }
            tagCompound.put("overflowItems", overflowItemsNbt);
        }
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {

        // Workaround to get the redstone mode for old builders to default to 'on'
        if (!tagCompound.contains("rsMode")) {
            rsMode = RedstoneMode.REDSTONE_ONREQUIRED;
        }


        readBufferFromNBT(tagCompound, inventoryHelper);
        if (tagCompound.contains("lastError")) {
            lastError = tagCompound.getString("lastError");
        } else {
            lastError = null;
        }
        mode = tagCompound.getInt("mode");
        anchor = tagCompound.getInt("anchor");
        rotate = tagCompound.getInt("rotate");
        silent = tagCompound.getBoolean("silent");
        supportMode = tagCompound.getBoolean("support");
        entityMode = tagCompound.getBoolean("entityMode");
        loopMode = tagCompound.getBoolean("loopMode");
        if (tagCompound.contains("waitMode")) {
            waitMode = tagCompound.getBoolean("waitMode");
        } else {
            waitMode = true;
        }
        hilightMode = tagCompound.getBoolean("hilightMode");
        scan = BlockPosTools.read(tagCompound, "scan");
        minBox = BlockPosTools.read(tagCompound, "minBox");
        maxBox = BlockPosTools.read(tagCompound, "maxBox");
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        writeBufferToNBT(tagCompound, inventoryHelper);
        if (lastError != null) {
            tagCompound.putString("lastError", lastError);
        }
        tagCompound.putInt("mode", mode);
        tagCompound.putInt("anchor", anchor);
        tagCompound.putInt("rotate", rotate);
        tagCompound.putBoolean("silent", silent);
        tagCompound.putBoolean("support", supportMode);
        tagCompound.putBoolean("entityMode", entityMode);
        tagCompound.putBoolean("loopMode", loopMode);
        tagCompound.putBoolean("waitMode", waitMode);
        tagCompound.putBoolean("hilightMode", hilightMode);
        BlockPosTools.write(tagCompound, "scan", scan);
        BlockPosTools.write(tagCompound, "minBox", minBox);
        BlockPosTools.write(tagCompound, "maxBox", maxBox);
    }

    // Request the current scan level.
    public void requestCurrentLevel() {
        requestDataFromServer(RFTools.MODID, CMD_GETLEVEL, TypedMap.EMPTY);
    }

    public static int getCurrentLevelClientSide() {
        return currentLevel;
    }

    public int getCurrentLevel() {
        return scan == null ? -1 : scan.getY();
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETROTATE.equals(command)) {
            setRotate(Integer.parseInt(params.get(ChoiceLabel.PARAM_CHOICE))/90);
            return true;
        } else if (CMD_SETANCHOR.equals(command)) {
            setAnchor(params.get(PARAM_ANCHOR_INDEX));
            return true;
        } else  if (CMD_SETMODE.equals(command)) {
            setMode(params.get(ChoiceLabel.PARAM_CHOICE_IDX));
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (PacketGetHudLog.CMD_GETHUDLOG.equals(command)) {
            return type.convert(getHudLog());
        }
        return rc;
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (PacketGetHudLog.CLIENTCMD_GETHUDLOG.equals(command)) {
            clientHudLog = Type.STRING.convert(list);
            return true;
        }
        return false;
    }


    @Override
    public TypedMap executeWithResult(String command, TypedMap args) {
        TypedMap rc = super.executeWithResult(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETLEVEL.equals(command)) {
            return TypedMap.builder().put(PARAM_LEVEL, scan == null ? -1 : scan.getY()).build();
        }
        return null;
    }

    @Override
    public boolean receiveDataFromServer(String command, @Nonnull TypedMap result) {
        boolean rc = super.receiveDataFromServer(command, result);
        if (rc) {
            return true;
        }
        if (CMD_GETLEVEL.equals(command)) {
            currentLevel = result.get(PARAM_LEVEL);
            return true;
        }
        return false;
    }

    @Override
    public void onReplaced(World world, BlockPos pos, BlockState state) {
        if (hasSupportMode()) {
            clearSupportBlocks();
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.add(1, 2, 1));
    }

//    @Optional.Method(modid = "theoneprobe")
//    @Override
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        int scan = getCurrentLevel();
//        probeInfo.text(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
//    }

    private static long lastTime = 0;

//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//        if (System.currentTimeMillis() - lastTime > 250) {
//            lastTime = System.currentTimeMillis();
//            requestCurrentLevel();
//        }
//        int scan = BuilderTileEntity.getCurrentLevelClientSide();
//        currenttip.add(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
//    }


    @Override
    public void rotateBlock(Rotation axis) {
        super.rotateBlock(axis);
        if (!world.isRemote) {
            if (hasSupportMode()) {
                clearSupportBlocks();
                resetBox();
            }
        }
    }

    // @todo 1.14
//    @Override
//    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, BlockState metadata, int fortune) {
//        super.getDrops(drops, world, pos, metadata, fortune);
//        List<ItemStack> overflowItems = getOverflowItems();
//        if(overflowItems != null) {
//            drops.addAll(overflowItems);
//        }
//    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(BuilderTileEntity.this, CONTAINER_FACTORY, 2) {
            @Override
            protected void onUpdate(int index) {
                super.onUpdate(index);
                ItemStack stack = getStackInSlot(index);
                if (index == SLOT_TAB && ((stack.isEmpty()
                        && !inventoryHelper.getStackInSlot(index).isEmpty())
                        || (!stack.isEmpty() && inventoryHelper.getStackInSlot(index).isEmpty()))) {
                    // Restart if we go from having a stack to not having stack or the other way around.
                    refreshSettings();
                }
                if (index == SLOT_FILTER) {
                    filterCache = null;
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == BuilderSetup.spaceChamberCardItem || stack.getItem() == BuilderSetup.shapeCardItem;
            }

            @Override
            public boolean isItemInsertable(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == BuilderSetup.spaceChamberCardItem || stack.getItem() == BuilderSetup.shapeCardItem;
            }
        };
    }
}
