package mcjty.rftools.blocks.endergen;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.CustomSidedInvWrapper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class PearlInjectorTileEntity extends GenericTileEntity implements DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PearlInjectorContainer.factory, PearlInjectorContainer.BUFFER_SIZE);

    // For pulse detection.
    private boolean prevIn = false;
    private boolean powered = false;

    private EndergenicTileEntity findEndergenicTileEntity() {
        IBlockState state = worldObj.getBlockState(getPos());
        int meta = state.getBlock().getMetaFromState(state);
        EnumFacing k = BlockTools.getOrientation(meta);
        EndergenicTileEntity te = getEndergenicGeneratorAt(k.getOpposite());
        if (te != null) {
            return te;
        }
        return getEndergenicGeneratorAt(EnumFacing.UP);
    }

    private EndergenicTileEntity getEndergenicGeneratorAt(EnumFacing k) {
        BlockPos o = getPos().offset(k);
        TileEntity te = worldObj.getTileEntity(o);
        if (te instanceof EndergenicTileEntity) {
            return (EndergenicTileEntity) te;
        }
        return null;
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    @Override
    public void setPowered(int powered) {
        this.powered = powered > 0;
        markDirty();
    }

    private void checkStateServer() {
        boolean pulse = powered && !prevIn;
        if (prevIn == powered) {
            return;
        }
        prevIn = powered;

        if (pulse) {
            injectPearl();
        }
        markDirty();
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
        powered = tagCompound.getBoolean("powered");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setBoolean("powered", powered);
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
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return Items.ender_pearl.equals(stack.getItem());
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return PearlInjectorContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return isItemValidForSlot(index, itemStackIn);
    }

    IItemHandler invHandler = new CustomSidedInvWrapper(this);

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) invHandler;
        }
        return super.getCapability(capability, facing);
    }
}
