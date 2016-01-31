package mcjty.rftools.blocks.builder;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketRequestIntegerFromServer;
import mcjty.lib.varia.BlockMeta;
import mcjty.lib.varia.BlockPosTools;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.RFToolsTools;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.items.builder.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockStaticLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.*;

public class BuilderTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String COMPONENT_NAME = "builder";

    public static final String CMD_SETMODE = "setMode";
    public static final String CMD_SETANCHOR = "setAnchor";
    public static final String CMD_SETROTATE = "setRotate";
    public static final String CMD_SETSILENT = "setSilent";
    public static final String CMD_SETSUPPORT = "setSupport";
    public static final String CMD_SETENTITIES = "setEntities";
    public static final String CMD_SETLOOP = "setLoop";
    public static final String CMD_GETLEVEL = "getLevel";
    public static final String CLIENTCMD_GETLEVEL = "getLevel";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, BuilderContainer.factory, 1);

    public static final int MODE_COPY = 0;
    public static final int MODE_MOVE = 1;
    public static final int MODE_SWAP = 2;
    public static final int MODE_BACK = 3;

    public static final String[] MODES = new String[] { "Copy", "Move", "Swap", "Back" };

    public static final String ROTATE_0 = "0";
    public static final String ROTATE_90 = "90";
    public static final String ROTATE_180 = "180";
    public static final String ROTATE_270 = "270";

    public static final int ANCHOR_SW = 0;
    public static final int ANCHOR_SE = 1;
    public static final int ANCHOR_NW = 2;
    public static final int ANCHOR_NE = 3;

    private int mode = MODE_COPY;
    private int rotate = 0;
    private int anchor = ANCHOR_SW;
    private boolean silent = false;
    private boolean supportMode = false;
    private boolean entityMode = false;
    private boolean loopMode = false;

    // For usage in the gui
    private static int currentLevel = 0;

    private int powered = 0;

    private boolean boxValid = false;
    private BlockPos minBox = null;
    private BlockPos maxBox = null;
    private BlockPos scan = null;
    private int projDx;
    private int projDy;
    private int projDz;

    private int cardType = ShapeCardItem.CARD_UNKNOWN; // One of the card types out of ShapeCardItem.CARD_...

    // For chunkloading with the quarry.
    private ForgeChunkManager.Ticket ticket = null;
    // The currently forced chunk.
    private ChunkCoordIntPair forcedChunk = null;

    // Cached set of blocks that we need to build in shaped mode
    private Set<BlockPos> cachedBlocks = null;
    private ChunkCoordIntPair cachedChunk = null;       // For which chunk are the cachedBlocks valid

    // Cached set of blocks that we want to void with the quarry.
    private Set<Block> cachedVoidableBlocks = null;

    public BuilderTileEntity() {
        super(BuilderConfiguration.BUILDER_MAXENERGY, BuilderConfiguration.BUILDER_RECEIVEPERTICK);
    }

    private boolean isShapeCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        if (itemStack == null || itemStack.stackSize == 0) {
            return false;
        }
        return itemStack.getItem() == BuilderSetup.shapeCardItem;
    }

    private NBTTagCompound hasCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        if (itemStack == null || itemStack.stackSize == 0) {
            return null;
        }

        return itemStack.getTagCompound();
    }

    private void makeSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);
        ShapeCardItem.Shape shape = ShapeCardItem.getShape(shapeCard);
        List<BlockPos> blocks = new ArrayList<>();
        ShapeCardItem.composeShape(shape.makeHollow(), worldObj, getPos(), dimension, offset, blocks,
                BuilderConfiguration.maxBuilderDimension*256* BuilderConfiguration.maxBuilderDimension, false, null);
        for (BlockPos p : blocks) {
            if (worldObj.isAirBlock(p)) {
                worldObj.setBlockState(p, BuilderSetup.supportBlock.getDefaultState().withProperty(SupportBlock.STATUS, SupportBlock.STATUS_OK), 3);
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

            for (int x = minBox.getX() ; x <= maxBox.getX() ; x++) {
                for (int y = minBox.getY() ; y <= maxBox.getY() ; y++) {
                    for (int z = minBox.getZ() ; z <= maxBox.getZ() ; z++) {
                        BlockPos src = new BlockPos(x, y, z);
                        BlockPos dest = sourceToDest(src);
                        IBlockState srcState = world.getBlockState(src);
                        Block srcBlock = srcState.getBlock();
                        IBlockState dstState = world.getBlockState(dest);
                        Block dstBlock = dstState.getBlock();
                        int error = SupportBlock.STATUS_OK;
                        if (mode != MODE_COPY) {
                            TileEntity srcTileEntity = world.getTileEntity(src);
                            TileEntity dstTileEntity = worldObj.getTileEntity(dest);

                            int error1 = isMovable(world, src.getX(), src.getY(), src.getZ(), srcBlock, srcTileEntity);
                            int error2 = isMovable(worldObj, dest.getX(), dest.getY(), dest.getZ(), dstBlock, dstTileEntity);
                            error = Math.max(error1, error2);
                        }
                        if (isEmpty(srcBlock) && !isEmpty(dstBlock)) {
                            worldObj.setBlockState(src, BuilderSetup.supportBlock.getDefaultState().withProperty(SupportBlock.STATUS, error), 3);
                        }
                        if (isEmpty(dstBlock) && !isEmpty(srcBlock)) {
                            worldObj.setBlockState(dest, BuilderSetup.supportBlock.getDefaultState().withProperty(SupportBlock.STATUS, error), 3);
                        }
                    }
                }
            }
        }
    }

    private void clearSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
        BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);
        ShapeCardItem.Shape shape = ShapeCardItem.getShape(shapeCard);
        List<BlockPos> blocks = new ArrayList<BlockPos>();
        ShapeCardItem.composeShape(shape.makeHollow(), worldObj, getPos(), dimension, offset, blocks, BuilderConfiguration.maxSpaceChamberDimension* BuilderConfiguration.maxSpaceChamberDimension* BuilderConfiguration.maxSpaceChamberDimension, false, null);
        for (BlockPos block : blocks) {
            if (worldObj.getBlockState(block).getBlock() == BuilderSetup.supportBlock) {
                worldObj.setBlockToAir(block);
            }
        }
    }

    public void clearSupportBlocks() {
        if (worldObj.isRemote) {
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

            for (int x = minBox.getX() ; x <= maxBox.getX() ; x++) {
                for (int y = minBox.getY() ; y <= maxBox.getY() ; y++) {
                    for (int z = minBox.getZ() ; z <= maxBox.getZ() ; z++) {
                        BlockPos src = new BlockPos(x, y, z);
                        if (world != null) {
                            Block srcBlock = world.getBlockState(src).getBlock();
                            if (srcBlock == BuilderSetup.supportBlock) {
                                world.setBlockToAir(src);
                            }
                        }
                        BlockPos dest = sourceToDest(src);
                        Block dstBlock = worldObj.getBlockState(dest).getBlock();
                        if (dstBlock == BuilderSetup.supportBlock) {
                            worldObj.setBlockToAir(dest);
                        }
                    }
                }
            }
        }
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
            ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
            BlockPos dimension = ShapeCardItem.getDimension(shapeCard);
            BlockPos minBox = positionBox(dimension);
            int dx = dimension.getX();
            int dy = dimension.getY();
            int dz = dimension.getZ();

            BlockPos offset = new BlockPos(minBox.getX() + (int)Math.ceil(dx / 2), minBox.getY() + (int)Math.ceil(dy / 2), minBox.getZ() + (int)Math.ceil(dz / 2));
            NBTTagCompound tagCompound = shapeCard.getTagCompound();
            tagCompound.setInteger("offsetX", offset.getX());
            tagCompound.setInteger("offsetY", offset.getY());
            tagCompound.setInteger("offsetZ", offset.getZ());
        }

        if (supportMode) {
            makeSupportBlocks();
        }
        markDirtyClient();
    }

    // Give a dimension, return a min coordinate of the box right in front of the builder
    private BlockPos positionBox(BlockPos dimension) {
        IBlockState state = worldObj.getBlockState(getPos());
        EnumFacing direction = state.getValue(BuilderSetup.builderBlock.FACING_HORIZ);
        int spanX = dimension.getX();
        int spanY = dimension.getY();
        int spanZ = dimension.getZ();
        int x = 0;
        int y;
        int z = 0;
        y = - ((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY - 1 : 0);
        switch (direction) {
            case SOUTH:
                x = - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX - 1 : 0);
                z = - spanZ;
                break;
            case NORTH:
                x = 1 - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX - 1 : 0);
                z = 1;
                break;
            case WEST:
                x = 1;
                z = - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ - 1 : 0);
                break;
            case EAST:
                x = - spanX;
                z = - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? 0 : spanZ - 1);
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
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            if (loopMode || (powered > 0 && scan == null)) {
                restartScan();
            }
            markDirty();
        }
    }

    private BlockPos rotate(BlockPos c) {
        switch (rotate) {
            case 0: return c;
            case 1: return new BlockPos(-c.getZ(), c.getY(), c.getX());
            case 2: return new BlockPos(-c.getX(), c.getY(), -c.getZ());
            case 3: return new BlockPos(c.getZ(), c.getY(), -c.getX());
        }
        return c;
    }

    private void createProjection(SpaceChamberRepository.SpaceChamberChannel chamberChannel) {
        BlockPos minC = rotate(chamberChannel.getMinCorner());
        BlockPos maxC = rotate(chamberChannel.getMaxCorner());
        BlockPos minCorner = new BlockPos(Math.min(minC.getX(), maxC.getX()), Math.min(minC.getY(), maxC.getY()), Math.min(minC.getZ(), maxC.getZ()));
        BlockPos maxCorner = new BlockPos(Math.max(minC.getX(), maxC.getX()), Math.max(minC.getY(), maxC.getY()), Math.max(minC.getZ(), maxC.getZ()));

        IBlockState state = worldObj.getBlockState(getPos());
        EnumFacing direction = state.getValue(BuilderSetup.builderBlock.FACING_HORIZ);
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

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(worldObj);
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
        cardType = ShapeCardItem.CARD_SPACE;

        createProjection(chamberChannel);

        minBox = minCorner;
        maxBox = maxCorner;
        restartScan();
    }

    private void checkStateServerShaped() {
        float factor = getInfusedFactor();
        for (int i = 0 ; i < BuilderConfiguration.quarryBaseSpeed + (factor * BuilderConfiguration.quarryInfusionSpeedFactor) ; i++) {
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
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (powered == 0 && loopMode) {
            return;
        }

        if (scan == null) {
            return;
        }

        if (isShapeCard()) {
            if (powered == 0) {
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

        float factor = getInfusedFactor();
        for (int i = 0 ; i < 2 + (factor * 40) ; i++) {
            if (scan != null) {
                handleBlock(world);
            }
        }
    }

    private void calculateBoxShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        if (shapeCard == null) {
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
        cardType = shapeCard.getItemDamage();

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

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(worldObj);
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

    private Set<BlockPos> getCachedBlocks(ChunkCoordIntPair chunk) {
        if ((chunk != null && !chunk.equals(cachedChunk)) || (chunk == null && cachedChunk != null)) {
            cachedBlocks = null;
        }

        if (cachedBlocks == null) {
            cachedBlocks = new HashSet<BlockPos>();
            ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
            ShapeCardItem.Shape shape = ShapeCardItem.getShape(shapeCard);
            BlockPos dimension = ShapeCardItem.getClampedDimension(shapeCard, BuilderConfiguration.maxBuilderDimension);
            BlockPos offset = ShapeCardItem.getClampedOffset(shapeCard, BuilderConfiguration.maxBuilderOffset);
            ShapeCardItem.composeShape(shape, worldObj, getPos(), dimension, offset, cachedBlocks,
                    BuilderConfiguration.maxSpaceChamberDimension * BuilderConfiguration.maxSpaceChamberDimension * BuilderConfiguration.maxSpaceChamberDimension,
                    !ShapeCardItem.isNormalShapeCard(shapeCard), chunk);
            cachedChunk = chunk;
        }
        return cachedBlocks;
    }

    private void handleBlockShaped() {
        for (int i = 0 ; i < 100 ; i++) {
            if (scan == null) {
                return;
            }
            if (getCachedBlocks(new ChunkCoordIntPair(scan.getX() >> 4, scan.getZ() >> 4)).contains(scan)) {
                if (!handleSingleBlock()) {
                    nextLocation();
                }
                return;
            } else {
                nextLocation();
            }
        }
    }

    private int getCardType() {
        if (cardType == ShapeCardItem.CARD_UNKNOWN) {
            ItemStack card = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
            if (card != null) {
                cardType = card.getItemDamage();
            }
        }
        return cardType;
    }

    // Return true if we have to wait at this spot.
    private boolean handleSingleBlock() {
        int sx = scan.getX();
        int sy = scan.getY();
        int sz = scan.getZ();
        if (!chunkLoad(sx, sz)) {
            // The chunk is not available and we could not chunkload it. We have to wait.
            return true;
        }

        int rfNeeded;

        switch (getCardType()) {
            case ShapeCardItem.CARD_VOID:
                rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.voidShapeCardFactor);
                break;
            case ShapeCardItem.CARD_QUARRY_FORTUNE:
            case ShapeCardItem.CARD_QUARRY_CLEAR_FORTUNE:
                rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.fortunequarryShapeCardFactor);
                break;
            case ShapeCardItem.CARD_QUARRY_SILK:
            case ShapeCardItem.CARD_QUARRY_CLEAR_SILK:
                rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.silkquarryShapeCardFactor);
                break;
            case ShapeCardItem.CARD_QUARRY:
            case ShapeCardItem.CARD_QUARRY_CLEAR:
                rfNeeded = BuilderConfiguration.builderRfPerQuarry;
                break;
            case ShapeCardItem.CARD_SHAPE:
                rfNeeded = BuilderConfiguration.builderRfPerOperation;
                break;
            default:
                rfNeeded = 0;
                break;
        }

        Block block = null;
        if (getCardType() != ShapeCardItem.CARD_SHAPE) {
            BlockPos spos = new BlockPos(sx, sy, sz);
            block = worldObj.getBlockState(spos).getBlock();
            if (!isEmpty(block)) {
                float hardness;
                if (block instanceof BlockDynamicLiquid || block instanceof BlockStaticLiquid) {
                    hardness = 1.0f;
                } else {
                    if (getCachedVoidableBlocks().contains(block)) {
                        rfNeeded = (int) (BuilderConfiguration.builderRfPerQuarry * BuilderConfiguration.voidShapeCardFactor);
                    }
                    hardness = block.getBlockHardness(worldObj, spos);
                }
                rfNeeded *= (int) ((hardness + 1) * 2);
            }
        }

        rfNeeded = (int) (rfNeeded * (3.0f - getInfusedFactor()) / 3.0f);

        if (rfNeeded > getEnergyStored(EnumFacing.DOWN)) {
            // Not enough energy.
            return true;
        }

        switch (getCardType()) {
            case ShapeCardItem.CARD_VOID:
                return voidBlock(rfNeeded, sx, sy, sz, block);
            case ShapeCardItem.CARD_QUARRY:
            case ShapeCardItem.CARD_QUARRY_CLEAR:
                return quarryBlock(rfNeeded, sx, sy, sz, block);
            case ShapeCardItem.CARD_QUARRY_FORTUNE:
            case ShapeCardItem.CARD_QUARRY_CLEAR_FORTUNE:
                return quarryBlock(rfNeeded, sx, sy, sz, block);
            case ShapeCardItem.CARD_QUARRY_SILK:
            case ShapeCardItem.CARD_QUARRY_CLEAR_SILK:
                return silkQuarryBlock(rfNeeded, sx, sy, sz, block);
            case ShapeCardItem.CARD_SHAPE:
                return buildBlock(rfNeeded, sx, sy, sz);
        }
        return true;
    }

    private boolean buildBlock(int rfNeeded, int sx, int sy, int sz) {
        if (isEmptyOrReplacable(worldObj, sx, sy, sz)) {
            BlockMeta block = consumeBlock(null, 0);
            if (block == null) {
                return true;    // We could not find a block. Wait
            }

            // @todo property?
            worldObj.setBlockState(new BlockPos(sx, sy, sz), block.getBlock().getStateFromMeta(block.getMeta()), 3);
//            worldObj.setBlockMetadataWithNotify(sx, sy, sz, block.getMeta(), 3);
            if (!silent) {
                RFToolsTools.playSound(worldObj, block.getBlock().stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
            }

            consumeEnergy(rfNeeded);
        }
        return false;
    }

    private Set<Block> getCachedVoidableBlocks() {
        if (cachedVoidableBlocks == null) {
            ItemStack card = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
            if (card != null && card.getItem() == BuilderSetup.shapeCardItem) {
                cachedVoidableBlocks = ShapeCardItem.getVoidedBlocks(card);
            } else {
                cachedVoidableBlocks = Collections.emptySet();
            }
        }
        return cachedVoidableBlocks;
    }

    private void clearOrDirtBlock(int rfNeeded, int sx, int sy, int sz, Block block, boolean clear) {
        BlockPos spos = new BlockPos(sx, sy, sz);
        if (clear) {
            worldObj.setBlockToAir(spos);
        } else {
            worldObj.setBlockState(spos, Blocks.dirt.getDefaultState(), 2);       // No block update!
        }
        consumeEnergy(rfNeeded);
        if (!silent) {
            RFToolsTools.playSound(worldObj, block.stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
        }
    }

    private boolean silkQuarryBlock(int rfNeeded, int sx, int sy, int sz, Block block) {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        if (sx >= xCoord-1 && sx <= xCoord+1 && sy >= yCoord-1 && sy <= yCoord+1 && sz >= zCoord-1 && sz <= zCoord+1) {
            // Skip a 3x3x3 block around the builder.
            return false;
        }
        if (isEmpty(block)) {
            return false;
        }
        BlockPos spos = new BlockPos(sx, sy, sz);
        if (block.getBlockHardness(worldObj, spos) >= 0) {
            boolean clear = ShapeCardItem.isClearingQuarry(getCardType());
            if ((!clear) && block == Blocks.dirt) {
                // We can skip dirt if we are not clearing.
                return false;
            }
            if (worldObj.getTileEntity(spos) != null) {
                // Skip tile entities
                return false;
            }

            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
            if (block.canEntityDestroy(worldObj, spos, fakePlayer)) {
                if (getCachedVoidableBlocks().contains(block)) {
                    clearOrDirtBlock(rfNeeded, sx, sy, sz, block, clear);
                } else {
                    IBlockState state = worldObj.getBlockState(spos);
                    List<ItemStack> drops;
                    if (block.canSilkHarvest(worldObj, spos, state, fakePlayer)) {
                        Item item = Item.getItemFromBlock(block);
                        if (item != null) {
                            int m = 0;
                            if (item.getHasSubtypes()) {
                                m = block.getMetaFromState(state);
                            }
                            drops = Collections.singletonList(new ItemStack(item, 1, m));
                        } else {
                            drops = Collections.emptyList();
                        }
                    } else {
                        drops = block.getDrops(worldObj, spos, state, 0);
                    }
                    if (checkAndInsertItems(drops)) {
                        clearOrDirtBlock(rfNeeded, sx, sy, sz, block, clear);
                    } else {
                        return true;    // Not enough room. Wait
                    }
                }
            }
        }
        return false;
    }

    private boolean quarryBlock(int rfNeeded, int sx, int sy, int sz, Block block) {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        if (sx >= xCoord-1 && sx <= xCoord+1 && sy >= yCoord-1 && sy <= yCoord+1 && sz >= zCoord-1 && sz <= zCoord+1) {
            // Skip a 3x3x3 block around the builder.
            return false;
        }
        if (isEmpty(block)) {
            return false;
        }
        BlockPos spos = new BlockPos(sx, sy, sz);
        if (block.getBlockHardness(worldObj, spos) >= 0) {
            boolean clear = ShapeCardItem.isClearingQuarry(getCardType());
            if ((!clear) && block == Blocks.dirt) {
                // We can skip dirt if we are not clearing.
                return false;
            }
            if (worldObj.getTileEntity(spos) != null) {
                // Skip tile entities
                return false;
            }

            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
            if (block.canEntityDestroy(worldObj, spos, fakePlayer)) {
                if (getCachedVoidableBlocks().contains(block)) {
                    clearOrDirtBlock(rfNeeded, sx, sy, sz, block, clear);
                } else {
                    IBlockState state = worldObj.getBlockState(spos);
                    List<ItemStack> drops = block.getDrops(worldObj, spos, state, (getCardType() == ShapeCardItem.CARD_QUARRY_FORTUNE || getCardType() == ShapeCardItem.CARD_QUARRY_CLEAR_FORTUNE) ? 3 : 0);
                    if (checkAndInsertItems(drops)) {
                        clearOrDirtBlock(rfNeeded, sx, sy, sz, block, clear);
                    } else {
                        return true;    // Not enough room. Wait
                    }
                }
            }
        }
        return false;
    }

    private boolean voidBlock(int rfNeeded, int sx, int sy, int sz, Block block) {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        if (sx >= xCoord-1 && sx <= xCoord+1 && sy >= yCoord-1 && sy <= yCoord+1 && sz >= zCoord-1 && sz <= zCoord+1) {
            // Skip a 3x3x3 block around the builder.
            return false;
        }
        BlockPos spos = new BlockPos(sx, sy, sz);
        if (block.getBlockHardness(worldObj, spos) >= 0) {
            if (!silent) {
                RFToolsTools.playSound(worldObj, block.stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
            }
            worldObj.setBlockToAir(spos);
            consumeEnergy(rfNeeded);
        }
        return false;
    }

    private void handleBlock(World world) {
        BlockPos dest = sourceToDest(scan);
        int x = scan.getX();
        int y = scan.getY();
        int z = scan.getZ();
        int destX = dest.getX();
        int destY = dest.getY();
        int destZ = dest.getZ();

        switch (mode) {
            case MODE_COPY:
                copyBlock(world, x, y, z, worldObj, destX, destY, destZ);
                break;
            case MODE_MOVE:
                if (entityMode) {
                    moveEntities(world, x, y, z, worldObj, destX, destY, destZ);
                }
                moveBlock(world, x, y, z, worldObj, destX, destY, destZ, rotate);
                break;
            case MODE_BACK:
                if (entityMode) {
                    moveEntities(worldObj, destX, destY, destZ, world, x, y, z);
                }
                moveBlock(worldObj, destX, destY, destZ, world, x, y, z, oppositeRotate());
                break;
            case MODE_SWAP:
                if (entityMode) {
                    swapEntities(world, x, y, z, worldObj, destX, destY, destZ);
                }
                swapBlock(world, x, y, z, worldObj, destX, destY, destZ);
                break;
        }

        nextLocation();
    }

    private static Random random = new Random();

    // Also works if block is null and just picks the first available block.
    private BlockMeta findAndConsumeBlock(IInventory inventory, Block block, int meta) {
        if (block == null) {
            // We are not looking for a specific block. Pick a random one out of the chest.
            List<Integer> slots = new ArrayList<Integer>();
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlock) {
                    slots.add(i);
                }
            }
            if (slots.size() == 0) {
                return null;
            }
            int randomSlot = slots.get(random.nextInt(slots.size()));
            ItemStack stack = inventory.getStackInSlot(randomSlot);
            ItemBlock itemBlock = (ItemBlock) stack.getItem();
            inventory.decrStackSize(randomSlot, 1);
            return new BlockMeta(itemBlock.getBlock(), stack.getItemDamage());
        } else {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlock) {
                    ItemBlock itemBlock = (ItemBlock) stack.getItem();
                    if (itemBlock.getBlock() == block && (meta == -1 || stack.getItemDamage() == meta)) {
                        inventory.decrStackSize(i, 1);
                        return new BlockMeta(itemBlock.getBlock(), stack.getItemDamage());
                    }
                }
            }
        }
        return null;
    }

    private boolean checkAndInsertItems(List<ItemStack> items) {
        TileEntity teAbove = worldObj.getTileEntity(getPos().up());
        boolean ok = false;
        if (teAbove instanceof IInventory) {
            ok = checkAndInsertItems(items, (IInventory) teAbove);
        }
        if (ok) {
            return true;
        }
        TileEntity teDown = worldObj.getTileEntity(getPos().down());
        if (teDown instanceof IInventory) {
            ok = checkAndInsertItems(items, (IInventory) teDown);
        }
        return ok;
    }

    private boolean checkAndInsertItems(List<ItemStack> items, IInventory inventory) {
        Map<Integer, ItemStack> undo = new HashMap<>();
        for (ItemStack item : items) {
            int remaining = InventoryHelper.mergeItemStackSafe(inventory, false, EnumFacing.DOWN, item, 0, inventory.getSizeInventory(), undo);
            if (remaining > 0) {
                undo(undo, inventory);
                return false;
            }
        }
        return true;
    }

    private void undo(Map<Integer,ItemStack> undo, IInventory inventory) {
        for (Map.Entry<Integer, ItemStack> entry : undo.entrySet()) {
            inventory.setInventorySlotContents(entry.getKey(), entry.getValue());
        }
    }


    private BlockMeta consumeBlock(Block block, int meta) {
        TileEntity te = worldObj.getTileEntity(getPos().up());
        if (te instanceof IInventory) {
            BlockMeta b = findAndConsumeBlock((IInventory) te, block, meta);
            if (b != null) {
                return b;
            }
        }
        te = worldObj.getTileEntity(getPos().down());
        if (te instanceof IInventory) {
            BlockMeta b = findAndConsumeBlock((IInventory) te, block, meta);
            if (b != null) {
                return b;
            }
        }
//        if (meta != -1) {
            // Try a second time with meta equal to -1 (which means to ignore meta).
//            return consumeBlock(block, -1);
//        }
        return null;
    }

    public static BuilderSetup.BlockInformation getBlockInformation(World world, int x, int y, int z, Block block, TileEntity tileEntity) {
        if (isEmpty(block)) {
            return BuilderSetup.BlockInformation.FREE;
        }

        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
        if (!block.canEntityDestroy(world, new BlockPos(x, y, z), fakePlayer)) {
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

    private int isMovable(World world, int x, int y, int z, Block block, TileEntity tileEntity) {
        return getBlockInformation(world, x, y, z, block, tileEntity).getBlockLevel();
    }

    public static boolean isEmptyOrReplacable(World world, int x, int y, int z) {
        Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
        if (block.isReplaceable(world, new BlockPos(x, y, z))) {
            return true;
        }
        return isEmpty(block);
    }

    // True if this block can just be overwritten (i.e. are or support block)
    public static boolean isEmpty(Block block) {
        if (block == null) {
            return true;
        }
        if (block.getMaterial() == Material.air) {
            return true;
        }
        if (block == BuilderSetup.supportBlock) {
            return true;
        }
        return false;
    }

    private void clearBlock(World world, int x, int y, int z) {
        if (supportMode) {
            world.setBlockState(new BlockPos(x, y, z), BuilderSetup.supportBlock.getDefaultState(), 3);
        } else {
            world.setBlockToAir(new BlockPos(x, y, z));
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

    private int rotateMeta(Block block, int meta, BuilderSetup.BlockInformation information, int rotMode) {
        Item item = Item.getItemFromBlock(block);
        if (item != null && item.getHasSubtypes()) {
            // If the item has subtypes we cannot rotate it.
            return meta;
        }

        switch (information.getRotateInfo()) {
            // @todo do this the proper way!
            case BuilderSetup.BlockInformation.ROTATE_mfff:
//                switch (rotMode) {
//                    case 0: return meta;
//                    case 1: {
//                        EnumFacing dir = ForgeDirection.values()[meta & 7];
//                        return (meta & 8) | dir.getRotation(ForgeDirection.UP).ordinal();
//                    }
//                    case 2: {
//                        ForgeDirection dir = ForgeDirection.values()[meta & 7];
//                        return (meta & 8) | dir.getOpposite().ordinal();
//                    }
//                    case 3: {
//                        ForgeDirection dir = ForgeDirection.values()[meta & 7];
//                        return (meta & 8) | dir.getOpposite().getRotation(ForgeDirection.UP).ordinal();
//                    }
//                }
                break;
            case BuilderSetup.BlockInformation.ROTATE_mmmm:
                return meta;
        }
        return meta;
    }

    private void copyBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        int rf = getEnergyStored(EnumFacing.DOWN);
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }

        if (isEmptyOrReplacable(destWorld, destX, destY, destZ)) {
            IBlockState state = world.getBlockState(getPos());
            Block origBlock = state.getBlock();
            int origMeta = origBlock.getMetaFromState(state);
            if (origBlock == null || origBlock.getMaterial() == Material.air) {
                return;
            }
            if (consumeBlock(origBlock, origMeta) == null) {
                return;
            }

            BuilderSetup.BlockInformation information = getBlockInformation(world, x, y, z, origBlock, null);
            origMeta = rotateMeta(origBlock, origMeta, information, rotate);

            destWorld.setBlockState(new BlockPos(destX, destY, destZ), origBlock.getStateFromMeta(origMeta), 3);
//            destWorld.setBlockMetadataWithNotify(destX, destY, destZ, origMeta, 3);
            if (!silent) {
                RFToolsTools.playSound(destWorld, origBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }

            consumeEnergy(rfNeeded);
        }
    }

    private double getDimensionCostFactor(World world, World destWorld) {
        return destWorld.provider.getDimensionId() == world.provider.getDimensionId() ? 1.0 : BuilderConfiguration.dimensionCostFactor;
    }

    private boolean consumeEntityEnergy(int rfNeeded, int rfNeededPlayer, Entity entity) {
        int rf = getEnergyStored(EnumFacing.DOWN);
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
        List entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.fromBounds(x - .1, y - .1, z - .1, x + 1.1, y + 1.1, z + 1.1));
        for (Object o : entities) {
            Entity entity = (Entity) o;

            if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) return;

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
        List entitiesSrc = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.fromBounds(x, y, z, x + 1, y + 1, z + 1));
        List entitiesDst = destWorld.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.fromBounds(destX, destY, destZ, destX + 1, destY + 1, destZ + 1));
        for (Object o : entitiesSrc) {
            Entity entity = (Entity) o;
            if (isEntityInBlock(x, y, z, entity)) {
                if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) return;

                double newX = destX + (entity.posX - x);
                double newY = destY + (entity.posY - y);
                double newZ = destZ + (entity.posZ - z);
                teleportEntity(world, destWorld, entity, newX, newY, newZ);
            }
        }
        for (Object o : entitiesDst) {
            Entity entity = (Entity) o;
            if (isEntityInBlock(destX, destY, destZ, entity)) {
                if (consumeEntityEnergy(rfNeeded, rfNeededPlayer, entity)) return;

                double newX = x + (entity.posX - destX);
                double newY = y + (entity.posY - destY);
                double newZ = z + (entity.posZ - destZ);
                teleportEntity(destWorld, world, entity, newX, newY, newZ);
            }
        }
    }

    private void teleportEntity(World world, World destWorld, Entity entity, double newX, double newY, double newZ) {
        if (entity instanceof EntityPlayer) {
            if (world.provider.getDimensionId() != destWorld.provider.getDimensionId()) {
                TeleportationTools.teleportToDimension((EntityPlayer) entity, destWorld.provider.getDimensionId(), newX, newY, newZ);
            }
            entity.setPositionAndUpdate(newX, newY, newZ);
        } else {
            if (world.provider.getDimensionId() != destWorld.provider.getDimensionId()) {
                NBTTagCompound tagCompound = new NBTTagCompound();
                float rotationYaw = entity.rotationYaw;
                float rotationPitch = entity.rotationPitch;
                entity.writeToNBT(tagCompound);
                Class<? extends Entity> entityClass = entity.getClass();
                world.removeEntity(entity);

                try {
                    Entity newEntity = entityClass.getConstructor(World.class).newInstance(destWorld);
                    newEntity.readFromNBT(tagCompound);
                    newEntity.setLocationAndAngles(newX, newY, newZ, rotationYaw, rotationPitch);
                    destWorld.spawnEntityInWorld(newEntity);
                } catch (Exception e) {
                }
            } else {
                entity.setLocationAndAngles(newX, newY, newZ, entity.rotationYaw, entity.rotationPitch);
                destWorld.updateEntityWithOptionalForce(entity, false);
            }
        }
    }


    private boolean isEntityInBlock(int x, int y, int z, Entity entity) {
        if (entity.posX >= x && entity.posX < x+1 && entity.posY >= y && entity.posY < y+1 && entity.posZ >= z && entity.posZ < z+1) {
            return true;
        }
        return false;
    }

    private void moveBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ, int rotMode) {
        Block destBlock = destWorld.getBlockState(new BlockPos(destX, destY, destZ)).getBlock();
        if (isEmpty(destBlock)) {
            BlockPos srcPos = new BlockPos(x, y, z);
            IBlockState state = world.getBlockState(srcPos);
            Block origBlock = state.getBlock();
            if (isEmpty(origBlock)) {
                return;
            }
            TileEntity origTileEntity = world.getTileEntity(srcPos);
            BuilderSetup.BlockInformation information = getBlockInformation(world, x, y, z, origBlock, origTileEntity);
            if (information.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                return;
            }

            int rf = getEnergyStored(EnumFacing.DOWN);
            int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * information.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
            if (rfNeeded > rf) {
                // Not enough energy.
                return;
            } else {
                consumeEnergy(rfNeeded);
            }

            int origMeta = origBlock.getMetaFromState(state);
            origMeta = rotateMeta(origBlock, origMeta, information, rotMode);

            world.removeTileEntity(srcPos);
            clearBlock(world, x, y, z);

            BlockPos destpos = new BlockPos(destX, destY, destZ);
            destWorld.setBlockState(destpos, origBlock.getStateFromMeta(origMeta), 3);
//            destWorld.setBlockMetadataWithNotify(destX, destY, destZ, origMeta, 3);
            if (origTileEntity != null) {
                origTileEntity.validate();
                destWorld.setTileEntity(destpos, origTileEntity);
                origTileEntity.markDirty();
                destWorld.markBlockForUpdate(destpos);
            }
            if (!silent) {
                RFToolsTools.playSound(world, origBlock.stepSound.getBreakSound(), x, y, z, 1.0f, 1.0f);
                RFToolsTools.playSound(destWorld, origBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }
        }
    }

    private void swapBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        BlockPos srcPos = new BlockPos(x, y, z);
        IBlockState srcState = world.getBlockState(srcPos);
        Block srcBlock = srcState.getBlock();
        TileEntity srcTileEntity = world.getTileEntity(srcPos);

        BlockPos dstPos = new BlockPos(destX, destY, destZ);
        IBlockState dstState = destWorld.getBlockState(dstPos);
        Block dstBlock = dstState.getBlock();
        TileEntity dstTileEntity = destWorld.getTileEntity(dstPos);

        if (isEmpty(srcBlock) && isEmpty(dstBlock)) {
            return;
        }

        BuilderSetup.BlockInformation srcInformation = getBlockInformation(world, x, y, z, srcBlock, srcTileEntity);
        if (srcInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
            return;
        }

        BuilderSetup.BlockInformation dstInformation = getBlockInformation(destWorld, destX, destY, destZ, dstBlock, dstTileEntity);
        if (dstInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
            return;
        }

        int rf = getEnergyStored(EnumFacing.DOWN);
        int rfNeeded = (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * srcInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
        rfNeeded += (int) (BuilderConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * dstInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        } else {
            consumeEnergy(rfNeeded);
        }

        int srcMeta = srcBlock.getMetaFromState(srcState);
        srcMeta = rotateMeta(srcBlock, srcMeta, srcInformation, oppositeRotate());
        int dstMeta = dstBlock.getMetaFromState(dstState);
        dstMeta = rotateMeta(dstBlock, dstMeta, dstInformation, rotate);

        world.removeTileEntity(srcPos);
        world.setBlockToAir(srcPos);
        destWorld.removeTileEntity(dstPos);
        destWorld.setBlockToAir(dstPos);

        destWorld.setBlockState(dstPos, srcBlock.getStateFromMeta(srcMeta), 3);
//        destWorld.setBlockMetadataWithNotify(destX, destY, destZ, srcMeta, 3);
        if (srcTileEntity != null) {
            srcTileEntity.validate();
            destWorld.setTileEntity(dstPos, srcTileEntity);
            srcTileEntity.markDirty();
            destWorld.markBlockForUpdate(dstPos);
        }

        world.setBlockState(srcPos, dstBlock.getStateFromMeta(dstMeta), 3);
//        world.setBlockMetadataWithNotify(x, y, z, dstMeta, 3);
        if (dstTileEntity != null) {
            dstTileEntity.validate();
            world.setTileEntity(srcPos, dstTileEntity);
            dstTileEntity.markDirty();
            world.markBlockForUpdate(srcPos);
        }

        if (!silent) {
            if (!isEmpty(srcBlock)) {
                RFToolsTools.playSound(world, srcBlock.stepSound.getBreakSound(), x, y, z, 1.0f, 1.0f);
            }
            if (!isEmpty(dstBlock)) {
                RFToolsTools.playSound(destWorld, dstBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }
        }
    }

    private BlockPos sourceToDest(BlockPos source) {
        BlockPos c = rotate(source);
        return new BlockPos(c.getX() + projDx, c.getY() + projDy, c.getZ() + projDz);
    }

    private void restartScan() {
        chunkUnload();
        if (loopMode || (powered > 0 && scan == null)) {
            if (getCardType() == ShapeCardItem.CARD_SPACE) {
                calculateBox();
                scan = minBox;
            } else if (getCardType() != ShapeCardItem.CARD_UNKNOWN) {
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

        if (worldObj.getChunkProvider().chunkExists(cx, cz)) {
            return true;
        }

        if (BuilderConfiguration.quarryChunkloads) {
            if (ticket == null) {
                ForgeChunkManager.setForcedChunkLoadingCallback(RFTools.instance, new ForgeChunkManager.LoadingCallback() {
                    @Override
                    public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {

                    }
                });
                ticket = ForgeChunkManager.requestTicket(RFTools.instance, worldObj, ForgeChunkManager.Type.NORMAL);
                if (ticket == null) {
                    // Chunk is not loaded and we can't get a ticket.
                    return false;
                }
            }

            ChunkCoordIntPair pair = new ChunkCoordIntPair(cx, cz);
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


    private void nextLocation() {
        if (scan != null) {
            int x = scan.getX();
            int y = scan.getY();
            int z = scan.getZ();

            if (getCardType() == ShapeCardItem.CARD_SPACE) {
                nextLocationNormal(x, y, z);
            } else {
                nextLocationQuarry(x, y, z);
            }
        }
    }

    private void nextLocationQuarry(int x, int y, int z) {
        if (x >= maxBox.getX() || ((x+1) % 16 == 0)) {
            if (z >= maxBox.getZ() || ((z+1) % 16 == 0)) {
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
        return BuilderContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return BuilderContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return BuilderContainer.factory.isOutputSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (index == BuilderContainer.SLOT_TAB && inventoryHelper.getStackInSlot(index) != null && amount > 0) {
            // Restart if we go from having a stack to not having stack or the other way around.
            refreshSettings();
        }
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == BuilderContainer.SLOT_TAB && ((stack == null && inventoryHelper.getStackInSlot(index) != null) || (stack != null && inventoryHelper.getStackInSlot(index) == null))) {
            // Restart if we go from having a stack to not having stack or the other way around.
            refreshSettings();
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
        cardType = ShapeCardItem.CARD_UNKNOWN;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getByte("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        mode = tagCompound.getInteger("mode");
        anchor = tagCompound.getInteger("anchor");
        rotate = tagCompound.getInteger("rotate");
        silent = tagCompound.getBoolean("silent");
        supportMode = tagCompound.getBoolean("support");
        entityMode = tagCompound.getBoolean("entityMode");
        loopMode = tagCompound.getBoolean("loopMode");
        scan = BlockPosTools.readFromNBT(tagCompound, "scan");
        minBox = BlockPosTools.readFromNBT(tagCompound, "minBox");
        maxBox = BlockPosTools.readFromNBT(tagCompound, "maxBox");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setByte("powered", (byte) powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("mode", mode);
        tagCompound.setInteger("anchor", anchor);
        tagCompound.setInteger("rotate", rotate);
        tagCompound.setBoolean("silent", silent);
        tagCompound.setBoolean("support", supportMode);
        tagCompound.setBoolean("entityMode", entityMode);
        tagCompound.setBoolean("loopMode", loopMode);
        BlockPosTools.writeToNBT(tagCompound, "scan", scan);
        BlockPosTools.writeToNBT(tagCompound, "minBox", minBox);
        BlockPosTools.writeToNBT(tagCompound, "maxBox", maxBox);
    }

    // Request the current scan level.
    public void requestCurrentLevel() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(RFTools.MODID, getPos(),
                CMD_GETLEVEL,
                CLIENTCMD_GETLEVEL));
    }

    public static int getCurrentLevel() {
        return currentLevel;
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETMODE.equals(command)) {
            setMode(args.get("mode").getInteger());
            return true;
        } else if (CMD_SETANCHOR.equals(command)) {
            setAnchor(args.get("anchor").getInteger());
            return true;
        } else if (CMD_SETROTATE.equals(command)) {
            setRotate(args.get("rotate").getInteger());
            return true;
        } else if (CMD_SETSILENT.equals(command)) {
            setSilent(args.get("silent").getBoolean());
            return true;
        } else if (CMD_SETSUPPORT.equals(command)) {
            setSupportMode(args.get("support").getBoolean());
            return true;
        } else if (CMD_SETENTITIES.equals(command)) {
            setEntityMode(args.get("entities").getBoolean());
            return true;
        } else if (CMD_SETLOOP.equals(command)) {
            setLoopMode(args.get("loop").getBoolean());
            return true;
        }
        return false;
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETLEVEL.equals(command)) {
            return scan == null ? -1 : scan.getY();
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETLEVEL.equals(command)) {
            currentLevel = result;
            return true;
        }
        return false;
    }


}
