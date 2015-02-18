package com.mcjty.rftools.blocks.screens.modules;

import com.mcjty.varia.Coordinate;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class ItemStackScreenModule implements ScreenModule {
    public static final int RFPERTICK = 0;
    private int slotIndex = 0;
    protected int dim = 0;
    protected Coordinate coordinate = Coordinate.INVALID;
    protected ScreenModuleHelper helper = new ScreenModuleHelper();


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
        ItemStack stack = inventory.getStackInSlot(slotIndex);
        if (stack == null) {
            return new Object[] { 0 };
        } else {
            return new Object[] { stack.stackSize };
        }
    }

    @Override
    public void setupFromNBT(NBTTagCompound tagCompound, int dim, int x, int y, int z) {

    }

    @Override
    public int getRfPerTick() {
        return RFPERTICK;
    }
}
