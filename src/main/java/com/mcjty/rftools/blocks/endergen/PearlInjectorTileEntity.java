package com.mcjty.rftools.blocks.endergen;

import com.mcjty.entity.GenericTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class PearlInjectorTileEntity extends GenericTileEntity implements IInventory {

    private ItemStack stacks[] = new ItemStack[PearlInjectorContainerFactory.BUFFER_SIZE];

    private EndergenicTileEntity endergenicTileEntity = null;

    // For pulse detection.
    private boolean prevIn = false;

    private EndergenicTileEntity findEndergenicTileEntity() {
        if (endergenicTileEntity == null) {
            int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
            ForgeDirection k = BlockTools.getOrientation(meta);
            if (!getEndergenicGeneratorAt(k.getOpposite())) {
                getEndergenicGeneratorAt(ForgeDirection.UP);
            }
        }
        return endergenicTileEntity;
    }

    private boolean getEndergenicGeneratorAt(ForgeDirection k) {
        int x = xCoord + k.offsetX;
        int y = yCoord + k.offsetY;
        int z = zCoord + k.offsetZ;
        TileEntity te = worldObj.getTileEntity(x, y, z);
        if (te instanceof EndergenicTileEntity) {
            endergenicTileEntity = (EndergenicTileEntity) te;
            return true;
        }
        return false;
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
        for (ItemStack stack : stacks) {
            if (Items.ender_pearl.equals(stack.getItem()) && stack.stackSize > 0) {
                stack.stackSize--;
                markDirty();
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
            if (mode == EndergenicTileEntity.CHARGE_HOLDING) {
                System.out.println("injectPearl: pearl lost");
                // The endergenic is already holding a pearl. This one is lost.
                return;
            } else {
                // It can accept a pearl.
                System.out.println("injectPearl: firePearlFromInjector");
                endergen.firePearlFromInjector();
                return;
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("prevIn", prevIn);
    }

    @Override
    public int getSizeInventory() {
        return stacks.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return stacks[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        if (stacks[index] != null) {
            if (stacks[index].stackSize <= amount) {
                ItemStack old = stacks[index];
                stacks[index] = null;
                markDirty();
                return old;
            }
            ItemStack its = stacks[index].splitStack(amount);
            if (stacks[index].stackSize == 0) {
                stacks[index] = null;
            }
            markDirty();
            return its;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        stacks[index] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
        markDirty();
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
