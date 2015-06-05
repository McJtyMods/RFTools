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
import mcjty.rftools.network.Argument;
import mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
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

    private InventoryHelper inventoryHelper = new InventoryHelper(this, BuilderContainer.factory, 1);

    public static final String MODE_MOVE = "Move";
    public static final String MODE_COPY = "Copy";
    public static final String MODE_SWAP = "Swap";
    public static final String MODE_BACK = "Back";

    public static final String ROTATE_0 = "0�";
    public static final String ROTATE_90 = "90�";
    public static final String ROTATE_180 = "180�";
    public static final String ROTATE_270 = "270�";

    public static final int ANCHOR_SW = 0;
    public static final int ANCHOR_SE = 1;
    public static final int ANCHOR_NW = 2;
    public static final int ANCHOR_NE = 3;

    private String mode = MODE_MOVE;
    private int rotate = 0;
    private int anchor = ANCHOR_SW;
    private int powered = 0;

    private boolean boxValid = false;
    private Coordinate minBox = null;
    private Coordinate maxBox = null;
    private Coordinate scan = null;
    private int projDx;
    private int projDy;
    private int proyDz;

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
        return new String[] { "hasCard", "getMode", "setMode", "getRotate", "setRotate", "getAnchor", "setAnchor" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] { hasCard() != null };
            case 1: return new Object[] { getMode() };
            case 2: setMode((String) arguments[0]); return null;
            case 3: return new Object[] { getRotate() };
            case 4: setRotate(((Double) arguments[0]).intValue()); return null;
            case 5: return new Object[] { getAnchor() };
            case 6: setAnchor(((Double) arguments[0]).intValue()); return null;
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
        String mode = args.checkString(0);
        setMode(mode);
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getRotate(Context context, Arguments args) throws Exception {
        return new Object[] { getRotate() };
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
        return new Object[] { getAnchor() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setAnchor(Context context, Arguments args) throws Exception {
        Integer angle = args.checkInteger(0);
        setAnchor(angle);
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

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getAnchor() {
        return anchor;
    }

    public void setAnchor(int anchor) {
        boxValid = false;
        this.anchor = anchor;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        boxValid = false;
        this.rotate = rotate;
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

    private void createProjection(SpaceChamberRepository.SpaceChamberChannel chamberChannel) {
        Coordinate minCorner = chamberChannel.getMinCorner();
        Coordinate maxCorner = chamberChannel.getMaxCorner();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection direction = BlockTools.getOrientationHoriz(meta);
        int spanX = maxCorner.getX() - minCorner.getX();
        int spanY = maxCorner.getY() - minCorner.getY();
        int spanZ = maxCorner.getZ() - minCorner.getZ();
        switch (direction) {
            case SOUTH:
                projDx = xCoord + ForgeDirection.NORTH.offsetX - minCorner.getX() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                proyDz = zCoord + ForgeDirection.NORTH.offsetZ - minCorner.getZ() - spanZ;
                break;
            case NORTH:
                projDx = xCoord + ForgeDirection.SOUTH.offsetX - minCorner.getX() - spanX + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanX : 0);
                proyDz = zCoord + ForgeDirection.SOUTH.offsetZ - minCorner.getZ();
                break;
            case WEST:
                projDx = xCoord + ForgeDirection.EAST.offsetX - minCorner.getX();
                proyDz = zCoord + ForgeDirection.EAST.offsetZ - minCorner.getZ() - ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
                break;
            case EAST:
                projDx = xCoord + ForgeDirection.WEST.offsetX - minCorner.getX() - spanX;
                proyDz = zCoord + ForgeDirection.WEST.offsetZ - minCorner.getZ() - spanZ + ((anchor == ANCHOR_NE || anchor == ANCHOR_SE) ? spanZ : 0);
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

        NBTTagCompound tc = hasCard();
        if (tc == null) {
            return;
        }

        int channel = tc.getInteger("channel");
        if (channel == -1) {
            return;
        }

        SpaceChamberRepository repository = SpaceChamberRepository.getChannels(worldObj);
        SpaceChamberRepository.SpaceChamberChannel chamberChannel = repository.getChannel(channel);
        if (chamberChannel == null) {
            return;
        }

        calculateBox(tc);

        if (!boxValid) {
            return;
        }

        if (scan == null) {
            return;
        }

        int dimension = chamberChannel.getDimension();
        World world = DimensionManager.getWorld(dimension);
        if (world == null) {
            // The other location must be loaded.
            return;
        }

        int x = scan.getX();
        int y = scan.getY();
        int z = scan.getZ();
        int destX = x + projDx;
        int destY = y + projDy;
        int destZ = z + proyDz;
        if (worldObj.isAirBlock(destX, destY, destZ)) {
            Block origBlock = world.getBlock(x, y, z);
            int origMeta = world.getBlockMetadata(x, y, z);
            worldObj.setBlock(destX, destY, destZ, origBlock, origMeta, 3);
            worldObj.setBlockMetadataWithNotify(destX, destY, destZ, origMeta, 3);
        }

        nextLocation();
    }

    private void nextLocation() {
        if (scan != null) {
            int x = scan.getX();
            int y = scan.getY();
            int z = scan.getZ();
            if (x >= maxBox.getX()) {
                if (y >= maxBox.getY()) {
                    if (z >= maxBox.getZ()) {
                        scan = minBox;
                    } else {
                        scan = new Coordinate(minBox.getX(), minBox.getY(), z+1);
                    }
                } else {
                    scan = new Coordinate(minBox.getX(), y+1, z);
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
        mode = tagCompound.getString("mode");
        anchor = tagCompound.getInteger("anchor");
        rotate = tagCompound.getInteger("rotate");
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
        tagCompound.setString("mode", mode);
        tagCompound.setInteger("anchor", anchor);
        tagCompound.setInteger("rotate", rotate);
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
            setMode(args.get("mode").getString());
            return true;
        } else if (CMD_SETANCHOR.equals(command)) {
            setAnchor(args.get("anchor").getInteger());
            return true;
        } else if (CMD_SETROTATE.equals(command)) {
            setRotate(args.get("rotate").getInteger());
            return true;
        }
        return false;
    }

}
