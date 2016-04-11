package mcjty.rftools.blocks.generator;


import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyContainerItem;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyProviderTileEntity;
import mcjty.lib.varia.CustomSidedInvWrapper;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class CoalGeneratorTileEntity extends GenericEnergyProviderTileEntity implements ITickable, DefaultSidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CoalGeneratorContainer.factory, 2);

    private int burning;

    public CoalGeneratorTileEntity() {
        super(CoalGeneratorConfiguration.MAXENERGY, CoalGeneratorConfiguration.SENDPERTICK);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            handleChargingItem();
            handleSendingEnergy();

            boolean working = burning > 0;

            if (burning > 0) {
                burning--;
                int rf = getRfPerTick();
                modifyEnergyStored(rf);
                if (burning == 0) {
                    markDirtyClient();
                } else {
                    markDirty();
                }
                return;
            }

            if (inventoryHelper.containsItem(CoalGeneratorContainer.SLOT_COALINPUT)) {
                inventoryHelper.decrStackSize(CoalGeneratorContainer.SLOT_COALINPUT, 1);
                burning = CoalGeneratorConfiguration.ticksPerCoal;
                burning += (int) (burning * getInfusedFactor() / 2.0f);
                if (working) {
                    markDirty();
                } else {
                    markDirtyClient();
                }
            } else {
                if (working) {
                    markDirtyClient();
                }
            }
        }
    }

    public int getRfPerTick() {
        int rf = CoalGeneratorConfiguration.rfPerTick;
        rf += (int) (rf * getInfusedFactor());
        return rf;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean working = burning > 0;

        super.onDataPacket(net, packet);

        if (worldObj.isRemote) {
            // If needed send a render update.
            boolean newWorking = burning > 0;
            if (newWorking != working) {
                worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    public boolean isWorking() {
        return burning > 0;
    }

    private void handleChargingItem() {
        ItemStack stack = inventoryHelper.getStackInSlot(CoalGeneratorContainer.SLOT_CHARGEITEM);
        if (stack != null && stack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem energyContainerItem = (IEnergyContainerItem) stack.getItem();
            int energyStored = getEnergyStored(EnumFacing.DOWN);
            int rfToGive = CoalGeneratorConfiguration.CHARGEITEMPERTICK <= energyStored ? CoalGeneratorConfiguration.CHARGEITEMPERTICK : energyStored;
            int received = energyContainerItem.receiveEnergy(stack, rfToGive, false);
            extractEnergy(EnumFacing.DOWN, received, false);
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
                    int rfToGive = CoalGeneratorConfiguration.SENDPERTICK <= energyStored ? CoalGeneratorConfiguration.SENDPERTICK : energyStored;

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
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { CoalGeneratorContainer.SLOT_COALINPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
        return isItemValidForSlot(index, stack);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == CoalGeneratorContainer.SLOT_CHARGEITEM) {
            return stack.getItem() instanceof IEnergyContainerItem;
        } else if (index == CoalGeneratorContainer.SLOT_COALINPUT) {
            return stack.getItem() == Items.coal;
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
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
