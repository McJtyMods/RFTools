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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

public class PowerCellTileEntity extends GenericTileEntity implements IEnergyProvider, IEnergyReceiver, DefaultSidedInventory {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PowerCellContainer.factory, 2);

    // Only use on the client side
    private int networkId = -1;

    // Only used when this block is not part of a network
    private int energy = 0;

    public PowerCellTileEntity() {
        super();
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbtTag = new NBTTagCompound();
        nbtTag.setInteger("id", getNetworkId());
        nbtTag.setInteger("energy", energy);
        return new S35PacketUpdateTileEntity(this.pos, 1, nbtTag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        networkId = packet.getNbtCompound().getInteger("id");
        energy = packet.getNbtCompound().getInteger("energy");
    }

    public void updateNetwork() {
        if (!inventoryHelper.containsItem(PowerCellContainer.SLOT_CARD)) {
            removeBlockFromNetwork();
            return;
        }
        PowerCellNetwork.Network network = getNetwork();
        if (network != null) {
            network.getBlocks().add(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
            PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(worldObj);
            powerCellNetwork.save(worldObj);
        }
        markDirtyClient();
    }

    public void removeBlockFromNetwork() {
        int networkId = getNetworkId();
        if (networkId != -1) {
            PowerCellNetwork generatorNetwork = PowerCellNetwork.getChannels(worldObj);
            PowerCellNetwork.Network network = generatorNetwork.getOrCreateNetwork(networkId);
            network.getBlocks().remove(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
            PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(worldObj);
            powerCellNetwork.save(worldObj);
            markDirtyClient();
        }
    }

    public int getNetworkId() {
        // On the client we use the networkId we got synced from the server
        if (worldObj.isRemote) {
            return networkId;
        }
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
        energy = tagCompound.getInteger("energy");
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
        int networkId = getNetworkId();
        if (networkId == -1) {
            return receiveEnergyLocal(maxReceive, simulate);
        } else {
            return receiveEnergyMulti(maxReceive, simulate);
        }
    }

    private int receiveEnergyMulti(int maxReceive, boolean simulate) {
        PowerCellNetwork.Network network = getNetwork();
        int maxInsert = Math.max(PowerCellConfiguration.rfPerCell * network.getBlocks().size()-network.getEnergy(), maxReceive);
        if (maxInsert > 0) {
            if (!simulate) {
                network.setEnergy(network.getEnergy() + maxInsert);
                PowerCellNetwork.getChannels(worldObj).save(worldObj);
            }
        }
        return maxInsert;
    }

    private int receiveEnergyLocal(int maxReceive, boolean simulate) {
        int maxInsert = Math.max(PowerCellConfiguration.rfPerCell-energy, maxReceive);
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
    public void setInventorySlotContents(int index, ItemStack stack) {
        if (index == PowerCellContainer.SLOT_CARD) {
            if (!worldObj.isRemote) {
                PowerCellNetwork.Network network = getNetwork();
                if (inventoryHelper.containsItem(index)) {
                    // Store the energy locally
                    if (network != null) {
                        energy = network.getEnergy() / Math.max(network.getBlocks().size(), 1);
                        network.getBlocks().remove(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
                    } else {
                        energy = 0;
                    }
                }
            }
        }
        this.getInventoryHelper().setInventorySlotContents(this.getInventoryStackLimit(), index, stack);
        if (index == PowerCellContainer.SLOT_CARD) {
            if (!worldObj.isRemote) {
                PowerCellNetwork.Network network = getNetwork();
                if (inventoryHelper.containsItem(index)) {
                    // Store the energy locally
                    if (network != null) {
                        network.setEnergy(energy + network.getEnergy());
                        network.getBlocks().add(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
                    }
                }
            }
        }
    }


    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }


}
