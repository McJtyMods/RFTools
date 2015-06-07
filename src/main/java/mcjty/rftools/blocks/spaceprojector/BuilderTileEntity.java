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
import mcjty.container.InventoryHelper;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.blocks.RFToolsTools;
import mcjty.rftools.network.Argument;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

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

    private int powered = 0;
    private int tickCounter = 0;

    private boolean boxValid = false;
    private Coordinate minBox = null;
    private Coordinate maxBox = null;
    private Coordinate scan = null;
    private int projDx;
    private int projDy;
    private int projDz;

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
        return new String[] { "hasCard", "getMode", "setMode", "getRotate", "setRotate", "getAnchor", "setAnchor", "getSupportMode", "setSupportMode" };
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

    private NBTTagCompound hasCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(0);
        if (itemStack == null || itemStack.stackSize == 0) {
            return null;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound;
    }

    private void makeSupportBlocks() {
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
                        int error = 0;
                        if (mode != MODE_COPY) {
                            TileEntity srcTileEntity = world.getTileEntity(src.getX(), src.getY(), src.getZ());
                            TileEntity dstTileEntity = worldObj.getTileEntity(dest.getX(), dest.getY(), dest.getZ());
                            if ((!isMovable(srcBlock, srcTileEntity)) || (!isMovable(dstBlock, dstTileEntity))) {
                                error = 1;
                            }
                        }
                        if (isEmpty(srcBlock) && !isEmpty(dstBlock)) {
                            world.setBlock(src.getX(), src.getY(), src.getZ(), SpaceProjectorSetup.supportBlock, error, 3);
                        }
                        if (isEmpty(dstBlock) && !isEmpty(srcBlock)) {
                            worldObj.setBlock(dest.getX(), dest.getY(), dest.getZ(), SpaceProjectorSetup.supportBlock, error, 3);
                        }
                    }
                }
            }
        }
    }

    public void clearSupportBlocks() {
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
        this.mode = mode;
        scan = minBox;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
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

        createProjection(chamberChannel);

        scan = minCorner;
        minBox = minCorner;
        maxBox = maxCorner;
    }

    @Override
    protected void checkStateServer() {
        if (powered == 0) {
            return;
        }

        SpaceChamberRepository.SpaceChamberChannel chamberChannel = calculateBox();
        if (chamberChannel == null) return;

        if (scan == null) {
            return;
        }

        float factor = getInfusedFactor();
        tickCounter++;
        if (tickCounter <= ((1.0-factor)*4)) {
            return;
        }
        tickCounter = 0;

        int dimension = chamberChannel.getDimension();
        World world = DimensionManager.getWorld(dimension);
        if (world == null) {
            // The other location must be loaded.
            return;
        }

        handleBlock(world);
        if (factor > .95 && scan != null) {
            handleBlock(world);
        }
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

    private void handleBlock(World world) {
        float factor = getInfusedFactor();

        int rf = getEnergyStored(ForgeDirection.DOWN);
        int rfNeeded = (int) (SpaceProjectorConfiguration.builderRfPerOperation * (4.0f - factor) / 4.0f);

        if (rfNeeded > rf) {
            // Not enough energy.
            return;
        }

        Coordinate dest = sourceToDest(scan);
        int x = scan.getX();
        int y = scan.getY();
        int z = scan.getZ();
        int destX = dest.getX();
        int destY = dest.getY();
        int destZ = dest.getZ();
        boolean success = false;
        switch (mode) {
            case MODE_COPY:
                success = copyBlock(world, x, y, z, worldObj, destX, destY, destZ);
                break;
            case MODE_MOVE:
                success = moveBlock(world, x, y, z, worldObj, destX, destY, destZ);
                break;
            case MODE_BACK:
                success = moveBlock(worldObj, destX, destY, destZ, world, x, y, z);
                break;
            case MODE_SWAP:
                success = swapBlock(world, x, y, z, worldObj, destX, destY, destZ);
                break;
        }

        if (success) {
            consumeEnergy(rfNeeded);
        }

        nextLocation();
    }

    private boolean findAndConsumeBlock(IInventory inventory, Block block, int meta) {
        for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0 && stack.getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                if (itemBlock.field_150939_a == block && (meta == -1 || stack.getItemDamage() == meta)) {
                    inventory.decrStackSize(i, 1);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean consumeBlock(Block block, int meta) {
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord+1, zCoord);
        if (te instanceof IInventory) {
            if (findAndConsumeBlock((IInventory) te, block, meta)) {
                return true;
            }
        }
        te = worldObj.getTileEntity(xCoord, yCoord-1, zCoord);
        if (te instanceof IInventory) {
            if (findAndConsumeBlock((IInventory) te, block, meta)) {
                return true;
            }
        }
        if (meta != -1) {
            // Try a second time with meta equal to -1 (which means to ignore meta).
            return consumeBlock(block, -1);
        }
        return false;
    }

    private boolean isMovable(Block block, TileEntity tileEntity) {
        if (tileEntity != null && SpaceProjectorConfiguration.ignoreTileEntities) {
            return false;
        }
//        System.out.println("block.getUnlocalizedName() = " + block.getUnlocalizedName());
        return true;
    }

    // True if this block can just be overwritten (i.e. are or support block)
    private boolean isEmpty(Block block) {
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

    private boolean copyBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        Block destBlock = destWorld.getBlock(destX, destY, destZ);
        if (isEmpty(destBlock)) {
            Block origBlock = world.getBlock(x, y, z);
            int origMeta = world.getBlockMetadata(x, y, z);
            if (origBlock == null || origBlock.getMaterial() == Material.air) {
                return false;
            }
            if (!consumeBlock(origBlock, origMeta)) {
                return false;
            }
            destWorld.setBlock(destX, destY, destZ, origBlock, origMeta, 3);
            destWorld.setBlockMetadataWithNotify(destX, destY, destZ, origMeta, 3);
            if (!silent) {
                RFToolsTools.playSound(destWorld, origBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }
            return true;
        }
        return false;
    }

    private boolean moveBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        Block destBlock = destWorld.getBlock(destX, destY, destZ);
        if (isEmpty(destBlock)) {
            Block origBlock = world.getBlock(x, y, z);
            if (isEmpty(origBlock)) {
                return false;
            }
            TileEntity origTileEntity = world.getTileEntity(x, y, z);
            if (!isMovable(origBlock, origTileEntity)) {
                return false;
            }
            int origMeta = world.getBlockMetadata(x, y, z);
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
            return true;
        }
        return false;
    }

    private boolean swapBlock(World world, int x, int y, int z, World destWorld, int destX, int destY, int destZ) {
        Block srcBlock = world.getBlock(x, y, z);
        TileEntity srcTileEntity = world.getTileEntity(x, y, z);

        Block dstBlock = destWorld.getBlock(destX, destY, destZ);
        TileEntity dstTileEntity = destWorld.getTileEntity(destX, destY, destZ);

        if ((!isMovable(srcBlock, srcTileEntity)) || !isMovable(dstBlock, dstTileEntity)) {
            return false;
        }

        int srcMeta = world.getBlockMetadata(x, y, z);
        int dstMeta = destWorld.getBlockMetadata(destX, destY, destZ);

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
            if (srcBlock != null) {
                RFToolsTools.playSound(world, srcBlock.stepSound.getBreakSound(), x, y, z, 1.0f, 1.0f);
            }
            if (dstBlock != null) {
                RFToolsTools.playSound(destWorld, dstBlock.stepSound.getBreakSound(), destX, destY, destZ, 1.0f, 1.0f);
            }
        }

        return true;
    }

    private Coordinate sourceToDest(Coordinate source) {
        Coordinate c = rotate(source);
        return new Coordinate(c.getX() + projDx, c.getY() + projDy, c.getZ() + projDz);
    }

    private void nextLocation() {
        if (scan != null) {
            int x = scan.getX();
            int y = scan.getY();
            int z = scan.getZ();
            if (x >= maxBox.getX()) {
                if (z >= maxBox.getZ()) {
                    if (y >= maxBox.getY()) {
                        if (mode != MODE_SWAP) {
                            // We don't restart in swap mode.
                            scan = minBox;
                        }
                    } else {
                        scan = new Coordinate(minBox.getX(), y+1, minBox.getZ());
                    }
                } else {
                    scan = new Coordinate(minBox.getX(), y, z+1);
                }
            } else {
                scan = new Coordinate(x+1, y, z);
            }
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
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
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
        return true;
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
        }
        return false;
    }

}
