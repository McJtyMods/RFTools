package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class PearlInjectorTileEntity extends GenericTileEntity implements DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PearlInjectorContainer.factory, PearlInjectorContainer.BUFFER_SIZE);

    // For pulse detection.
    private boolean prevIn = false;

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    public EndergenicTileEntity findEndergenicTileEntity() {
        IBlockState state = getWorld().getBlockState(getPos());
        EnumFacing k = OrientationTools.getOrientation(state);
        EndergenicTileEntity te = getEndergenicGeneratorAt(k.getOpposite());
        if (te != null) {
            return te;
        }
        return getEndergenicGeneratorAt(EnumFacing.UP);
    }

    private EndergenicTileEntity getEndergenicGeneratorAt(EnumFacing k) {
        BlockPos o = getPos().offset(k);
        TileEntity te = getWorld().getTileEntity(o);
        if (te instanceof EndergenicTileEntity) {
            return (EndergenicTileEntity) te;
        }
        return null;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        boolean pulse = (powerLevel > 0) && !prevIn;
        if (prevIn == powerLevel > 0) {
            return;
        }
        prevIn = powerLevel > 0;

        if (pulse) {
            injectPearl();
        }
        markDirty();
    }

    private boolean takePearl() {
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            if (!stack.isEmpty() && Items.ENDER_PEARL.equals(stack.getItem()) && stack.getCount() > 0) {
                decrStackSize(i, 1);
                return true;
            }
        }
        return false;
    }

    public void injectPearl() {
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
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return Items.ENDER_PEARL.equals(stack.getItem());
    }

    private int[] accessibleSlots;

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        if (accessibleSlots == null) {
            accessibleSlots = new int[PearlInjectorContainer.BUFFER_SIZE];
            for (int i = 0 ; i < PearlInjectorContainer.BUFFER_SIZE ; i++) {
                accessibleSlots[i] = i;
            }
        }
        return accessibleSlots;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index, itemStackIn);
    }
}
