package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.BlockTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

public class PearlInjectorTileEntity extends GenericTileEntity implements IInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PearlInjectorContainer.factory, PearlInjectorContainer.BUFFER_SIZE);

    // For pulse detection.
    private boolean prevIn = false;

    private EndergenicTileEntity findEndergenicTileEntity() {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        ForgeDirection k = BlockTools.getOrientation(meta);
        EndergenicTileEntity te = getEndergenicGeneratorAt(k.getOpposite());
        if (te != null) {
            return te;
        }
        return getEndergenicGeneratorAt(ForgeDirection.UP);
    }

    private EndergenicTileEntity getEndergenicGeneratorAt(ForgeDirection k) {
        int x = xCoord + k.offsetX;
        int y = yCoord + k.offsetY;
        int z = zCoord + k.offsetZ;
        TileEntity te = worldObj.getTileEntity(x, y, z);
        if (te instanceof EndergenicTileEntity) {
            return (EndergenicTileEntity) te;
        }
        return null;
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean newvalue = BlockTools.getRedstoneSignal(meta);
        boolean pulse = newvalue && !prevIn;
        prevIn = newvalue;

        if (pulse) {
            injectPearl();
        }
    }

    private boolean takePearl() {
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            if (stack != null && Items.ender_pearl.equals(stack.getItem()) && stack.stackSize > 0) {
                decrStackSize(i, 1);
                return true;
            }
        }
        return false;
    }

    private void injectPearl() {
        EndergenicTileEntity endergen = findEndergenicTileEntity();
        if (endergen != null) {
            if (!takePearl()) {
                // No pearls in the inventory.
                return;
            }
            int mode = endergen.getChargingMode();
            // If the endergenic is already holding a pearl then this one is lost.
            if (mode != EndergenicTileEntity.CHARGE_HOLDING) {
                // It can accept a pearl.
                endergen.firePearlFromInjector();
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
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
            inventoryHelper.setStackInSlot(i+PearlInjectorContainer.SLOT_BUFFER, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("prevIn", prevIn);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
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
        return "Pearl Injector Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 16;
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
        return Items.ender_pearl.equals(stack.getItem());
    }

}
