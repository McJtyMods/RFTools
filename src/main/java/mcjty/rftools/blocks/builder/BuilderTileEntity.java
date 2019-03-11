package mcjty.rftools.blocks.builder;

import com.mojang.authlib.GameProfile;
import mcjty.lib.bindings.DefaultAction;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IAction;
import mcjty.lib.bindings.IValue;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
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
import mcjty.rftools.setup.CommonSetup;
import mcjty.rftools.shapes.Shape;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class BuilderTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable,
        IHudSupport {

    public static final String CMD_SETMODE = "builder.setMode";
    public static final String CMD_SETROTATE = "builder.setRotate";

    public static final String CMD_SETANCHOR = "builder.setAnchor";
    public static final Key<Integer> PARAM_ANCHOR_INDEX = new Key<>("anchorIndex", Type.INTEGER);

    public static final String CMD_GETLEVEL = "getLevel";
    public static final Key<Integer> PARAM_LEVEL = new Key<>("level", Type.INTEGER);

    public static final int SLOT_TAB = 0;
    public static final int SLOT_FILTER = 1;
    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(RFTools.MODID, "gui/builder.gui"));

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

    private int collectCounter = BuilderConfiguration.collectTimer;
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
    private ForgeChunkManager.Ticket ticket = null;
    // The currently forced chunk.
    private ChunkPos forcedChunk = null;

    // Cached set of blocks that we need to build in shaped mode
    private Map<BlockPos, IBlockState> cachedBlocks = null;
    private ChunkPos cachedChunk = null;       // For which chunk are the cachedBlocks valid

    // Cached set of blocks that we want to void with the quarry.
    private Set<Block> cachedVoidableBlocks = null;

    // Drops from a block that we broke but couldn't fit in an inventory
    private List<ItemStack> overflowItems = null;

    private FakePlayer harvester = null;

    public BuilderTileEntity() {
        super(BuilderConfiguration.BUILDER_MAXENERGY, BuilderConfiguration.BUILDER_RECEIVEPERTICK);
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

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    private FakePlayer getHarvester() {
        if (harvester == null) {
            harvester = FakePlayerFactory.get((WorldServer) world,  new GameProfile(UUID.nameUUIDFromBytes("rftools_builder".getBytes()), "rftools_builder"));
        }
        harvester.setWorld(world);
        harvester.setPosition(pos.getX(), pos.getY(), pos.getZ());
        return harvester;
    }

    @Override
    public EnumFacing getBlockOrientation() {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == BuilderSetup.builderBlock) {
            return OrientationTools.getOrientationHoriz(state);
        } else {
            return null;
        }
    }

    @Override
    public boolean isBlockAboveAir() {
        return getWorld().isAirBlock(pos.up());
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

    private NBTTagCompound hasCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(SLOT_TAB);
        if (itemStack.isEmpty()) {
            return null;
        }

        return itemStack.getTagCompound();
    }

    private void makeSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);
        Shape shape = ShapeCardItem.getShape(shapeCard);
        Map<BlockPos, IBlockState> blocks = new HashMap<>();
        ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), getWorld(), getPos(), dimension, offset, blocks, BuilderConfiguration.maxBuilderDimension * 256 * BuilderConfiguration.maxBuilderDimension, false, false, null);
        for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
            BlockPos p = entry.getKey();
            if (getWorld().isAirBlock(p)) {
                getWorld().setBlockState(p, BuilderSetup.supportBlock.getDefaultState().withProperty(SupportBlock.STATUS, SupportBlock.STATUS_OK), 3);
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
                        IBlockState srcState = world.getBlockState(src);
                        Block srcBlock = srcState.getBlock();
                        IBlockState dstState = world.getBlockState(dest);
                        Block dstBlock = dstState.getBlock();
                        int error = SupportBlock.STATUS_OK;
                        if (mode != MODE_COPY) {
                            TileEntity srcTileEntity = world.getTileEntity(src);
                            TileEntity dstTileEntity = getWorld().getTileEntity(dest);

                            int error1 = isMovable(world, src, srcBlock, srcTileEntity);
                            int error2 = isMovable(getWorld(), dest, dstBlock, dstTileEntity);
                            error = Math.max(error1, error2);
                        }
                        if (isEmpty(srcState, srcBlock) && !isEmpty(dstState, dstBlock)) {
                            getWorld().setBlockState(src, BuilderSetup.supportBlock.getDefaultState().withProperty(SupportBlock.STATUS, error), 3);
                        }
                        if (isEmpty(dstState, dstBlock) && !isEmpty(srcState, srcBlock)) {
                            getWorld().setBlockState(dest, BuilderSetup.supportBlock.getDefaultState().withProperty(SupportBlock.STATUS, error), 3);
                        }
                    }
                }
            }
        }
    }

    private void clearSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);
        Shape shape = ShapeCardItem.getShape(shapeCard);
        Map<BlockPos, IBlockState> blocks = new HashMap<>();
        ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), getWorld(), getPos(), dimension, offset, blocks, BuilderConfiguration.maxSpaceChamberDimension * BuilderConfiguration.maxSpaceChamberDimension * BuilderConfiguration.maxSpaceChamberDimension, false, false, null);
        for (Map.Entry<BlockPos, IBlockState> entry : blocks.entrySet()) {
            BlockPos block = entry.getKey();
            if (getWorld().getBlockState(block).getBlock() == BuilderSetup.supportBlock) {
                getWorld().setBlockToAir(block);
            }
        }
    }

    public void clearSupportBlocks() {
        if (getWorld().isRemote) {
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
            World world = DimensionManager.getWorld(dimension);

            BlockPos.MutableBlockPos src = new BlockPos.MutableBlockPos();
            BlockPos.MutableBlockPos dest = new BlockPos.MutableBlockPos();
            for (int x = minBox.getX(); x <= maxBox.getX(); x++) {
                for (int y = minBox.getY(); y <= maxBox.getY(); y++) {
                    for (int z = minBox.getZ(); z <= maxBox.getZ(); z++) {
                        src.setPos(x, y, z);
                        if (world != null) {
                            Block srcBlock = world.getBlockState(src).getBlock();
                            if (srcBlock == BuilderSetup.supportBlock) {
                                world.setBlockToAir(src);
                            }
                        }
                        sourceToDest(src, dest);
                        Block dstBlock = getWorld().getBlockState(dest).getBlock();
                        if (dstBlock == BuilderSetup.supportBlock) {
                            getWorld().setBlockToAir(dest);
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

    public boolean suspend(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {
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
        IBlockState state = getWorld().getBlockState(getPos());
        EnumFacing direction = state.getValue(BaseBlock.FACING_HORIZ);
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

        IBlockState state = getWorld().getBlockState(getPos());
        EnumFacing direction = state.getValue(BaseBlock.FACING_HORIZ);
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        int spanX = maxCorner.getX() - minCorner.getX();
        int spanY = maxCorner.getY() - minCorner.getY();
        int spanZ = maxCorner.getZ() - minCorner.getZ();
        switch (direction) {
            case SOUTH:
                projDx = xCoord + EnumFacing.NORTH.getDirectionVec().getX() - minCorner.getX() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + EnumFacing.NORTH.getDirectionVec().getZ() - minCorner.getZ() - spanZ;
                break;
            case NORTH:
                projDx = xCoord + EnumFacing.SOUTH.getDirectionVec().getX() - minCorner.getX() - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + EnumFacing.SOUTH.getDirectionVec().getZ() - minCorner.getZ();
                break;
            case WEST:
                projDx = xCoord + EnumFacing.EAST.getDirectionVec().getX() - minCorner.getX();
                projDz = zCoord + EnumFacing.EAST.getDirectionVec().getZ() - minCorner.getZ() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case EAST:
                projDx = xCoord + EnumFacing.WEST.getDirectionVec().getX() - minCorner.getX() - spanX;
                projDz = zCoord + EnumFacing.WEST.getDirectionVec().getZ() - minCorner.getZ() - spanZ + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case DOWN:
            case UP:
            default:
                break;
        }
        projDy = yCoord - minCorner.getY() - ((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY : 0);
    }

    private void calculateBox(NBTTagCompound cardCompound) {
        int channel = cardCompound.getInteger("channel");

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(getWorld());
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
        for (int i = 0; i < BuilderConfiguration.quarryBaseSpeed + (factor * BuilderConfiguration.quarryInfusionSpeedFactor); i++) {
            if (scan != null) {
                handleBlockShaped();
            }
        }
    }


    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
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
        World world = DimensionManager.getWorld(dimension);
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
            for (EntityPlayerMP player : getWorld().getMinecraftServer().getPlayerList().getPlayers()) {
                if (player.dimension == getWorld().provider.getDimension()) {
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
        collectCounter = BuilderConfiguration.collectTimer;
        if (!loopMode) {
            scan = null;
        }

        long rf = getStoredPower();
        float area = (maxBox.getX() - minBox.getX() + 1) * (maxBox.getY() - minBox.getY() + 1) * (maxBox.getZ() - minBox.getZ() + 1);
        float infusedFactor = (4.0f - getInfusedFactor()) / 4.0f;
        int rfNeeded = (int) (BuilderConfiguration.collectRFPerTickPerArea * area * infusedFactor) * BuilderConfiguration.collectTimer;
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }
        consumeEnergy(rfNeeded);

        AxisAlignedBB bb = new AxisAlignedBB(minBox.getX() - .8, minBox.getY() - .8, minBox.getZ() - .8, maxBox.getX() + .8, maxBox.getY() + .8, maxBox.getZ() + .8);
        List<Entity> items = world.getEntitiesWithinAABB(Entity.class, bb);
        for (Entity entity : items) {
            if (entity instanceof EntityItem) {
                if (collectItem(world, infusedFactor, (EntityItem) entity)) {
                    return;
                }
            } else if (entity instanceof EntityXPOrb) {
                if (collectXP(world, infusedFactor, (EntityXPOrb) entity)) {
                    return;
                }
            }
        }
    }

    private boolean collectXP(World world, float infusedFactor, EntityXPOrb orb) {
        long rf;
        int rfNeeded;

        int xp = orb.getXpValue();

        rf = getStoredPower();
        rfNeeded = (int) (BuilderConfiguration.collectRFPerXP * infusedFactor * xp);
        if (rfNeeded > rf) {
            // Not enough energy.
            return true;
        }

        collectXP += xp;

        int bottles = collectXP / 7;
        if (bottles > 0) {
            if (insertItem(new ItemStack(Items.EXPERIENCE_BOTTLE, bottles)).isEmpty()) {
                collectXP = collectXP % 7;
                world.removeEntity(orb);
                consumeEnergy(rfNeeded);
            } else {
                collectXP = 0;
            }
        }

        return false;
    }

    private boolean collectItem(World world, float infusedFactor, EntityItem item) {
        long rf;
        int rfNeeded;

        ItemStack stack = item.getItem();

        rf = getStoredPower();
        rfNeeded = (int) (BuilderConfiguration.collectRFPerItem * infusedFactor) * stack.getCount();
        if (rfNeeded > rf) {
            // Not enough energy.
            return true;
        }
        consumeEnergy(rfNeeded);

        world.removeEntity(item);
        stack = insertItem(stack);
        if (!stack.isEmpty()) {
            BlockPos position = item.getPosition();
            EntityItem entityItem = new EntityItem(getWorld(), position.getX(), position.getY(), position.getZ(), stack);
            getWorld().spawnEntity(entityItem);
        }
        return false;
    }

    private void calculateBoxShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
        if (shapeCard.isEmpty()) {
            return;
        }
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);

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
        cardType = ShapeCardType.fromDamage(shapeCard.getItemDamage());

        cachedBlocks = null;
        cachedChunk = null;
        cachedVoidableBlocks = null;
        minBox = minCorner;
        maxBox = maxCorner;
        restartScan();
    }

    private SpaceChamberRepository.SpaceChamberChannel calculateBox() {
        NBTTagCompound tc = hasCard();
        if (tc == null) {
            return null;
        }

        int channel = tc.getInteger("channel");
        if (channel == -1) {
            return null;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(getWorld());
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

    private Map<BlockPos, IBlockState> getCachedBlocks(ChunkPos chunk) {
        if ((chunk != null && !chunk.equals(cachedChunk)) || (chunk == null && cachedChunk != null)) {
            cachedBlocks = null;
        }

        if (cachedBlocks == null) {
            cachedBlocks = new HashMap<>();
            ItemStack shapeCard = inventoryHelper.getStackInSlot(SLOT_TAB);
            Shape shape = ShapeCardItem.getShape(shapeCard);
            boolean solid = ShapeCardItem.isSolid(shapeCard);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);
            boolean forquarry = !ShapeCardItem.isNormalShapeCard(shapeCard);
            ShapeCardItem.composeFormula(shapeCard, shape.getFormulaFactory().get(), getWorld(), getPos(), dimension, offset, cachedBlocks, BuilderConfiguration.maxSpaceChamberDimension * BuilderConfiguration.maxSpaceChamberDimension * BuilderConfiguration.maxSpaceChamberDimension, solid, forquarry, chunk);
            cachedChunk = chunk;
        }
        return cachedBlocks;
    }

    private void handleBlockShaped() {
        for (int i = 0; i < 100; i++) {
            if (scan == null) {
                return;
            }
            Map<BlockPos, IBlockState> blocks = getCachedBlocks(new ChunkPos(scan.getX() >> 4, scan.getZ() >> 4));
            if (blocks.containsKey(scan)) {
                IBlockState state = blocks.get(scan);
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
                cardType = ShapeCardType.fromDamage(card.getItemDamage());
            }
        }
        return cardType;
    }

    // Return true if we have to wait at this spot.
    private boolean handleSingleBlock(IBlockState pickState) {
        BlockPos srcPos = scan;
        int sx = scan.getX();
        int sy = scan.getY();
        int sz = scan.getZ();
        if (!chunkLoad(sx, sz)) {
            // The chunk is not available and we could not chunkload it. We have to wait.
            return suspend("Chunk not available!");
        }

        int rfNeeded = getCardType().getRfNeeded();

        IBlockState state = null;
        if (getCardType() != ShapeCardType.CARD_SHAPE && getCardType() != ShapeCardType.CARD_PUMP_LIQUID) {
            state = getWorld().getBlockState(srcPos);
            Block block = state.getBlock();
            if (!isEmpty(state, block)) {
                float hardness;
                if (isFluidBlock(block)) {
                    hardness = 1.0f;
                } else {
                    if (getCachedVoidableBlocks().contains(block)) {
                        rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.voidShapeCardFactor);
                    }
                    hardness = block.getBlockHardness(state, getWorld(), srcPos);
                }
                rfNeeded *= (int) ((hardness + 1) * 2);
            }
        }

        rfNeeded = (int) (rfNeeded * (3.0f - getInfusedFactor()) / 3.0f);

        if (rfNeeded > getStoredPower()) {
            // Not enough energy.
            return suspend("Not enough power!");
        }

        return getCardType().handleSingleBlock(this, rfNeeded, srcPos, state, pickState);
    }

    public boolean buildBlock(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {
        if (isEmptyOrReplacable(getWorld(), srcPos)) {
            TakeableItem item = createTakeableItem(getWorld(), srcPos, pickState);
            ItemStack stack = item.peek();
            if (stack.isEmpty()) {
                return waitOrSkip("Cannot find block!\nor missing inventory\non top or below");    // We could not find a block. Wait
            }

            FakePlayer fakePlayer = getHarvester();
            IBlockState newState = BlockTools.placeStackAt(fakePlayer, stack, getWorld(), srcPos, pickState);
            if (!ItemStack.areItemStacksEqual(stack, item.peek())) { // Did we actually use up whatever we were holding?
                if(!stack.isEmpty()) { // Are we holding something else that we should put back?
                    stack = item.takeAndReplace(stack); // First try to put our new item where we got what we placed
                    if(!stack.isEmpty()) { // If that didn't work, then try to put it anywhere it will fit
                        stack = insertItem(stack);
                        if(!stack.isEmpty()) { // If that still didn't work, then just drop whatever we're holding
                            getWorld().spawnEntity(new EntityItem(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), stack));
                        }
                    }
                } else {
                    item.take(); // If we aren't holding anything, then just consume what we placed
                }
            }

            if (!silent) {
                SoundTools.playSound(getWorld(), newState.getBlock().getSoundType(newState, getWorld(), srcPos, fakePlayer).getPlaceSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
            }

            consumeEnergy(rfNeeded);
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

    private void clearOrDirtBlock(int rfNeeded, BlockPos spos, IBlockState srcState, boolean clear) {
        if (srcState.getBlock() instanceof BlockShulkerBox) {
            TileEntity te = world.getTileEntity(spos);
            if(te instanceof TileEntityShulkerBox) {
                ((TileEntityShulkerBox)te).clear(); // We already collected a drop before we called this. Clear to make sure setBlockState doesn't spawn another.
            }
        }

        if (clear) {
            getWorld().setBlockToAir(spos);
        } else {
            getWorld().setBlockState(spos, getReplacementBlock(), 2);       // No block update!
        }
        consumeEnergy(rfNeeded);
        if (!silent) {
            SoundTools.playSound(getWorld(), srcState.getBlock().getSoundType(srcState, getWorld(), spos, null).getBreakSound(), spos.getX(), spos.getY(), spos.getZ(), 1.0f, 1.0f);
        }
    }

    private IBlockState getReplacementBlock() {
        return BuilderConfiguration.getQuarryReplace();
    }

    public boolean silkQuarryBlock(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {
        return commonQuarryBlock(true, rfNeeded, srcPos, srcState);
    }

    private void getFilterCache() {
        if (filterCache == null) {
            filterCache = StorageFilterItem.getCache(inventoryHelper.getStackInSlot(SLOT_FILTER));
        }
    }

    public static boolean allowedToBreak(IBlockState state, World world, BlockPos pos, EntityPlayer entityPlayer) {
        if (!state.getBlock().canEntityDestroy(state, world, pos, entityPlayer)) {
            return false;
        }
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, entityPlayer);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }

    public boolean quarryBlock(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {
        return commonQuarryBlock(false, rfNeeded, srcPos, srcState);
    }

    private boolean commonQuarryBlock(boolean silk, int rfNeeded, BlockPos srcPos, IBlockState srcState) {
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
        if (block.getBlockHardness(srcState, getWorld(), srcPos) >= 0) {
            boolean clear = getCardType().isClearing();
            if ((!clear) && srcState == getReplacementBlock()) {
                // We can skip dirt if we are not clearing.
                return skip();
            }
            if ((!BuilderConfiguration.quarryTileEntities) && getWorld().getTileEntity(srcPos) != null) {
                // Skip tile entities
                return skip();
            }

            FakePlayer fakePlayer = getHarvester();
            if (allowedToBreak(srcState, getWorld(), srcPos, fakePlayer)) {
                ItemStack filter = getStackInSlot(SLOT_FILTER);
                if (!filter.isEmpty()) {
                    getFilterCache();
                    if (filterCache != null) {
                        boolean match = filterCache.match(block.getItem(getWorld(), srcPos, srcState));
                        if (!match) {
                            consumeEnergy(Math.min(rfNeeded, BuilderConfiguration.builderRfPerSkipped));
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
                    if(silk && block.canSilkHarvest(getWorld(), srcPos, srcState, fakePlayer)) {
                        ItemStack drop;
                        try {
                            drop = (ItemStack) CommonSetup.Block_getSilkTouch.invoke(block, srcState);
                        } catch (IllegalAccessException|InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                        drops = new ArrayList<>();
                        if (!drop.isEmpty()) {
                            drops.add(drop);
                        }
                        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, getWorld(), srcPos, srcState, 0, 1.0f, true, fakePlayer);
                    } else {
                        int fortune = getCardType().isFortune() ? 3 : 0;
                        if (block instanceof BlockShulkerBox) {
                            // Shulker boxes drop in setBlockState, rather than anywhere sensible. Work around this.
                            drops = new ArrayList<>();
                            TileEntity te = getWorld().getTileEntity(srcPos);
                            if (te instanceof TileEntityShulkerBox) {
                                TileEntityShulkerBox teShulkerBox = (TileEntityShulkerBox)te;
                                ItemStack stack = new ItemStack(Item.getItemFromBlock(block));
                                teShulkerBox.saveToNbt(stack.getOrCreateSubCompound("BlockEntityTag"));
                                if (teShulkerBox.hasCustomName()) {
                                    stack.setStackDisplayName(teShulkerBox.getName());
                                }
                                drops.add(stack);
                            }
                        } else {
                            drops = block.getDrops(getWorld(), srcPos, srcState, fortune);
                        }
                        net.minecraftforge.event.ForgeEventFactory.fireBlockHarvesting(drops, getWorld(), srcPos, srcState, fortune, 1.0f, false, fakePlayer);
                    }
                    if (checkValidItems(block, drops) && !insertItems(drops)) {
                        overflowItems = drops;
                        clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
                        return waitOrSkip("Not enough room!\nor no usable storage\non top or below!");    // Not enough room. Wait
                    }
                }
                clearOrDirtBlock(rfNeeded, srcPos, srcState, clear);
            }
        }
        return silk ? skip() : false;
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof BlockLiquid || block instanceof BlockFluidBase;
    }

    private static int getFluidLevel(IBlockState srcState) {
        if (srcState.getBlock() instanceof BlockLiquid) {
            return srcState.getValue(BlockLiquid.LEVEL);
        }
        if (srcState.getBlock() instanceof BlockFluidBase) {
            return srcState.getValue(BlockFluidBase.LEVEL);
        }
        return -1;
    }

    public boolean placeLiquidBlock(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {

        if (isEmptyOrReplacable(getWorld(), srcPos)) {
            FluidStack stack = consumeLiquid(getWorld(), srcPos);
            if (stack == null) {
                return waitOrSkip("Cannot find liquid!\nor no usable tank\nabove or below");    // We could not find a block. Wait
            }

            Fluid fluid = stack.getFluid();
            if (fluid.doesVaporize(stack) && getWorld().provider.doesWaterVaporize()) {
                fluid.vaporize(null, getWorld(), srcPos, stack);
            } else {
                // We assume here the liquid is placable.
                Block block = fluid.getBlock();
                FakePlayer fakePlayer = getHarvester();
                getWorld().setBlockState(srcPos, block.getDefaultState(), 11);

                if (!silent) {
                    SoundTools.playSound(getWorld(), block.getSoundType(block.getDefaultState(), getWorld(), srcPos, fakePlayer).getPlaceSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
                }
            }

            consumeEnergy(rfNeeded);
        }
        return skip();
    }

    public boolean pumpBlock(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {
        Block block = srcState.getBlock();
        Fluid fluid = FluidRegistry.lookupFluidForBlock(block);
        if (fluid == null) {
            return skip();
        }
        if (!isFluidBlock(block)) {
            return skip();
        }

        if (getFluidLevel(srcState) != 0) {
            return skip();
        }


        if (block.getBlockHardness(srcState, getWorld(), srcPos) >= 0) {
            FakePlayer fakePlayer = getHarvester();
            if (allowedToBreak(srcState, getWorld(), srcPos, fakePlayer)) {
                if (checkAndInsertFluids(fluid)) {
                    consumeEnergy(rfNeeded);
                    boolean clear = getCardType().isClearing();
                    if (clear) {
                        getWorld().setBlockToAir(srcPos);
                    } else {
                        getWorld().setBlockState(srcPos, getReplacementBlock(), 2);       // No block update!
                    }
                    if (!silent) {
                        SoundTools.playSound(getWorld(), block.getSoundType(srcState, getWorld(), srcPos, fakePlayer).getBreakSound(), srcPos.getX(), srcPos.getY(), srcPos.getZ(), 1.0f, 1.0f);
                    }
                    return skip();
                }
                return waitOrSkip("No room for liquid\nor no usable tank\nabove or below!");    // No room in tanks or not a valid tank: wait
            }
        }
        return skip();
    }

    public boolean voidBlock(int rfNeeded, BlockPos srcPos, IBlockState srcState, IBlockState pickState) {
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
        if (allowedToBreak(srcState, getWorld(), srcPos, fakePlayer)) {
            if (block.getBlockHardness(srcState, getWorld(), srcPos) >= 0) {
                ItemStack filter = getStackInSlot(SLOT_FILTER);
                if (!filter.isEmpty()) {
                    getFilterCache();
                    if (filterCache != null) {
                        boolean match = filterCache.match(block.getItem(getWorld(), srcPos, srcState));
                        if (!match) {
                            consumeEnergy(Math.min(rfNeeded, BuilderConfiguration.builderRfPerSkipped));
                            return skip();   // Skip this
                        }
                    }
                }

                if (!silent) {
                    SoundTools.playSound(getWorld(), block.getSoundType(srcState, getWorld(), srcPos, fakePlayer).getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
                }
                getWorld().setBlockToAir(srcPos);
                consumeEnergy(rfNeeded);
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
                copyBlock(world, srcPos, getWorld(), destPos);
                break;
            case MODE_MOVE:
                if (entityMode) {
                    moveEntities(world, x, y, z, getWorld(), destX, destY, destZ);
                }
                moveBlock(world, srcPos, getWorld(), destPos, rotate);
                break;
            case MODE_BACK:
                if (entityMode) {
                    moveEntities(getWorld(), destX, destY, destZ, world, x, y, z);
                }
                moveBlock(getWorld(), destPos, world, srcPos, oppositeRotate());
                break;
            case MODE_SWAP:
                if (entityMode) {
                    swapEntities(world, x, y, z, getWorld(), destX, destY, destZ);
                }
                swapBlock(world, srcPos, getWorld(), destPos);
                break;
        }

        nextLocation();
    }

    private static Random random = new Random();

    // Also works if block is null and just picks the first available block.
    private TakeableItem findBlockTakeableItem(IItemHandler inventory, World srcWorld, BlockPos srcPos, IBlockState state) {
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
        return item instanceof ItemBlock || item instanceof ItemSkull || item instanceof ItemBlockSpecial
                || item instanceof IPlantable || item instanceof ItemRedstone;
    }

    // Also works if block is null and just picks the first available block.
    private TakeableItem findBlockTakeableItem(IInventory inventory, World srcWorld, BlockPos srcPos, IBlockState state) {
        if (state == null) {
            // We are not looking for a specific block. Pick a random one out of the chest.
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
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
            if(isPlacable(srcItem)) {
                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);
                    if (!stack.isEmpty() && stack.isItemEqual(srcItem)) {
                        return new TakeableItem(inventory, i);
                    }
                }
            }
        }
        return TakeableItem.EMPTY;
    }

    // To protect against mods doing bad things we have to check
    // the items that we try to insert.
    private boolean checkValidItems(Block block, List<ItemStack> items) {
        for (ItemStack stack : items) {
            if ((!stack.isEmpty()) && stack.getItem() == null) {
                Logging.logError("Builder tried to quarry " + block.getRegistryName().toString() + " and it returned null item!");
                Broadcaster.broadcast(getWorld(), pos.getX(), pos.getY(), pos.getZ(), "Builder tried to quarry "
                                + block.getRegistryName().toString() + " and it returned null item!\nPlease report to mod author!",
                        10);
                return false; // We don't wait for this. Just skip the item
            }
        }
        return true;
    }

    private boolean checkAndInsertFluids(Fluid fluid) {
        if (checkFluidTank(fluid, getPos().up(), EnumFacing.DOWN)) {
            return true;
        }
        if (checkFluidTank(fluid, getPos().down(), EnumFacing.UP)) {
            return true;
        }
        return false;
    }

    private boolean checkFluidTank(Fluid fluid, BlockPos up, EnumFacing side) {
        TileEntity te = getWorld().getTileEntity(up);
        if (te != null && te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side)) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            FluidStack fluidStack = new FluidStack(fluid, 1000);
            int amount = handler.fill(fluidStack, false);
            if (amount == 1000) {
                handler.fill(fluidStack, true);
                return true;
            }
        }
        return false;
    }

    private boolean insertItems(List<ItemStack> items) {
        TileEntity te = getWorld().getTileEntity(getPos().up());
        boolean ok = InventoryHelper.insertItemsAtomic(items, te, EnumFacing.DOWN);
        if (!ok) {
            te = getWorld().getTileEntity(getPos().down());
            ok = InventoryHelper.insertItemsAtomic(items, te, EnumFacing.UP);
        }
        return ok;
    }

    // Return what could not be inserted
    private ItemStack insertItem(ItemStack s) {
        s = InventoryHelper.insertItem(getWorld(), getPos(), EnumFacing.UP, s);
        if (!s.isEmpty()) {
            s = InventoryHelper.insertItem(getWorld(), getPos(), EnumFacing.DOWN, s);
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
    private TakeableItem createTakeableItem(EnumFacing direction, World srcWorld, BlockPos srcPos, IBlockState state) {
        TileEntity te = getWorld().getTileEntity(getPos().offset(direction));
        if (te != null) {
            if (te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite())) {
                IItemHandler capability = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite());
                return findBlockTakeableItem(capability, srcWorld, srcPos, state);
            } else if (te instanceof IInventory) {
                return findBlockTakeableItem((IInventory) te, srcWorld, srcPos, state);
            }
        }
        return TakeableItem.EMPTY;
    }

    private FluidStack consumeLiquid(World srcWorld, BlockPos srcPos) {
        FluidStack b = consumeLiquid(EnumFacing.UP, srcWorld, srcPos);
        if (b == null) {
            b = consumeLiquid(EnumFacing.DOWN, srcWorld, srcPos);
        }
        return b;
    }

    private FluidStack consumeLiquid(EnumFacing direction, World srcWorld, BlockPos srcPos) {
        TileEntity te = getWorld().getTileEntity(getPos().offset(direction));
        if (te != null) {
            if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite())) {
                IFluidHandler capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, direction.getOpposite());
                return findAndConsumeLiquid(capability, srcWorld, srcPos);
            }
            if (te.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null)) {
                IFluidHandler capability = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
                return findAndConsumeLiquid(capability, srcWorld, srcPos);
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

    private TakeableItem createTakeableItem(World srcWorld, BlockPos srcPos, IBlockState state) {
        TakeableItem b = createTakeableItem(EnumFacing.UP, srcWorld, srcPos, state);
        if (b.peek().isEmpty()) {
            b = createTakeableItem(EnumFacing.DOWN, srcWorld, srcPos, state);
        }
        return b;
    }

    public static BuilderSetup.BlockInformation getBlockInformation(EntityPlayer fakePlayer, World world, BlockPos pos, Block block, TileEntity tileEntity) {
        IBlockState state = world.getBlockState(pos);
        if (isEmpty(state, block)) {
            return BuilderSetup.BlockInformation.FREE;
        }

        if (!allowedToBreak(state, world, pos, fakePlayer)) {
            return BuilderSetup.BlockInformation.INVALID;
        }

        BuilderSetup.BlockInformation blockInformation = BuilderSetup.getBlockInformation(block);
        if (tileEntity != null) {
            switch (BuilderConfiguration.teMode) {
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
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isReplaceable(world, pos)) {
            return true;
        }
        return isEmpty(state, block);
    }

    // True if this block can just be overwritten (i.e. are or support block)
    public static boolean isEmpty(IBlockState state, Block block) {
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
            world.setBlockToAir(pos);
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
        long rf = getStoredPower();
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(srcWorld, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }

        if (isEmptyOrReplacable(destWorld, destPos)) {
            if (srcWorld.isAirBlock(srcPos)) {
                return;
            }
            IBlockState srcState = srcWorld.getBlockState(srcPos);
            TakeableItem takeableItem = createTakeableItem(srcWorld, srcPos, srcState);
            ItemStack consumedStack = takeableItem.peek();
            if (consumedStack.isEmpty()) {
                return;
            }

            FakePlayer fakePlayer = getHarvester();
            IBlockState newState = BlockTools.placeStackAt(fakePlayer, consumedStack, destWorld, destPos, srcState);
            destWorld.setBlockState(destPos, newState, 3);  // placeBlockAt can reset the orientation. Restore it here

            if (!ItemStack.areItemStacksEqual(consumedStack, takeableItem.peek())) { // Did we actually use up whatever we were holding?
                if(!consumedStack.isEmpty()) { // Are we holding something else that we should put back?
                    consumedStack = takeableItem.takeAndReplace(consumedStack); // First try to put our new item where we got what we placed
                    if(!consumedStack.isEmpty()) { // If that didn't work, then try to put it anywhere it will fit
                        consumedStack = insertItem(consumedStack);
                        if(!consumedStack.isEmpty()) { // If that still didn't work, then just drop whatever we're holding
                            getWorld().spawnEntity(new EntityItem(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), consumedStack));
                        }
                    }
                } else {
                    takeableItem.take(); // If we aren't holding anything, then just consume what we placed
                }
            }

            if (!silent) {
                SoundTools.playSound(destWorld, newState.getBlock().getSoundType(newState, destWorld, destPos, fakePlayer).getPlaceSound(), destPos.getX(), destPos.getY(), destPos.getZ(), 1.0f, 1.0f);
            }

            consumeEnergy(rfNeeded);
        }
    }

    private double getDimensionCostFactor(World world, World destWorld) {
        return destWorld.provider.getDimension() == world.provider.getDimension() ? 1.0 : BuilderConfiguration.dimensionCostFactor;
    }

    private boolean consumeEntityEnergy(int rfNeeded, int rfNeededPlayer, Entity entity) {
        long rf = getStoredPower();
        int rfn;
        if (entity instanceof EntityPlayer) {
            rfn = rfNeededPlayer;
        } else {
            rfn = rfNeeded;
        }
        if (rfn > rf) {
            // Not enough energy.
            return true;
        } else {
            consumeEnergy(rfn);
        }
        return false;
    }

    private void moveEntities(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerEntity * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        int rfNeededPlayer = (int) (BuilderConfiguration.builderRfPerPlayer * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);

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
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerEntity * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        int rfNeededPlayer = (int) (BuilderConfiguration.builderRfPerPlayer * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);

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
        if (!TeleportationTools.allowTeleport(entity, world.provider.getDimension(), entity.getPosition(), destWorld.provider.getDimension(), new BlockPos(newX, newY, newZ))) {
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
        IBlockState oldDestState = destWorld.getBlockState(destPos);
        Block oldDestBlock = oldDestState.getBlock();
        if (isEmpty(oldDestState, oldDestBlock)) {
            IBlockState srcState = srcWorld.getBlockState(srcPos);
            Block srcBlock = srcState.getBlock();
            if (isEmpty(srcState, srcBlock)) {
                return;
            }
            TileEntity srcTileEntity = srcWorld.getTileEntity(srcPos);
            BuilderSetup.BlockInformation srcInformation = getBlockInformation(getHarvester(), srcWorld, srcPos, srcBlock, srcTileEntity);
            if (srcInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                return;
            }

            long rf = getStoredPower();
            int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(srcWorld, destWorld) * srcInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
            if (rfNeeded > rf) {
                // Not enough energy.
                return;
            } else {
                consumeEnergy(rfNeeded);
            }

            NBTTagCompound tc = null;
            if (srcTileEntity != null) {
                tc = new NBTTagCompound();
                srcTileEntity.writeToNBT(tc);
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

    private void setTileEntityNBT(World destWorld, NBTTagCompound tc, BlockPos destpos, IBlockState newDestState) {
        tc.setInteger("x", destpos.getX());
        tc.setInteger("y", destpos.getY());
        tc.setInteger("z", destpos.getZ());
        TileEntity tileEntity = TileEntity.create(destWorld, tc);
        if (tileEntity != null) {
            destWorld.getChunkFromBlockCoords(destpos).addTileEntity(tileEntity);
            tileEntity.markDirty();
            destWorld.notifyBlockUpdate(destpos, newDestState, newDestState, 3);
        }
    }

    private void swapBlock(World srcWorld, BlockPos srcPos, World destWorld, BlockPos dstPos) {
        IBlockState oldSrcState = srcWorld.getBlockState(srcPos);
        Block srcBlock = oldSrcState.getBlock();
        TileEntity srcTileEntity = srcWorld.getTileEntity(srcPos);

        IBlockState oldDstState = destWorld.getBlockState(dstPos);
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

        long rf = getStoredPower();
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(srcWorld, destWorld) * srcInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
        rfNeeded += (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(srcWorld, destWorld) * dstInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        } else {
            consumeEnergy(rfNeeded);
        }

        srcWorld.removeTileEntity(srcPos);
        srcWorld.setBlockToAir(srcPos);
        destWorld.removeTileEntity(dstPos);
        destWorld.setBlockToAir(dstPos);

        IBlockState newDstState = oldSrcState;
        destWorld.setBlockState(dstPos, newDstState, 3);
//        destWorld.setBlockMetadataWithNotify(destX, destY, destZ, srcMeta, 3);
        if (srcTileEntity != null) {
            srcTileEntity.validate();
            destWorld.setTileEntity(dstPos, srcTileEntity);
            srcTileEntity.markDirty();
            destWorld.notifyBlockUpdate(dstPos, newDstState, newDstState, 3);
        }

        IBlockState newSrcState = oldDstState;
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
    public void invalidate() {
        super.invalidate();
        chunkUnload();
    }

    private void chunkUnload() {
        if (forcedChunk != null && ticket != null) {
            ForgeChunkManager.unforceChunk(ticket, forcedChunk);
            forcedChunk = null;
        }
    }

    private boolean chunkLoad(int x, int z) {
        int cx = x >> 4;
        int cz = z >> 4;

        if (WorldTools.chunkLoaded(getWorld(), new BlockPos(x, 0, z))) {
            return true;
        }

        if (BuilderConfiguration.quarryChunkloads) {
            if (ticket == null) {
                ticket = ForgeChunkManager.requestTicket(RFTools.instance, getWorld(), ForgeChunkManager.Type.NORMAL);
                if (ticket == null) {
                    // Chunk is not loaded and we can't get a ticket.
                    return false;
                }
            }

            ChunkPos pair = new ChunkPos(cx, cz);
            if (pair.equals(forcedChunk)) {
                return true;
            }
            if (forcedChunk != null) {
                ForgeChunkManager.unforceChunk(ticket, forcedChunk);
            }
            forcedChunk = pair;
            ForgeChunkManager.forceChunk(ticket, forcedChunk);
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

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return CONTAINER_FACTORY.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return CONTAINER_FACTORY.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return CONTAINER_FACTORY.isOutputSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index == SLOT_TAB && !inventoryHelper.getStackInSlot(index).isEmpty() && amount > 0) {
            // Restart if we go from having a stack to not having stack or the other way around.
            refreshSettings();
        }
        if (index == SLOT_FILTER) {
            filterCache = null;
        }
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == SLOT_TAB && ((stack.isEmpty()
                && !inventoryHelper.getStackInSlot(index).isEmpty())
                || (!stack.isEmpty() && inventoryHelper.getStackInSlot(index).isEmpty()))) {
            // Restart if we go from having a stack to not having stack or the other way around.
            refreshSettings();
        }
        if (index == SLOT_FILTER) {
            filterCache = null;
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
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
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return stack.getItem() == BuilderSetup.spaceChamberCardItem || stack.getItem() == BuilderSetup.shapeCardItem;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        if(tagCompound.hasKey("overflowItems")) {
            NBTTagList overflowItemsNbt = tagCompound.getTagList("overflowItems", Constants.NBT.TAG_COMPOUND);
            overflowItems = new ArrayList<>(overflowItemsNbt.tagCount());
            for(NBTBase overflowNbt : overflowItemsNbt) {
                overflowItems.add(new ItemStack((NBTTagCompound)overflowNbt));
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if(overflowItems != null) {
            NBTTagList overflowItemsNbt = new NBTTagList();
            for(ItemStack overflow : overflowItems) {
                overflowItemsNbt.appendTag(overflow.writeToNBT(new NBTTagCompound()));
            }
            tagCompound.setTag("overflowItems", overflowItemsNbt);
        }
        return tagCompound;
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);

        // Workaround to get the redstone mode for old builders to default to 'on'
        if (!tagCompound.hasKey("rsMode")) {
            rsMode = RedstoneMode.REDSTONE_ONREQUIRED;
        }


        readBufferFromNBT(tagCompound, inventoryHelper);
        if (tagCompound.hasKey("lastError")) {
            lastError = tagCompound.getString("lastError");
        } else {
            lastError = null;
        }
        mode = tagCompound.getInteger("mode");
        anchor = tagCompound.getInteger("anchor");
        rotate = tagCompound.getInteger("rotate");
        silent = tagCompound.getBoolean("silent");
        supportMode = tagCompound.getBoolean("support");
        entityMode = tagCompound.getBoolean("entityMode");
        loopMode = tagCompound.getBoolean("loopMode");
        if (tagCompound.hasKey("waitMode")) {
            waitMode = tagCompound.getBoolean("waitMode");
        } else {
            waitMode = true;
        }
        hilightMode = tagCompound.getBoolean("hilightMode");
        scan = BlockPosTools.readFromNBT(tagCompound, "scan");
        minBox = BlockPosTools.readFromNBT(tagCompound, "minBox");
        maxBox = BlockPosTools.readFromNBT(tagCompound, "maxBox");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        if (lastError != null) {
            tagCompound.setString("lastError", lastError);
        }
        tagCompound.setInteger("mode", mode);
        tagCompound.setInteger("anchor", anchor);
        tagCompound.setInteger("rotate", rotate);
        tagCompound.setBoolean("silent", silent);
        tagCompound.setBoolean("support", supportMode);
        tagCompound.setBoolean("entityMode", entityMode);
        tagCompound.setBoolean("loopMode", loopMode);
        tagCompound.setBoolean("waitMode", waitMode);
        tagCompound.setBoolean("hilightMode", hilightMode);
        BlockPosTools.writeToNBT(tagCompound, "scan", scan);
        BlockPosTools.writeToNBT(tagCompound, "minBox", minBox);
        BlockPosTools.writeToNBT(tagCompound, "maxBox", maxBox);
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
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
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
    public void onBlockBreak(World world, BlockPos pos, IBlockState state) {
        if (hasSupportMode()) {
            clearSupportBlocks();
        }
    }

    @SuppressWarnings("NullableProblems")
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.add(1, 2, 1));
    }

    @Optional.Method(modid = "theoneprobe")
    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        int scan = getCurrentLevel();
        probeInfo.text(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
    }

    private static long lastTime = 0;

    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);
        if (System.currentTimeMillis() - lastTime > 250) {
            lastTime = System.currentTimeMillis();
            requestCurrentLevel();
        }
        int scan = BuilderTileEntity.getCurrentLevelClientSide();
        currenttip.add(TextFormatting.GREEN + "Current level: " + (scan == -1 ? "not scanning" : scan));
    }

    @Override
    public void rotateBlock(EnumFacing axis) {
        super.rotateBlock(axis);
        if (!world.isRemote) {
            if (hasSupportMode()) {
                clearSupportBlocks();
                resetBox();
            }
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState metadata, int fortune) {
        super.getDrops(drops, world, pos, metadata, fortune);
        List<ItemStack> overflowItems = getOverflowItems();
        if(overflowItems != null) {
            drops.addAll(overflowItems);
        }
    }
}
