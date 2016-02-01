package mcjty.rftools.blocks.powercell;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.items.powercell.PowerCellCardItem;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class PowerCellTileEntity extends GenericTileEntity implements IEnergyProvider, IEnergyReceiver, DefaultSidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PowerCellContainer.factory, 2);

    public PowerCellTileEntity() {
        super();
    }

    public void updateNetwork() {
        PowerCellNetwork.Network network = getNetwork();
        if (network != null) {
            network.getBlocks().add(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
            PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(worldObj);
            powerCellNetwork.save(worldObj);
        }
    }

    public void removeBlockFromNetwork() {
        int networkId = getNetworkId();
        if (networkId != -1) {
            PowerCellNetwork generatorNetwork = PowerCellNetwork.getChannels(worldObj);
            PowerCellNetwork.Network network = generatorNetwork.getOrCreateNetwork(networkId);
            network.getBlocks().remove(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
            PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(worldObj);
            powerCellNetwork.save(worldObj);
        }
    }

    public int getNetworkId() {
        if (inventoryHelper.containsItem(PowerCellContainer.SLOT_CARD)) {
            ItemStack stack = inventoryHelper.getStackInSlot(PowerCellContainer.SLOT_CARD);
            PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(worldObj);
            int id = PowerCellCardItem.getId(stack);
            if (id == -1) {
                id = powerCellNetwork.newChannel();
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
                stack.getTagCompound().setInteger("id", id);
            }
            return id;
        }
        return -1;
    }

    public PowerCellNetwork.Network getNetwork() {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return null;
        }
        PowerCellNetwork generatorNetwork = PowerCellNetwork.getChannels(worldObj);
        return generatorNetwork.getOrCreateNetwork(networkId);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    protected void checkStateServer() {
        int energyStored = getEnergyStored(EnumFacing.DOWN);

        if (energyStored <= 0) {
            return;
        }

        for (int i = 0 ; i < 6 ; i++) {
            BlockPos pos = getPos().offset(EnumFacing.VALUES[i]);
            TileEntity te = worldObj.getTileEntity(pos);
            if (EnergyTools.isEnergyTE(te)) {
                IEnergyConnection connection = (IEnergyConnection) te;
                EnumFacing opposite = EnumFacing.VALUES[i].getOpposite();
                if (connection.canConnectEnergy(opposite)) {
                    int rfToGive;
                    if (PowerCellConfiguration.rfPerTick <= energyStored) {
                        rfToGive = PowerCellConfiguration.rfPerTick;
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
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return 0;
        }
        PowerCellNetwork.Network network = getNetwork();
        int energy = network.getEnergy();
        if (maxExtract > energy) {
            maxExtract = energy;
        }
        if (maxExtract > PowerCellConfiguration.rfPerTick) {
            maxExtract = PowerCellConfiguration.rfPerTick;
        }
        if (!simulate) {
            network.setEnergy(energy - maxExtract);
            PowerCellNetwork.getChannels(worldObj).save(worldObj);
        }
        return maxExtract;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return 0;
        }
        PowerCellNetwork.Network network = getNetwork();
        return network.getEnergy();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return 0;
        }
        PowerCellNetwork.Network network = getNetwork();
        return network.getBlocks().size() * PowerCellConfiguration.rfPerCell;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }


}
