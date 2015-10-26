package mcjty.rftools.blocks.spaceprojector;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketRequestIntegerFromServer;
import mcjty.lib.varia.BlockMeta;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.blocks.RFToolsTools;
import mcjty.rftools.blocks.teleporter.RfToolsTeleporter;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.items.ModItems;
import mcjty.rftools.items.shapecard.ShapeCardItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class BuilderTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory, SimpleComponent, IPeripheral {

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

    public static final String ROTATE_0 = "0�";
    public static final String ROTATE_90 = "90�";
    public static final String ROTATE_180 = "180�";
    public static final String ROTATE_270 = "270�";

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
    private Coordinate minBox = null;
    private Coordinate maxBox = null;
    private Coordinate scan = null;
    private int projDx;
    private int projDy;
    private int projDz;

    private int cardType = ShapeCardItem.CARD_UNKNOWN; // One of the card types out of ShapeCardItem.CARD_...

    // Cached set of blocks that we need to build in shaped mode
    private Set<Coordinate> cachedBlocks = null;

    public BuilderTileEntity() {
        super(SpaceProjectorConfiguration.BUILDER_MAXENERGY, SpaceProjectorConfiguration.BUILDER_RECEIVEPERTICK);
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return COMPONENT_NAME;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return new String[] { "hasCard", "getMode", "setMode", "getRotate", "setRotate", "getAnchor", "setAnchor", "getSupportMode", "setSupportMode", "getEntityMode", "setEntityMode",
            "getLoopMode", "setLoopMode" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] { hasCard() != null };
            case 1: return new Object[] { getMode() };
            case 2: setMode(((Double) arguments[0]).intValue()); return null;
            case 3: return new Object[] { getRotate() };
            case 4: setRotate(((Double) arguments[0]).intValue()); return null;
            case 5: return new Object[] { getAnchor() };
            case 6: setAnchor(((Double) arguments[0]).intValue()); return null;
            case 7: return new Object[] { hasSupportMode() };
            case 8: setSupportMode(((Double) arguments[0]).intValue() > 0); return null;
            case 9: return new Object[] { hasEntityMode() };
            case 10: setEntityMode(((Double) arguments[0]).intValue() > 0); return null;
            case 11: return new Object[] { hasLoopMode() };
            case 12: setLoopMode(((Double) arguments[0]).intValue() > 0); return null;
        }
        return new Object[0];
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void detach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public boolean equals(IPeripheral other) {
        return false;
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] hasCard(Context context, Arguments args) throws Exception {
        return new Object[] { hasCard() != null };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getMode(Context context, Arguments args) throws Exception {
        return new Object[] { getMode() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setMode(Context context, Arguments args) throws Exception {
        int mode = args.checkInteger(0);
        setMode(mode);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getRotate(Context context, Arguments args) throws Exception {
        return new Object[] { getRotate()};
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setRotate(Context context, Arguments args) throws Exception {
        Integer angle = args.checkInteger(0);
        setRotate(angle);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getAnchor(Context context, Arguments args) throws Exception {
        return new Object[] { getAnchor()};
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setAnchor(Context context, Arguments args) throws Exception {
        Integer angle = args.checkInteger(0);
        setAnchor(angle);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getSupportMode(Context context, Arguments args) throws Exception {
        return new Object[] { hasSupportMode()};
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setSupportMode(Context context, Arguments args) throws Exception {
        boolean support = args.checkBoolean(0);
        setSupportMode(support);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getEntityMode(Context context, Arguments args) throws Exception {
        return new Object[] { hasEntityMode()};
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setEntityMode(Context context, Arguments args) throws Exception {
        boolean ent = args.checkBoolean(0);
        setEntityMode(ent);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getLoopMode(Context context, Arguments args) throws Exception {
        return new Object[] { hasLoopMode()};
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setLoopMode(Context context, Arguments args) throws Exception {
        boolean ent = args.checkBoolean(0);
        setLoopMode(ent);
        return null;
    }

    private boolean isShapeCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        if (itemStack == null || itemStack.stackSize == 0) {
            return false;
        }
        return itemStack.getItem() == ModItems.shapeCardItem;
    }

    private NBTTagCompound hasCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        if (itemStack == null || itemStack.stackSize == 0) {
            return null;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound;
    }

    private void makeSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        Coordinate dimension = ShapeCardItem.getDimension(shapeCard);
        Coordinate offset = ShapeCardItem.getOffset(shapeCard);
        ShapeCardItem.Shape shape = ShapeCardItem.getShape(shapeCard);
        List<Coordinate> blocks = new ArrayList<Coordinate>();
        ShapeCardItem.composeShape(shape, worldObj, getCoordinate(), dimension, offset, blocks, SpaceProjectorConfiguration.maxSpaceChamberDimension*SpaceProjectorConfiguration.maxSpaceChamberDimension*SpaceProjectorConfiguration.maxSpaceChamberDimension, false);
        for (Coordinate block : blocks) {
            if (worldObj.isAirBlock(block.getX(), block.getY(), block.getZ())) {
                int error = SupportBlock.STATUS_OK;
                worldObj.setBlock(block.getX(), block.getY(), block.getZ(), SpaceProjectorSetup.supportBlock, error, 3);
                worldObj.setBlockMetadataWithNotify(block.getX(), block.getY(), block.getZ(), error, 3);
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
                        Coordinate src = new Coordinate(x, y, z);
                        Coordinate dest = sourceToDest(src);
                        Block srcBlock = world.getBlock(src.getX(), src.getY(), src.getZ());
                        Block dstBlock = worldObj.getBlock(dest.getX(), dest.getY(), dest.getZ());
                        int error = SupportBlock.STATUS_OK;
                        if (mode != MODE_COPY) {
                            TileEntity srcTileEntity = world.getTileEntity(src.getX(), src.getY(), src.getZ());
                            TileEntity dstTileEntity = worldObj.getTileEntity(dest.getX(), dest.getY(), dest.getZ());

                            int error1 = isMovable(world, src.getX(), src.getY(), src.getZ(), srcBlock, srcTileEntity);
                            int error2 = isMovable(worldObj, dest.getX(), dest.getY(), dest.getZ(), dstBlock, dstTileEntity);
                            error = Math.max(error1, error2);
                        }
                        if (isEmpty(srcBlock) && !isEmpty(dstBlock)) {
                            world.setBlock(src.getX(), src.getY(), src.getZ(), SpaceProjectorSetup.supportBlock, error, 3);
                            world.setBlockMetadataWithNotify(src.getX(), src.getY(), src.getZ(), error, 3);
                        }
                        if (isEmpty(dstBlock) && !isEmpty(srcBlock)) {
                            worldObj.setBlock(dest.getX(), dest.getY(), dest.getZ(), SpaceProjectorSetup.supportBlock, error, 3);
                            worldObj.setBlockMetadataWithNotify(dest.getX(), dest.getY(), dest.getZ(), error, 3);
                        }
                    }
                }
            }
        }
    }

    private void clearSupportBlocksShaped() {
        ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        Coordinate dimension = ShapeCardItem.getDimension(shapeCard);
        Coordinate offset = ShapeCardItem.getOffset(shapeCard);
        ShapeCardItem.Shape shape = ShapeCardItem.getShape(shapeCard);
        List<Coordinate> blocks = new ArrayList<Coordinate>();
        ShapeCardItem.composeShape(shape, worldObj, getCoordinate(), dimension, offset, blocks, SpaceProjectorConfiguration.maxSpaceChamberDimension*SpaceProjectorConfiguration.maxSpaceChamberDimension*SpaceProjectorConfiguration.maxSpaceChamberDimension, false);
        for (Coordinate block : blocks) {
            if (worldObj.getBlock(block.getX(), block.getY(), block.getZ()) == SpaceProjectorSetup.supportBlock) {
                worldObj.setBlockToAir(block.getX(), block.getY(), block.getZ());
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
                        Coordinate src = new Coordinate(x, y, z);
                        if (world != null) {
                            Block srcBlock = world.getBlock(src.getX(), src.getY(), src.getZ());
                            if (srcBlock == SpaceProjectorSetup.supportBlock) {
                                world.setBlockToAir(src.getX(), src.getY(), src.getZ());
                            }
                        }
                        Coordinate dest = sourceToDest(src);
                        Block dstBlock = worldObj.getBlock(dest.getX(), dest.getY(), dest.getZ());
                        if (dstBlock == SpaceProjectorSetup.supportBlock) {
                            worldObj.setBlockToAir(dest.getX(), dest.getY(), dest.getZ());
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
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean hasEntityMode() {
        return entityMode;
    }

    public void setEntityMode(boolean entityMode) {
        this.entityMode = entityMode;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean isSilent() {
        return silent;
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode != this.mode) {
            this.mode = mode;
            restartScan();
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
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
        if (supportMode) {
            makeSupportBlocks();
        }
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

    private Coordinate rotate(Coordinate c) {
        switch (rotate) {
            case 0: return c;
            case 1: return new Coordinate(-c.getZ(), c.getY(), c.getX());
            case 2: return new Coordinate(-c.getX(), c.getY(), -c.getZ());
            case 3: return new Coordinate(c.getZ(), c.getY(), -c.getX());
        }
        return c;
    }

    private void createProjection(SpaceChamberRepository.SpaceChamberChannel chamberChannel) {
        Coordinate minC = rotate(chamberChannel.getMinCorner());
        Coordinate maxC = rotate(chamberChannel.getMaxCorner());
        Coordinate minCorner = new Coordinate(Math.min(minC.getX(), maxC.getX()), Math.min(minC.getY(), maxC.getY()), Math.min(minC.getZ(), maxC.getZ()));
        Coordinate maxCorner = new Coordinate(Math.max(minC.getX(), maxC.getX()), Math.max(minC.getY(), maxC.getY()), Math.max(minC.getZ(), maxC.getZ()));

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection direction = BlockTools.getOrientationHoriz(meta);
        int spanX = maxCorner.getX() - minCorner.getX();
        int spanY = maxCorner.getY() - minCorner.getY();
        int spanZ = maxCorner.getZ() - minCorner.getZ();
        switch (direction) {
            case SOUTH:
                projDx = xCoord + ForgeDirection.NORTH.offsetX - minCorner.getX() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + ForgeDirection.NORTH.offsetZ - minCorner.getZ() - spanZ;
                break;
            case NORTH:
                projDx = xCoord + ForgeDirection.SOUTH.offsetX - minCorner.getX() - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                projDz = zCoord + ForgeDirection.SOUTH.offsetZ - minCorner.getZ();
                break;
            case WEST:
                projDx = xCoord + ForgeDirection.EAST.offsetX - minCorner.getX();
                projDz = zCoord + ForgeDirection.EAST.offsetZ - minCorner.getZ() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case EAST:
                projDx = xCoord + ForgeDirection.WEST.offsetX - minCorner.getX() - spanX;
                projDz = zCoord + ForgeDirection.WEST.offsetZ - minCorner.getZ() - spanZ + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case DOWN:
            case UP:
            case UNKNOWN:
                break;
        }
        projDy = yCoord - minCorner.getY() - ((anchor == ANCHOR_NE || anchor == ANCHOR_NW) ? spanY : 0);
    }

    private void calculateBox(NBTTagCompound cardCompound) {
        int channel = cardCompound.getInteger("channel");

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(worldObj);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        Coordinate minCorner = chamberChannel.getMinCorner();
        Coordinate maxCorner = chamberChannel.getMaxCorner();
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
        for (int i = 0 ; i < 8 + (factor * 20) ; i++) {
            if (scan != null) {
                handleBlockShaped();
            }
        }
    }


    @Override
    protected void checkStateServer() {
        if (powered == 0 && loopMode) {
            return;
        }

        if (scan == null) {
            return;
        }

        if (isShapeCard()) {
            if (powered == 0) {
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
        Coordinate dimension = ShapeCardItem.getDimension(shapeCard);
        Coordinate offset = ShapeCardItem.getOffset(shapeCard);

        Coordinate minCorner = ShapeCardItem.getMinCorner(getCoordinate(), dimension, offset);
        Coordinate maxCorner = ShapeCardItem.getMaxCorner(getCoordinate(), dimension, offset);

        if (boxValid) {
            // Double check if the box is indeed still valid.
            if (minCorner.equals(minBox) && maxCorner.equals(maxBox)) {
                return;
            }
        }

        boxValid = true;
        cardType = shapeCard.getItemDamage();

        cachedBlocks = null;
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

    private Set<Coordinate> getCachedBlocks() {
        if (cachedBlocks == null) {
            cachedBlocks = new HashSet<Coordinate>();
            ItemStack shapeCard = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
            ShapeCardItem.Shape shape = ShapeCardItem.getShape(shapeCard);
            Coordinate dimension = ShapeCardItem.getDimension(shapeCard);
            Coordinate offset = ShapeCardItem.getOffset(shapeCard);
            ShapeCardItem.composeShape(shape, worldObj, getCoordinate(), dimension, offset, cachedBlocks,
                    SpaceProjectorConfiguration.maxSpaceChamberDimension * SpaceProjectorConfiguration.maxSpaceChamberDimension * SpaceProjectorConfiguration.maxSpaceChamberDimension,
                    !ShapeCardItem.isNormalCard(shapeCard));
        }
        return cachedBlocks;
    }

    private void handleBlockShaped() {
        for (int i = 0 ; i < 100 ; i++) {
            if (scan == null) {
                return;
            }
            if (getCachedBlocks().contains(scan)) {
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
        ItemStack itemStack = inventoryHelper.getStackInSlot(BuilderContainer.SLOT_TAB);
        int rf = (int) (getEnergyStored(ForgeDirection.DOWN) * ShapeCardItem.getCostFactor(itemStack));
        int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerOperation * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return true;
        }

        int sx = scan.getX();
        int sy = scan.getY();
        int sz = scan.getZ();
        switch (getCardType()) {
            case ShapeCardItem.CARD_VOID: return voidBlock(rfNeeded, sx, sy, sz);
            case ShapeCardItem.CARD_QUARRY: return quarryBlock(rfNeeded, sx, sy, sz);
            case ShapeCardItem.CARD_QUARRY_SILK: return silkQuarryBlock(rfNeeded, sx, sy, sz);
            case ShapeCardItem.CARD_SHAPE: return buildBlock(rfNeeded, sx, sy, sz);
        }
        return true;
    }

    private boolean buildBlock(int rfNeeded, int sx, int sy, int sz) {
        if (isEmptyOrReplacable(worldObj, sx, sy, sz)) {
            BlockMeta block = consumeBlock(null, 0);
            if (block == null) {
                return true;    // We could not find a block. Wait
            }

            worldObj.setBlock(sx, sy, sz, block.getBlock(), block.getMeta(), 3);
            worldObj.setBlockMetadataWithNotify(sx, sy, sz, block.getMeta(), 3);
            if (!silent) {
                RFToolsTools.playSound(worldObj, block.getBlock().stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
            }

            consumeEnergy(rfNeeded);
        }
        return false;
    }

    private boolean silkQuarryBlock(int rfNeeded, int sx, int sy, int sz) {
        if (sx >= xCoord-1 && sx <= xCoord+1 && sy >= yCoord-1 && sy <= yCoord+1 && sz >= zCoord-1 && sz <= zCoord+1) {
            // Skip a 3x3x3 block around the builder.
            return false;
        }
        Block block = worldObj.getBlock(sx, sy, sz);
        if (isEmpty(block)) {
            return false;
        }
        if (block.getBlockHardness(worldObj, sx, sy, sz) >= 0) {
            int meta = worldObj.getBlockMetadata(sx, sy, sz);

            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
            if (block.canEntityDestroy(worldObj, sx, sy, sz, fakePlayer)) {
                List<ItemStack> drops;
                if (block.canSilkHarvest(worldObj, fakePlayer, sx, sy, sz, meta)) {
                    Item item = Item.getItemFromBlock(block);
                    int m = 0;
                    if (item != null && item.getHasSubtypes()) {
                        m = meta;
                    }
                    drops = Collections.singletonList(new ItemStack(item, 1, m));
                } else {
                    drops = block.getDrops(worldObj, sx, sy, sz, meta, 0);
                }
                if (checkAndInsertItems(drops)) {
                    worldObj.setBlockToAir(sx, sy, sz);
                    consumeEnergy(rfNeeded);
                    if (!silent) {
                        RFToolsTools.playSound(worldObj, block.stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
                    }
                } else {
                    return true;    // Not enough room. Wait
                }
            }
        }
        return false;
    }

    private boolean quarryBlock(int rfNeeded, int sx, int sy, int sz) {
        if (sx >= xCoord-1 && sx <= xCoord+1 && sy >= yCoord-1 && sy <= yCoord+1 && sz >= zCoord-1 && sz <= zCoord+1) {
            // Skip a 3x3x3 block around the builder.
            return false;
        }
        Block block = worldObj.getBlock(sx, sy, sz);
        if (isEmpty(block)) {
            return false;
        }
        if (block.getBlockHardness(worldObj, sx, sy, sz) >= 0) {
            int meta = worldObj.getBlockMetadata(sx, sy, sz);

            FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
            if (block.canEntityDestroy(worldObj, sx, sy, sz, fakePlayer)) {
                ArrayList<ItemStack> drops = block.getDrops(worldObj, sx, sy, sz, meta, 0);
                if (checkAndInsertItems(drops)) {
                    worldObj.setBlockToAir(sx, sy, sz);
                    consumeEnergy(rfNeeded);
                    if (!silent) {
                        RFToolsTools.playSound(worldObj, block.stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
                    }
                } else {
                    return true;    // Not enough room. Wait
                }
            }
        }
        return false;
    }

    private boolean voidBlock(int rfNeeded, int sx, int sy, int sz) {
        if (sx >= xCoord-1 && sx <= xCoord+1 && sy >= yCoord-1 && sy <= yCoord+1 && sz >= zCoord-1 && sz <= zCoord+1) {
            // Skip a 3x3x3 block around the builder.
            return false;
        }
        Block block = worldObj.getBlock(sx, sy, sz);
        if (block.getBlockHardness(worldObj, sx, sy, sz) >= 0) {
            if (!silent) {
                RFToolsTools.playSound(worldObj, block.stepSound.getBreakSound(), sx, sy, sz, 1.0f, 1.0f);
            }
            worldObj.setBlockToAir(sx, sy, sz);
            consumeEnergy(rfNeeded);
        }
        return false;
    }

    private void handleBlock(World world) {
        Coordinate dest = sourceToDest(scan);
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
            return new BlockMeta(itemBlock.field_150939_a, stack.getItemDamage());
        } else {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlock) {
                    ItemBlock itemBlock = (ItemBlock) stack.getItem();
                    if (itemBlock.field_150939_a == block && (meta == -1 || stack.getItemDamage() == meta)) {
                        inventory.decrStackSize(i, 1);
                        return new BlockMeta(itemBlock.field_150939_a, stack.getItemDamage());
                    }
                }
            }
        }
        return null;
    }

    private boolean checkAndInsertItems(List<ItemStack> items) {
        TileEntity teAbove = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
        boolean ok = false;
        if (teAbove instanceof IInventory) {
            ok = checkAndInsertItems(items, (IInventory) teAbove);
        }
        if (ok) {
            return true;
        }
        TileEntity teDown = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
        if (teDown instanceof IInventory) {
            ok = checkAndInsertItems(items, (IInventory) teDown);
        }
        return ok;
    }

    private boolean checkAndInsertItems(List<ItemStack> items, IInventory inventory) {
        Map<Integer, ItemStack> undo = new HashMap<Integer, ItemStack>();
        for (ItemStack item : items) {
            int remaining = InventoryHelper.mergeItemStackSafe(inventory, ForgeDirection.DOWN.ordinal(), item, 0, inventory.getSizeInventory(), undo);
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
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
        if (te instanceof IInventory) {
            BlockMeta b = findAndConsumeBlock((IInventory) te, block, meta);
            if (b != null) {
                return b;
            }
        }
        te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
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

    public static SpaceProjectorSetup.BlockInformation getBlockInformation(World world, int x, int y, int z, Block block, TileEntity tileEntity) {
        if (isEmpty(block)) {
            return SpaceProjectorSetup.BlockInformation.FREE;
        }

        FakePlayer fakePlayer = FakePlayerFactory.getMinecraft(DimensionManager.getWorld(0));
        if (!block.canEntityDestroy(world, x, y, z, fakePlayer)) {
            return SpaceProjectorSetup.BlockInformation.INVALID;
        }

        SpaceProjectorSetup.BlockInformation blockInformation = SpaceProjectorSetup.getBlockInformation(block);
        if (tileEntity != null) {
            switch (SpaceProjectorConfiguration.teMode) {
                case MOVE_FORBIDDEN:
                    return SpaceProjectorSetup.BlockInformation.INVALID;
                case MOVE_WHITELIST:
                    if (blockInformation == null || blockInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                        return SpaceProjectorSetup.BlockInformation.INVALID;
                    }
                    break;
                case MOVE_BLACKLIST:
                    if (blockInformation != null && blockInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                        return SpaceProjectorSetup.BlockInformation.INVALID;
                    }
                    break;
                case MOVE_ALLOWED:
                    break;
            }
        }
        if (blockInformation != null) {
            return blockInformation;
        }
        return SpaceProjectorSetup.BlockInformation.OK;
    }

    private int isMovable(World world, int x, int y, int z, Block block, TileEntity tileEntity) {
        return getBlockInformation(world, x, y, z, block, tileEntity).getBlockLevel();
    }

    public static boolean isEmptyOrReplacable(World world, int x, int y, int z) {
        Block block = world.getBlock(x, y, z);
        if (block.isReplaceable(world, x, y, z)) {
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
        if (block == SpaceProjectorSetup.supportBlock) {
            return true;
        }
        return false;
    }

    private void clearBlock(World world, int x, int y, int z) {
        if (supportMode) {
            world.setBlock(x, y, z, SpaceProjectorSetup.supportBlock, 0, 3);
        } else {
            world.setBlockToAir(x, y, z);
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

    private int rotateMeta(int meta, SpaceProjectorSetup.BlockInformation information, int rotMode) {
        switch (information.getRotateInfo()) {
            case SpaceProjectorSetup.BlockInformation.ROTATE_mfff:
                switch (rotMode) {
                    case 0: return meta;
                    case 1: {
                        ForgeDirection dir = ForgeDirection.values()[meta & 7];
                        return (meta & 8) | dir.getRotation(ForgeDirection.UP).ordinal();
                    }
                    case 2: {
                        ForgeDirection dir = ForgeDirection.values()[meta & 7];
                        return (meta & 8) | dir.getOpposite().ordinal();
                    }
                    case 3: {
                        ForgeDirection dir = ForgeDirection.values()[meta & 7];
                        return (meta & 8) | dir.getOpposite().getRotation(ForgeDirection.UP).ordinal();
                    }
                }
                break;
            case SpaceProjectorSetup.BlockInformation.ROTATE_mmmm:
                return meta;
        }
        return meta;
    }

    private void copyBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }

        if (isEmptyOrReplacable(destWorld, destX, destY, destZ)) {
            Block origBlock = world.getBlock(x, y, z);
            int origMeta = world.getBlockMetadata(x, y, z);
            if (origBlock == null || origBlock.getMaterial() == Material.air) {
                return;
            }
            if (consumeBlock(origBlock, origMeta) == null) {
                return;
            }

            SpaceProjectorSetup.BlockInformation information = getBlockInformation(world, x, y, z, origBlock, null);
            origMeta = rotateMeta(origMeta, information, rotate);

            destWorld.setBlock(destX, destY, destZ, origBlock, origMeta, 3);
            destWorld.setBlockMetadataWithNotify(destX, destY, destZ, origMeta, 3);
            if (!silent) {
                RFToolsTools.playSound(destWorld, origBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }

            consumeEnergy(rfNeeded);
        }
    }

    private double getDimensionCostFactor(World world, World destWorld) {
        return destWorld.provider.dimensionId == world.provider.dimensionId ? 1.0 : SpaceProjectorConfiguration.dimensionCostFactor;
    }

    private boolean consumeEntityEnergy(int rfNeeded, int rfNeededPlayer, Entity entity) {
        int rf = getEnergyStored(ForgeDirection.DOWN);
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
        int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerEntity * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        int rfNeededPlayer = (int) (SpaceProjectorConfiguration.builderRfPerPlayer * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);

        // Check for entities.
        List entities = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(x-.1, y-.1, z-.1, x + 1.1, y + 1.1, z + 1.1));
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
        int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerEntity * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);
        int rfNeededPlayer = (int) (SpaceProjectorConfiguration.builderRfPerPlayer * getDimensionCostFactor(world, destWorld) * (4.0f - getInfusedFactor()) / 4.0f);

        // Check for entities.
        List entitiesSrc = world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1));
        List entitiesDst = destWorld.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(destX, destY, destZ, destX + 1, destY + 1, destZ + 1));
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
            if (world.provider.dimensionId != destWorld.provider.dimensionId) {
                TeleportationTools.teleportToDimension((EntityPlayer) entity, destWorld.provider.dimensionId, newX, newY, newZ);
            }
            ((EntityPlayer) entity).setPositionAndUpdate(newX, newY, newZ);
        } else {
            if (world.provider.dimensionId != destWorld.provider.dimensionId) {
                MinecraftServer.getServer().getConfigurationManager().transferEntityToWorld(entity, destWorld.provider.dimensionId, (WorldServer) world, (WorldServer) destWorld,
                        new RfToolsTeleporter((WorldServer) destWorld, newX, newY, newZ));
            }
            entity.setPosition(newX, newY, newZ);
        }
    }

    private boolean isEntityInBlock(int x, int y, int z, Entity entity) {
        if (entity.posX >= x && entity.posX < x+1 && entity.posY >= y && entity.posY < y+1 && entity.posZ >= z && entity.posZ < z+1) {
            return true;
        }
        return false;
    }

    private void moveBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ, int rotMode) {
        Block destBlock = destWorld.getBlock(destX, destY, destZ);
        if (isEmpty(destBlock)) {
            Block origBlock = world.getBlock(x, y, z);
            if (isEmpty(origBlock)) {
                return;
            }
            TileEntity origTileEntity = world.getTileEntity(x, y, z);
            SpaceProjectorSetup.BlockInformation information = getBlockInformation(world, x, y, z, origBlock, origTileEntity);
            if (information.getBlockLevel() == SupportBlock.STATUS_ERROR) {
                return;
            }

            int rf = getEnergyStored(ForgeDirection.DOWN);
            int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * information.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
            if (rfNeeded > rf) {
                // Not enough energy.
                return;
            } else {
                consumeEnergy(rfNeeded);
            }

            int origMeta = world.getBlockMetadata(x, y, z);
            origMeta = rotateMeta(origMeta, information, rotMode);

            world.removeTileEntity(x, y, z);
            clearBlock(world, x, y, z);

            destWorld.setBlock(destX, destY, destZ, origBlock, origMeta, 3);
            destWorld.setBlockMetadataWithNotify(destX, destY, destZ, origMeta, 3);
            if (origTileEntity != null) {
                origTileEntity.validate();
                destWorld.setTileEntity(destX, destY, destZ, origTileEntity);
                origTileEntity.markDirty();
                destWorld.markBlockForUpdate(destX, destY, destZ);
            }
            if (!silent) {
                RFToolsTools.playSound(world, origBlock.stepSound.getBreakSound(), x, y, z, 1.0f, 1.0f);
                RFToolsTools.playSound(destWorld, origBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }
        }
    }

    private void swapBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        Block srcBlock = world.getBlock(x, y, z);
        TileEntity srcTileEntity = world.getTileEntity(x, y, z);

        Block dstBlock = destWorld.getBlock(destX, destY, destZ);
        TileEntity dstTileEntity = destWorld.getTileEntity(destX, destY, destZ);

        if (isEmpty(srcBlock) && isEmpty(dstBlock)) {
            return;
        }

        SpaceProjectorSetup.BlockInformation srcInformation = getBlockInformation(world, x, y, z, srcBlock, srcTileEntity);
        if (srcInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
            return;
        }

        SpaceProjectorSetup.BlockInformation dstInformation = getBlockInformation(destWorld, destX, destY, destZ, dstBlock, dstTileEntity);
        if (dstInformation.getBlockLevel() == SupportBlock.STATUS_ERROR) {
            return;
        }

        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * srcInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
        rfNeeded += (int) (SpaceProjectorConfiguration.builderRfPerOperation * getDimensionCostFactor(world, destWorld) * dstInformation.getCostFactor() * (4.0f - getInfusedFactor()) / 4.0f);
        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        } else {
            consumeEnergy(rfNeeded);
        }

        int srcMeta = world.getBlockMetadata(x, y, z);
        srcMeta = rotateMeta(srcMeta, srcInformation, oppositeRotate());
        int dstMeta = destWorld.getBlockMetadata(destX, destY, destZ);
        dstMeta = rotateMeta(dstMeta, dstInformation, rotate);

        world.removeTileEntity(x, y, z);
        world.setBlockToAir(x, y, z);
        destWorld.removeTileEntity(destX, destY, destZ);
        destWorld.setBlockToAir(destX, destY, destZ);

        destWorld.setBlock(destX, destY, destZ, srcBlock, srcMeta, 3);
        destWorld.setBlockMetadataWithNotify(destX, destY, destZ, srcMeta, 3);
        if (srcTileEntity != null) {
            srcTileEntity.validate();
            destWorld.setTileEntity(destX, destY, destZ, srcTileEntity);
            srcTileEntity.markDirty();
            destWorld.markBlockForUpdate(destX, destY, destZ);
        }

        world.setBlock(x, y, z, dstBlock, dstMeta, 3);
        world.setBlockMetadataWithNotify(x, y, z, dstMeta, 3);
        if (dstTileEntity != null) {
            dstTileEntity.validate();
            world.setTileEntity(x, y, z, dstTileEntity);
            dstTileEntity.markDirty();
            world.markBlockForUpdate(x, y, z);
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

    private Coordinate sourceToDest(Coordinate source) {
        Coordinate c = rotate(source);
        return new Coordinate(c.getX() + projDx, c.getY() + projDy, c.getZ() + projDz);
    }

    private void restartScan() {
        if (loopMode || (powered > 0 && scan == null)) {
            if (isShapeCard()) {
                calculateBoxShaped();
            } else {
                calculateBox();
            }
            cachedBlocks = null;
            if (ShapeCardItem.isQuarry(getCardType())) {
                // We start at the top for a quarry
                scan = new Coordinate(minBox.getX(), maxBox.getY(), minBox.getZ());
            } else {
                scan = minBox;
            }
        } else {
            scan = null;
        }
    }


    private void nextLocation() {
//        ForgeChunkManager.Ticket ticket = ForgeChunkManager.requestTicket(RFTools.instance, worldObj, ForgeChunkManager.Type.NORMAL);
//        ForgeChunkManager.forceChunk(ticket, new ChunkCoordIntPair(0, 0));

        if (scan != null) {
            int x = scan.getX();
            int y = scan.getY();
            int z = scan.getZ();

            if (ShapeCardItem.isQuarry(getCardType())) {
                nextLocationQuarry(x, y, z);
            } else {
                nextLocationNormal(x, y, z);
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
                        scan = new Coordinate(x, y, z);
                    } else if (z < maxBox.getZ()) {
                        x = minBox.getX();
                        z++;
                        y = maxBox.getY();
                        scan = new Coordinate(x, y, z);
                    } else {
                        restartScan();
                    }
                } else {
                    scan = new Coordinate((x >> 4) << 4, y - 1, (z >> 4) << 4);
                }
            } else {
                scan = new Coordinate((x >> 4) << 4, y, z + 1);
            }
        } else {
            scan = new Coordinate(x + 1, y, z);
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
                    scan = new Coordinate(minBox.getX(), y + 1, minBox.getZ());
                }
            } else {
                scan = new Coordinate(minBox.getX(), y, z + 1);
            }
        } else {
            scan = new Coordinate(x + 1, y, z);
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return BuilderContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return BuilderContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return BuilderContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
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
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
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
        boxValid = false;
        scan = null;
        cardType = ShapeCardItem.CARD_UNKNOWN;
    }

    @Override
    public String getInventoryName() {
        return "Builder";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
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
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

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
        readBufferFromNBT(tagCompound);
        mode = tagCompound.getInteger("mode");
        anchor = tagCompound.getInteger("anchor");
        rotate = tagCompound.getInteger("rotate");
        silent = tagCompound.getBoolean("silent");
        supportMode = tagCompound.getBoolean("support");
        entityMode = tagCompound.getBoolean("entityMode");
        loopMode = tagCompound.getBoolean("loopMode");
        scan = Coordinate.readFromNBT(tagCompound, "scan");
        minBox = Coordinate.readFromNBT(tagCompound, "minBox");
        maxBox = Coordinate.readFromNBT(tagCompound, "maxBox");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setByte("powered", (byte) powered);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("mode", mode);
        tagCompound.setInteger("anchor", anchor);
        tagCompound.setInteger("rotate", rotate);
        tagCompound.setBoolean("silent", silent);
        tagCompound.setBoolean("support", supportMode);
        tagCompound.setBoolean("entityMode", entityMode);
        tagCompound.setBoolean("loopMode", loopMode);
        Coordinate.writeToNBT(tagCompound, "scan", scan);
        Coordinate.writeToNBT(tagCompound, "minBox", minBox);
        Coordinate.writeToNBT(tagCompound, "maxBox", maxBox);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    // Request the current scan level.
    public void requestCurrentLevel() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
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
