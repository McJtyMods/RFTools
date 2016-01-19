package mcjty.rftools.blocks.infuser;

import mcjty.lib.api.Infusable;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class MachineInfuserTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, MachineInfuserContainer.factory, 2);

    private int infusing = 0;

    public MachineInfuserTileEntity() {
        super(MachineInfuserConfiguration.MAXENERGY, MachineInfuserConfiguration.RECEIVEPERTICK);
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (infusing > 0) {
            infusing--;
            if (infusing == 0) {
                ItemStack outputStack = inventoryHelper.getStackInSlot(1);
                finishInfusing(outputStack);
            }
            markDirty();
        } else {
            ItemStack inputStack = inventoryHelper.getStackInSlot(0);
            ItemStack outputStack = inventoryHelper.getStackInSlot(1);
            if (inputStack != null && inputStack.getItem() == ModItems.dimensionalShardItem && isInfusable(outputStack)) {
                startInfusing();
            }
        }
    }

    private boolean isInfusable(ItemStack stack) {
        NBTTagCompound tagCompound = getTagCompound(stack);
        if (tagCompound == null) {
            return false;
        }
        int infused = tagCompound.getInteger("infused");
        if (infused >= GeneralConfig.maxInfuse) {
            return false;   // Already infused to the maximum.
        }
        return true;
    }

    private NBTTagCompound getTagCompound(ItemStack stack) {
        if (stack == null) {
            return null;
        }

        if (stack.stackSize != 1) {
            return null;
        }

        Item item = stack.getItem();
        if (!(item instanceof ItemBlock)) {
            return null;
        }
        Block block = ((ItemBlock)item).getBlock();
        if (!(block instanceof Infusable)) {
            return null;
        }
        NBTTagCompound tagCompound = stack.getTagCompound();
        if (tagCompound == null) {
            return new NBTTagCompound();
        } else {
            return tagCompound;
        }
    }

    private void finishInfusing(ItemStack stack) {
        NBTTagCompound tagCompound = getTagCompound(stack);
        if (tagCompound == null) {
            return;
        }
        int infused = tagCompound.getInteger("infused");
        tagCompound.setInteger("infused", infused+1);
        stack.setTagCompound(tagCompound);
    }

    private void startInfusing() {
        int rf = MachineInfuserConfiguration.rfPerTick;
        rf = (int) (rf * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(EnumFacing.DOWN) < rf) {
            // Not enough energy.
            return;
        }
        consumeEnergy(rf);

        inventoryHelper.getStackInSlot(0).splitStack(1);
        if (inventoryHelper.getStackInSlot(0).stackSize == 0) {
            inventoryHelper.setStackInSlot(0, null);
        }
        infusing = 5;
        markDirty();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { MachineInfuserContainer.SLOT_SHARDINPUT, MachineInfuserContainer.SLOT_MACHINEOUTPUT};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return MachineInfuserContainer.factory.isInputSlot(index) || MachineInfuserContainer.factory.isSpecificItemSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return MachineInfuserContainer.factory.isOutputSlot(index);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        infusing = tagCompound.getInteger("infusing");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("infusing", infusing);
    }
}
