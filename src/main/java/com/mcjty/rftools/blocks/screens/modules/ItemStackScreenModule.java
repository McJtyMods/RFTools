package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.varia.Coordinate;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ItemStackScreenModule implements ScreenModule {
    public static final int RFPERTICK = 4;
    private int slot1 = -1;
    private int slot2 = -1;
    private int slot3 = -1;
    private int slot4 = -1;
    protected int dim = 0;
    protected Coordinate coordinate = Coordinate.INVALID;


    @Override
    public Object[] getData(long millis) {
        World world = DimensionManager.getWorld(dim);
        if (world == null) {
            return null;
        }

        if (!world.getChunkProvider().chunkExists(coordinate.getX() >> 4, coordinate.getZ() >> 4)) {
            return null;
        }

        TileEntity te = world.getTileEntity(coordinate.getX(), coordinate.getY(), coordinate.getZ());
        if (!(te instanceof IInventory)) {
            return null;
        }
        IInventory inventory = (IInventory) te;
        ItemStack stack1 = getItemStack(inventory, slot1);
        ItemStack stack2 = getItemStack(inventory, slot2);
        ItemStack stack3 = getItemStack(inventory, slot3);
        ItemStack stack4 = getItemStack(inventory, slot4);
        return new Object[] { stack1, stack2, stack3, stack4 };
    }

    private ItemStack getItemStack(IInventory inventory, int slot) {
        if (slot == -1) {
            return null;
        }
        if (slot < inventory.getSizeInventory()) {
            return inventory.getStackInSlot(slot);
        } else {
            return null;
        }
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        if (tagCompound != null) {
            setupCoordinateFromNBT(tagCompound, dim, x, y, z);
            if (tagCompound.hasKey("slot1")) {
                slot1 = tagCompound.getInteger("slot1");
            }
            if (tagCompound.hasKey("slot2")) {
                slot2 = tagCompound.getInteger("slot2");
            }
            if (tagCompound.hasKey("slot3")) {
                slot3 = tagCompound.getInteger("slot3");
            }
            if (tagCompound.hasKey("slot4")) {
                slot4 = tagCompound.getInteger("slot4");
            }
        }
    }

    protected void setupCoordinateFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {
        coordinate = Coordinate.INVALID;
        if (tagCompound.hasKey("monitorx")) {
            this.dim = tagCompound.getInteger("dim");
            if (dim == this.dim) {
                Coordinate c = new Coordinate(tagCompound.getInteger("monitorx"), tagCompound.getInteger("monitory"), tagCompound.getInteger("monitorz"));
                int dx = Math.abs(c.getX() - x);
                int dy = Math.abs(c.getY() - y);
                int dz = Math.abs(c.getZ() - z);
                if (dx <= 64 && dy <= 64 && dz <= 64) {
                    coordinate = c;
                }
            }
        }
    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
