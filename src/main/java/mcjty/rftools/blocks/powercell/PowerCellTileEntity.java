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
import net.minecraft.util.ITickable;

public class PowerCellTileEntity extends GenericTileEntity implements IEnergyProvider, IEnergyReceiver, DefaultSidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PowerCellContainer.factory, 2);

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
        int oldid = networkId;
        networkId = packet.getNbtCompound().getInteger("id");
        energy = packet.getNbtCompound().getInteger("energy");
        if (oldid != networkId) {
            worldObj.markBlockRangeForRenderUpdate(pos.add(-1, -1, -1), pos.add(1, 1, 1));
        }
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
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        handleCardSlots();
        sendOutEnergy();
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

    private void handleCardSlots() {
        if (inventoryHelper.containsItem(PowerCellContainer.SLOT_CARD)) {
            ItemStack stack = inventoryHelper.getStackInSlot(PowerCellContainer.SLOT_CARD);
            int id = PowerCellCardItem.getId(stack);
            if (id == -1) {
                PowerCellNetwork powerCellNetwork = PowerCellNetwork.getChannels(worldObj);
                id = powerCellNetwork.newChannel();
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }
                stack.getTagCompound().setInteger("id", id);
                networkId = id;
                markDirtyClient();
                addThisToNetwork();
            } else if (id != networkId) {
                networkId = id;
                markDirtyClient();
                addThisToNetwork();
            }
            int e = getNetwork().getEnergy() / Math.max(1, getNetwork().getBlocks().size());
            if (e != energy) {
                energy = e;
                markDirty();
            }

            if (inventoryHelper.containsItem(PowerCellContainer.SLOT_CARDCOPY)) {
                ItemStack copy = inventoryHelper.getStackInSlot(PowerCellContainer.SLOT_CARDCOPY);
                if (!copy.hasTagCompound()) {
                    copy.setTagCompound(new NBTTagCompound());
                }
                copy.getTagCompound().setInteger("id", networkId);
            }

        } else {
            if (networkId != -1) {
                networkId = -1;
                markDirtyClient();
            }
        }
    }

    private void addThisToNetwork() {
        getNetwork().getBlocks().add(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
        PowerCellNetwork.getChannels(worldObj).save(worldObj);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (index == PowerCellContainer.SLOT_CARD) {
            if (!worldObj.isRemote) {
                PowerCellNetwork.Network network = getNetwork();
                network.getBlocks().remove(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
                PowerCellNetwork.getChannels(worldObj).save(worldObj);
            }
            networkId = -1;
            markDirty();
        }
        return inventoryHelper.removeStackFromSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (index == PowerCellContainer.SLOT_CARD) {
            if (!worldObj.isRemote) {
                PowerCellNetwork.Network network = getNetwork();
                network.getBlocks().remove(new GlobalCoordinate(getPos(), worldObj.provider.getDimensionId()));
                PowerCellNetwork.getChannels(worldObj).save(worldObj);
            }
            networkId = -1;
            markDirty();
        }
        return inventoryHelper.decrStackSize(index, count);
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
