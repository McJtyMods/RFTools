package mcjty.rftools.blocks.generator;


import cofh.api.energy.IEnergyConnection;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyProviderTileEntity;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

public class CoalGeneratorTileEntity extends GenericEnergyProviderTileEntity implements ITickable, DefaultSidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CoalGeneratorContainer.factory, 1);

    private int burning;

    public CoalGeneratorTileEntity() {
        super(CoalGeneratorConfiguration.MAXENERGY, CoalGeneratorConfiguration.SENDPERTICK);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            handleSendingEnergy();

            if (burning > 0) {
                burning--;
                modifyEnergyStored(CoalGeneratorConfiguration.rfPerTick);
                markDirty();
                return;
            }

            if (inventoryHelper.containsItem(CoalGeneratorContainer.SLOT_COALINPUT)) {
                inventoryHelper.decrStackSize(CoalGeneratorContainer.SLOT_COALINPUT, 1);
                burning = CoalGeneratorConfiguration.ticksPerCoal;
                markDirty();
            }
        }
    }

    private void handleSendingEnergy() {
        int energyStored = getEnergyStored(EnumFacing.DOWN);

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos pos = getPos().offset(facing);
            TileEntity te = worldObj.getTileEntity(pos);
            if (EnergyTools.isEnergyTE(te)) {
                IEnergyConnection connection = (IEnergyConnection) te;
                EnumFacing opposite = facing.getOpposite();
                if (connection.canConnectEnergy(opposite)) {
                    int rfToGive;
                    if (CoalGeneratorConfiguration.SENDPERTICK <= energyStored) {
                        rfToGive = CoalGeneratorConfiguration.SENDPERTICK;
                    } else {
                        rfToGive = energyStored;
                    }

                    int received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                    energyStored -= extractEnergy(EnumFacing.DOWN, received, false);
                    if (energyStored <= 0) {
                        break;
                    }
                }
            }
        }
    }


    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { CoalGeneratorContainer.SLOT_COALINPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
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
    public ItemStack decrStackSize(int index, int count) {
        return inventoryHelper.decrStackSize(index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return inventoryHelper.removeStackFromSlot(index);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(64, index, stack);
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        burning = tagCompound.getInteger("burning");
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("burning", burning);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }
}
