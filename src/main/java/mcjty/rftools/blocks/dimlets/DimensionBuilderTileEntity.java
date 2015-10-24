package mcjty.rftools.blocks.dimlets;

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
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.RedstoneMode;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.dimension.description.DimensionDescriptor;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;
import java.util.Random;

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class DimensionBuilderTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory, SimpleComponent, IPeripheral {

    public static final String CMD_GETBUILDING = "getBuilding";
    public static final String CLIENTCMD_GETBUILDING = "getBuilding";
    public static final String CMD_RSMODE = "rsMode";

    public static final String COMPONENT_NAME = "dimension_builder";

    // For usage in the gui
    private static int buildPercentage = 0;

    private int creative = -1;      // -1 is unknown
    private RedstoneMode redstoneMode = RedstoneMode.REDSTONE_IGNORED;
    private int powered = 0;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionBuilderContainer.factory, 1);

    public DimensionBuilderTileEntity() {
        super(DimletConfiguration.BUILDER_MAXENERGY, DimletConfiguration.BUILDER_RECEIVEPERTICK);
    }

    private boolean isCreative() {
        if (creative == -1) {
            Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
            if (DimletSetup.creativeDimensionBuilderBlock.equals(block)) {
                creative = 1;
            } else {
                creative = 0;
            }
        }
        return creative == 1;
    }

    @Override
    protected void checkStateServer() {
        NBTTagCompound tagCompound = hasTab();
        if (tagCompound == null) {
            setState(-1);
            return;
        }

        if (redstoneMode != RedstoneMode.REDSTONE_IGNORED) {
            boolean rs = powered > 0;
            if (redstoneMode == RedstoneMode.REDSTONE_OFFREQUIRED) {
                if (rs) {
                    setState(-1);
                    return;
                }
            } else if (redstoneMode == RedstoneMode.REDSTONE_ONREQUIRED) {
                if (!rs) {
                    setState(-1);
                    return;
                }
            }
        }

        int ticksLeft = tagCompound.getInteger("ticksLeft");
        if (ticksLeft > 0) {
            ticksLeft = createDimensionTick(tagCompound, ticksLeft);
        } else {
            maintainDimensionTick(tagCompound);
        }

        setState(ticksLeft);
    }

    @Override
    public void setPowered(int powered) {
        if (this.powered != powered) {
            this.powered = powered;
            markDirty();
        }
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return COMPONENT_NAME;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return new String[] { "hasTab", "getBuildingPercentage", "getDimensionPower", "getRedstoneMode", "setRedstoneMode" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: return new Object[] { hasTab() != null };
            case 1: return getBuildingPercentage();
            case 2: return getDimensionPower();
            case 3: return new Object[] { getRedstoneMode().getDescription() };
            case 4: return setRedstoneMode((String) arguments[0]);

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
    public Object[] hasTab(Context context, Arguments args) throws Exception {
        return new Object[] { hasTab() != null };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getBuildingPercentage(Context context, Arguments args) throws Exception {
        return getBuildingPercentage();
    }

    private Object[] getBuildingPercentage() {
        NBTTagCompound tagCompound = hasTab();
        if (tagCompound != null) {
            int ticksLeft = tagCompound.getInteger("ticksLeft");
            int tickCost = tagCompound.getInteger("tickCost");
            int pct = (tickCost - ticksLeft) * 100 / tickCost;
            return new Object[] { pct };
        } else {
            return new Object[] { 0 };
        }
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getDimensionPower(Context context, Arguments args) throws Exception {
        return getDimensionPower();
    }

    private Object[] getDimensionPower() {
        NBTTagCompound tagCompound = hasTab();
        if (tagCompound != null) {
            int id = tagCompound.getInteger("id");
            int power = 0;
            if (id != 0) {
                DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(worldObj);
                power = dimensionStorage.getEnergyLevel(id);
            }
            return new Object[] { power };
        } else {
            return new Object[] { 0 };
        }
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

    private Object[] setRedstoneMode(String mode) {
        RedstoneMode redstoneMode = RedstoneMode.getMode(mode);
        if (redstoneMode == null) {
            throw new IllegalArgumentException("Not a valid mode");
        }
        setRedstoneMode(redstoneMode);
        return null;
    }


    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(RedstoneMode redstoneMode) {
        this.redstoneMode = redstoneMode;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    private NBTTagCompound hasTab() {
        ItemStack itemStack = inventoryHelper.getStackInSlot(0);
        if (itemStack == null || itemStack.stackSize == 0) {
            return null;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        return tagCompound;
    }

    private static int counter = 20;

    private void maintainDimensionTick(NBTTagCompound tagCompound) {
        int id = tagCompound.getInteger("id");

        if (id != 0) {
            DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(worldObj);
            int rf;
            if (isCreative()) {
                rf = DimletConfiguration.BUILDER_MAXENERGY;
            } else {
                rf = getEnergyStored(ForgeDirection.DOWN);
            }
            int energy = dimensionStorage.getEnergyLevel(id);
            int maxEnergy = DimletConfiguration.MAX_DIMENSION_POWER - energy;      // Max energy the dimension can still get.
            if (rf > maxEnergy) {
                rf = maxEnergy;
            }
            counter--;
            if (counter < 0) {
                counter = 20;
                if (Logging.debugMode) {
                    Logging.log("#################### id:" + id + ", rf:" + rf + ", energy:" + energy + ", max:" + maxEnergy);
                }
            }
            if (!isCreative()) {
                consumeEnergy(rf);
            }
            dimensionStorage.setEnergyLevel(id, energy + rf);
            dimensionStorage.save(worldObj);
        }
    }

    private static Random random = new Random();

    private int createDimensionTick(NBTTagCompound tagCompound, int ticksLeft) {
        int createCost = tagCompound.getInteger("rfCreateCost");
        createCost = (int) (createCost * (2.0f - getInfusedFactor()) / 2.0f);

        if (isCreative() || (getEnergyStored(ForgeDirection.DOWN) >= createCost)) {
            if (isCreative()) {
                ticksLeft = 0;
            } else {
                consumeEnergy(createCost);
                ticksLeft--;
                if (random.nextFloat() < getInfusedFactor()) {
                    // Randomly reduce another tick if the device is infused.
                    ticksLeft--;
                    if (ticksLeft < 0) {
                        ticksLeft = 0;
                    }
                }
            }
            tagCompound.setInteger("ticksLeft", ticksLeft);
            if (ticksLeft <= 0) {
                RfToolsDimensionManager manager = RfToolsDimensionManager.getDimensionManager(worldObj);
                DimensionDescriptor descriptor = new DimensionDescriptor(tagCompound);
                String name = tagCompound.getString("name");
                int id = manager.createNewDimension(worldObj, descriptor, name);
                tagCompound.setInteger("id", id);
            }
        }
        return ticksLeft;
    }

    private void setState(int ticksLeft) {
        int state = 0;
        if (ticksLeft == 0) {
            state = 0;
        } else if (ticksLeft == -1) {
            state = 1;
        } else if (((ticksLeft >> 2) & 1) == 0) {
            state = 2;
        } else {
            state = 3;
        }
        int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newmeta = BlockTools.setState(metadata, state);
        if (newmeta != metadata) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newmeta, 2);
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimensionBuilderContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimensionBuilderContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return DimensionBuilderContainer.factory.isOutputSlot(index);
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
        return "Builder Inventory";
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

    // Request the building percentage from the server. This has to be called on the client side.
    public void requestBuildingPercentage() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETBUILDING,
                CLIENTCMD_GETBUILDING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETBUILDING.equals(command)) {
            ItemStack itemStack = inventoryHelper.getStackInSlot(0);
            if (itemStack == null || itemStack.stackSize == 0) {
                return 0;
            } else {
                NBTTagCompound tagCompound = itemStack.getTagCompound();
                if (tagCompound == null) {
                    return 0;
                }
                int ticksLeft = tagCompound.getInteger("ticksLeft");
                int tickCost = tagCompound.getInteger("tickCost");
                return (tickCost - ticksLeft) * 100 / tickCost;
            }
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETBUILDING.equals(command)) {
            buildPercentage = result;
            return true;
        }
        return false;
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
        }
        return false;
    }

    public static int getBuildPercentage() {
        return buildPercentage;
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
}
