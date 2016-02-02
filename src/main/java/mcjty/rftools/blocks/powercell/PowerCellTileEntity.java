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
import net.minecraft.util.ITickable;

public class PowerCellTileEntity extends GenericTileEntity implements IEnergyProvider, IEnergyReceiver, DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PowerCellContainer.factory, 2);

    private int networkId = -1;

    // Only used when this block is not part of a network
    private int energy = 0;

    public PowerCellTileEntity() {
        super();
    }

    public int getNetworkId() {
        return networkId;
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
        energy = tagCompound.getInteger("energy");
        networkId = tagCompound.getInteger("networkId");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setInteger("energy", energy);
        tagCompound.setInteger("networkId", networkId);
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            sendOutEnergy();
        }
    }

    private void sendOutEnergy() {
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

    private void handleCardRemoval() {
        if (!worldObj.isRemote) {
            PowerCellNetwork.Network network = getNetwork();
            energy = network.extractEnergySingleBlock();
            network.getBlocks().remove(getGlobalPos());
            PowerCellNetwork.getChannels(worldObj).save(worldObj);
        }
        networkId = -1;
        markDirty();
    }

    private void handleCardInsertion() {
        ItemStack stack = inventoryHelper.getStackInSlot(PowerCellContainer.SLOT_CARD);
        int id = PowerCellCardItem.getId(stack);
        if (!worldObj.isRemote) {
            PowerCellNetwork channels = PowerCellNetwork.getChannels(worldObj);
            if (id == -1) {
                id = channels.newChannel();
                PowerCellCardItem.setId(stack, id);
            }
            networkId = id;
            PowerCellNetwork.Network network = getNetwork();
            network.getBlocks().add(getGlobalPos());
            network.setEnergy(network.getEnergy() + energy);
            channels.save(worldObj);
        } else {
            networkId = id;
        }
        markDirty();
    }

    public int getEnergy() {
        return energy;
    }

    public GlobalCoordinate getGlobalPos() {
        return new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId());
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index == PowerCellContainer.SLOT_CARD) {
            handleCardRemoval();
        }
        return inventoryHelper.removeStackFromSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == PowerCellContainer.SLOT_CARD && inventoryHelper.containsItem(index) && count >= inventoryHelper.getStackInSlot(index).stackSize) {
            handleCardRemoval();
        }
        return inventoryHelper.decrStackSize(index, count);
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == PowerCellContainer.SLOT_CARD) {
            handleCardRemoval();
        }
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index == PowerCellContainer.SLOT_CARD && inventoryHelper.containsItem(index)) {
            handleCardInsertion();
        }
        else if (index == PowerCellContainer.SLOT_CARDCOPY && inventoryHelper.containsItem(index)) {
            PowerCellCardItem.setId(inventoryHelper.getStackInSlot(index), networkId);
        }
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return receiveEnergyLocal(maxReceive, simulate);
        } else {
            return receiveEnergyMulti(maxReceive, simulate);
        }
    }

    private int receiveEnergyMulti(int maxReceive, boolean simulate) {
        PowerCellNetwork.Network network = getNetwork();
        int maxInsert = Math.min(PowerCellConfiguration.rfPerCell * network.getBlocks().size() - network.getEnergy(), maxReceive);
        if (maxInsert > 0) {
            if (!simulate) {
                network.setEnergy(network.getEnergy() + maxInsert);
                PowerCellNetwork.getChannels(worldObj).save(worldObj);
            }
        }
        return maxInsert;
    }

    private int receiveEnergyLocal(int maxReceive, boolean simulate) {
        int maxInsert = Math.min(PowerCellConfiguration.rfPerCell - energy, maxReceive);
        if (maxInsert > 0) {
            if (!simulate) {
                energy += maxInsert;
                markDirty();
            }
        }
        return maxInsert;
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return extractEnergyLocal(maxExtract, simulate);
        } else {
            return extractEnergyMulti(maxExtract, simulate);
        }
    }

    private int extractEnergyMulti(int maxExtract, boolean simulate) {
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

    private int extractEnergyLocal(int maxExtract, boolean simulate) {
        // We act as a single block
        if (maxExtract > energy) {
            maxExtract = energy;
        }
        if (maxExtract > PowerCellConfiguration.rfPerTick) {
            maxExtract = PowerCellConfiguration.rfPerTick;
        }
        if (!simulate) {
            energy -= maxExtract;
            markDirty();
        }
        return maxExtract;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return energy;
        }
        PowerCellNetwork.Network network = getNetwork();
        return network.getEnergy();
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        int networkId = getNetworkId();
        if (networkId == -1) {
            return PowerCellConfiguration.rfPerCell;
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
