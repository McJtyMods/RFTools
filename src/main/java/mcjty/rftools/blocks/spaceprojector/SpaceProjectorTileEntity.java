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
import mcjty.lib.varia.Coordinate;
import mcjty.rftools.blocks.RedstoneMode;
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

import java.util.Map;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class SpaceProjectorTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory, SimpleComponent, IPeripheral {

    public static final String COMPONENT_NAME = "space_projector";

    public static final String CMD_RSMODE = "rsMode";
    public static final String CMD_PROJECT = "project";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, SpaceProjectorContainer.factory, 1);

    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private int powered = 0;
    private Coordinate minBox = null;
    private Coordinate maxBox = null;

    public SpaceProjectorTileEntity() {
        super(SpaceProjectorConfiguration.SPACEPROJECTOR_MAXENERGY, SpaceProjectorConfiguration.SPACEPROJECTOR_RECEIVEPERTICK);
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return COMPONENT_NAME;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return new String[] { "hasCard", "getRedstoneMode", "setRedstoneMode"  };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] { hasCard() != null };
            case 1: return new Object[] { getRedstoneMode().getDescription() };
            case 2: return setRedstoneMode((String) arguments[0]);
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
    public Object[] getRedstoneMode(Context context, Arguments args) throws Exception {
        return new Object[] { getRedstoneMode().getDescription() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] setRedstoneMode(Context context, Arguments args) throws Exception {
        String mode = args.checkString(0);
        return setRedstoneMode(mode);
    }

    private NBTTagCompound hasCard() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(0);
        if (itemStack == null || itemStack.stackSize == 0) {
            return null;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound;
    }


    @Override
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markDirty();
        }
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRedstoneMode(redstoneMode);
        return null;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    public void unproject() {
        if (minBox == null) {
            return;
        }

        for (int x = minBox.getX() ; x <= maxBox.getX() ; x++) {
            for (int y = minBox.getY() ; y <= maxBox.getY() ; y++) {
                for (int z = minBox.getZ() ; z <= maxBox.getZ() ; z++) {
                    Block block = worldObj.getBlock(x, y, z);
                    if (block.equals(SpaceProjectorSetup.proxyBlock)) {
                        worldObj.setBlockToAir(x, y, z);
                    }
                }
            }
        }
        minBox = null;
        maxBox = null;

        markDirty();
    }



    private void project() {
        if (minBox != null) {
            unproject();
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
        Coordinate minCorner = chamberChannel.getMinCorner();
        Coordinate maxCorner = chamberChannel.getMaxCorner();
        if (minCorner == null || maxCorner == null) {
            return;
        }

        int dimension = chamberChannel.getDimension();
        World world = DimensionManager.getWorld(dimension);
        int dx = xCoord + 1 - minCorner.getX();
        int dy = yCoord + 1 - minCorner.getY();
        int dz = zCoord + 1 - minCorner.getZ();

        minBox = new Coordinate(minCorner.getX() + dx, minCorner.getY() + dy, minCorner.getZ() + dz);
        maxBox = new Coordinate(maxCorner.getX() + dx, maxCorner.getY() + dy, maxCorner.getZ() + dz);

        for (int x = minCorner.getX() ; x <= maxCorner.getX() ; x++) {
            for (int y = minCorner.getY() ; y <= maxCorner.getY() ; y++) {
                for (int z = minCorner.getZ() ; z <= maxCorner.getZ() ; z++) {
                    Block block = world.getBlock(x, y, z);
                    if (block != null && !block.isAir(world, x, y, z)) {
                        worldObj.setBlock(dx + x, dy + y, dz + z, SpaceProjectorSetup.proxyBlock, world.getBlockMetadata(x, y, z), 3);
                        ProxyBlockTileEntity proxyBlockTileEntity = (ProxyBlockTileEntity) worldObj.getTileEntity(dx + x, dy + y, dz + z);
                        proxyBlockTileEntity.setCamoBlock(Block.blockRegistry.getIDForObject(block));
                        proxyBlockTileEntity.setOrigCoordinate(new Coordinate(x, y, z), dimension);
                    }
                }
            }
        }
        markDirty();
    }

    @Override
    protected void checkStateServer() {
        if (minBox != null) {
            NBTTagCompound tc = hasCard();
            if (tc == null) {
                unproject();
            }
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return SpaceProjectorContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return SpaceProjectorContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return SpaceProjectorContainer.factory.isOutputSlot(index);
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
        return "Space Projector";
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
        minBox = Coordinate.readFromNBT(tagCompound, "minBox");
        maxBox = Coordinate.readFromNBT(tagCompound, "maxBox");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        int m = tagCompound.getByte("rsMode");
        redstoneMode = RedstoneMode.values()[m];
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
        Coordinate.writeToNBT(tagCompound, "minBox", minBox);
        Coordinate.writeToNBT(tagCompound, "maxBox", maxBox);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setByte("rsMode", (byte) redstoneMode.ordinal());
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
        if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRedstoneMode(RedstoneMode.getMode(m));
            return true;
        } else if (CMD_PROJECT.equals(command)) {
            project();
            return true;
        }
        return false;
    }

}
