package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.description.DimensionDescriptor;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;
import java.util.Random;

public class DimensionBuilderTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_GETBUILDING = "getBuilding";
    public static final String CLIENTCMD_GETBUILDING = "getBuilding";

    private static int buildPercentage = 0;
    private int creative = -1;      // -1 is unknown

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionBuilderContainer.factory, 1);

    public DimensionBuilderTileEntity() {
        super(DimletConfiguration.BUILDER_MAXENERGY, DimletConfiguration.BUILDER_RECEIVEPERTICK);
    }

    private boolean isCreative() {
        if (creative == -1) {
            Block block = worldObj.getBlock(xCoord, yCoord, zCoord);
            if (ModBlocks.creativeDimensionBuilderBlock.equals(block)) {
                creative = 1;
            } else {
                creative = 0;
            }
        }
        return creative == 1;
    }

    @Override
    protected void checkStateServer() {
        ItemStack itemStack = inventoryHelper.getStacks()[0];
        if (itemStack == null || itemStack.stackSize == 0) {
            setState(-1, 0);
            return;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();

        if (tagCompound == null) {
            setState(-1, 0);
            return;
        }

        int ticksLeft = tagCompound.getInteger("ticksLeft");
        int tickCost = tagCompound.getInteger("tickCost");
        if (ticksLeft > 0) {
            ticksLeft = createDimensionTick(tagCompound, ticksLeft);
        } else {
            maintainDimensionTick(tagCompound);
        }

        setState(ticksLeft, tickCost);
    }

    static int counter = 20;

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
                if (RFTools.debugMode) {
                    RFTools.log("#################### id:" + id + ", rf:" + rf + ", energy:" + energy + ", max:" + maxEnergy);
                }
            }
            if (!isCreative()) {
                extractEnergy(ForgeDirection.DOWN, rf, false);
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
                extractEnergy(ForgeDirection.DOWN, createCost, false);
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

    private void setState(int ticksLeft, int tickCost) {
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
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
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

    // Request the building percentage from the server. This has to be called on the client side.
    public void requestBuildingPercentage() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
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
            ItemStack itemStack = inventoryHelper.getStacks()[0];
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

    public static int getBuildPercentage() {
        return buildPercentage;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }
}
