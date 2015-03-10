package com.mcjty.rftools.blocks.environmental;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import com.mcjty.rftools.network.Argument;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnvironmentalControllerTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_SETRADIUS = "setRadius";
    public static final String CMD_SETBOUNDS = "setBounds";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, EnvironmentalControllerContainer.factory, EnvironmentalControllerContainer.ENV_MODULES);

    // Cached server modules
    private List<EnvironmentModule> environmentModules = null;
    private int totalRfPerTick = 0;     // The total rf per tick for all modules.
    private int radius = 50;
    private int miny = 30;
    private int maxy = 70;
    private int volume = -1;
    private boolean active = false;

    private int powerTimeout = 0;

    public EnvironmentalControllerTileEntity() {
        super(EnvironmentalConfiguration.ENVIRONMENTAL_MAXENERGY, EnvironmentalConfiguration.ENVIRONMENTAL_RECEIVEPERTICK);
    }

    public boolean isActive() {
        return active;
    }

    public int getTotalRfPerTick() {
        if (environmentModules == null) {
            getEnvironmentModules();
        }
        return (int) (totalRfPerTick * (4.0f - getInfusedFactor()) / 4.0f);
    }

    public int getVolume() {
        if (volume == -1) {
            volume = (int) ((radius * radius * Math.PI) * (maxy-miny + 1));
        }
        return volume;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
        volume = -1;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getMiny() {
        return miny;
    }

    public void setMiny(int miny) {
        this.miny = miny;
        volume = -1;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public int getMaxy() {
        return maxy;
    }

    public void setMaxy(int maxy) {
        this.maxy = maxy;
        volume = -1;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    protected void checkStateServer() {
        if (powerTimeout > 0) {
            powerTimeout--;
            return;
        }

        int rf = getEnergyStored(ForgeDirection.DOWN);

        getEnvironmentModules();

        int rfNeeded = getTotalRfPerTick();
        if (rfNeeded > rf || environmentModules.isEmpty()) {
            for (EnvironmentModule module : environmentModules) {
                module.activate(false);
            }
            powerTimeout = 20;
            if (active) {
                active = false;
                markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        } else {
            extractEnergy(ForgeDirection.DOWN, rfNeeded, false);
            for (EnvironmentModule module : environmentModules) {
                module.activate(true);
                module.tick(worldObj, xCoord, yCoord, zCoord, radius, miny, maxy);
            }
            if (!active) {
                active = true;
                markDirty();
                worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }
        }
    }

    // This is called server side.
    public List<EnvironmentModule> getEnvironmentModules() {
        if (environmentModules == null) {
            int volume = getVolume();
            totalRfPerTick = 0;
            environmentModules = new ArrayList<EnvironmentModule>();
            for (ItemStack itemStack : inventoryHelper.getStacks()) {
                if (itemStack != null && itemStack.getItem() instanceof EnvModuleProvider) {
                    EnvModuleProvider moduleProvider = (EnvModuleProvider) itemStack.getItem();
                    Class<? extends EnvironmentModule> moduleClass = moduleProvider.getServerEnvironmentModule();
                    EnvironmentModule environmentModule;
                    try {
                        environmentModule = moduleClass.newInstance();
                    } catch (InstantiationException e) {
                        RFTools.log("Failed to instantiate controller module!");
                        continue;
                    } catch (IllegalAccessException e) {
                        RFTools.log("Failed to instantiate controller module!");
                        continue;
                    }
                    environmentModules.add(environmentModule);
                    totalRfPerTick += (int) (environmentModule.getRfPerTick() * volume);
                }
            }

        }
        return environmentModules;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return EnvironmentalControllerContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, int side) {
        return EnvironmentalControllerContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, int side) {
        return EnvironmentalControllerContainer.factory.isOutputSlot(index);
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
        environmentModules = null;
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        environmentModules = null;
    }

    @Override
    public String getInventoryName() {
        return "Environmental Inventory";
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
        totalRfPerTick = tagCompound.getInteger("rfPerTick");
        active = tagCompound.getBoolean("active");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        radius = tagCompound.getInteger("radius");
        miny = tagCompound.getInteger("miny");
        maxy = tagCompound.getInteger("maxy");
        volume = -1;
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < bufferTagList.tagCount(); i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i + EnvironmentalControllerContainer.SLOT_MODULES] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
        }
        environmentModules = null;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("rfPerTick", totalRfPerTick);
        tagCompound.setBoolean("active", active);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("radius", radius);
        tagCompound.setInteger("miny", miny);
        tagCompound.setInteger("maxy", maxy);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = EnvironmentalControllerContainer.SLOT_MODULES; i < inventoryHelper.getStacks().length; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETRADIUS.equals(command)) {
            setRadius(args.get("radius").getInteger());
            return true;
        } else if (CMD_SETBOUNDS.equals(command)) {
            int miny = args.get("miny").getInteger();
            int maxy = args.get("maxy").getInteger();
            setMiny(miny);
            setMaxy(maxy);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }
}
