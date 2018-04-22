package mcjty.rftools.blocks.infuser;

import mcjty.lib.api.Infusable;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.container.*;
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

    public static final int SLOT_SHARDINPUT = 0;
    public static final int SLOT_MACHINEOUTPUT = 1;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimensionalShardItem)), ContainerFactory.CONTAINER_CONTAINER, SLOT_SHARDINPUT, 64, 24, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), ContainerFactory.CONTAINER_CONTAINER, SLOT_MACHINEOUTPUT, 118, 24, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };
    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 2);

    private int infusing = 0;

    public MachineInfuserTileEntity() {
        super(MachineInfuserConfiguration.MAXENERGY, MachineInfuserConfiguration.RECEIVEPERTICK);
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
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
            if (!inputStack.isEmpty() && inputStack.getItem() == ModItems.dimensionalShardItem && isInfusable(outputStack)) {
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

    public static NBTTagCompound getTagCompound(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        if (stack.getCount() != 1) {
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

        if (getEnergyStored() < rf) {
            // Not enough energy.
            return;
        }
        consumeEnergy(rf);

        inventoryHelper.getStackInSlot(0).splitStack(1);
        if (inventoryHelper.getStackInSlot(0).isEmpty()) {
            inventoryHelper.setStackInSlot(0, ItemStack.EMPTY);
        }
        infusing = 5;
        markDirty();
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { SLOT_SHARDINPUT, SLOT_MACHINEOUTPUT};
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return CONTAINER_FACTORY.isInputSlot(index) || CONTAINER_FACTORY.isSpecificItemSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return CONTAINER_FACTORY.isOutputSlot(index);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_SHARDINPUT && stack.getItem() != ModItems.dimensionalShardItem) {
            return false;
        }
        return true;
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
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("infusing", infusing);
    }
}
