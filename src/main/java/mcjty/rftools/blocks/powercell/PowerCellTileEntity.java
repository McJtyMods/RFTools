package mcjty.rftools.blocks.powercell;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.blocks.builder.BuilderConfiguration;
import mcjty.rftools.items.powercell.PowerCellCardItem;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;

import java.util.Map;

public class PowerCellTileEntity extends GenericTileEntity implements IEnergyProvider, IEnergyReceiver, DefaultSidedInventory, ITickable {

    public static String CMD_SETNONE = "setNone";
    public static String CMD_SETINPUT = "setInput";
    public static String CMD_SETOUTPUT = "setOutput";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PowerCellContainer.factory, 2);

    private int networkId = -1;

    // Only used when this block is not part of a network
    private int energy = 0;

    public static enum Mode implements IStringSerializable {
        MODE_NONE("none"),
        MODE_INPUT("input"),   // Blue
        MODE_OUTPUT("output"); // Yellow

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
    private Mode modes[] = new Mode[] { Mode.MODE_NONE, Mode.MODE_NONE, Mode.MODE_NONE, Mode.MODE_NONE, Mode.MODE_NONE, Mode.MODE_NONE };

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
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        Mode[] old = new Mode[] { modes[0], modes[1], modes[2], modes[3], modes[4], modes[5] };
        super.onDataPacket(net, packet);
        for (int i = 0 ; i < 6 ; i++) {
            if (old[i] != modes[i]) {
                worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
                return;
            }
        }
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
        modes[0] = Mode.values()[tagCompound.getByte("m0")];
        modes[1] = Mode.values()[tagCompound.getByte("m1")];
        modes[2] = Mode.values()[tagCompound.getByte("m2")];
        modes[3] = Mode.values()[tagCompound.getByte("m3")];
        modes[4] = Mode.values()[tagCompound.getByte("m4")];
        modes[5] = Mode.values()[tagCompound.getByte("m5")];
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
        tagCompound.setByte("m0", (byte) modes[0].ordinal());
        tagCompound.setByte("m1", (byte) modes[1].ordinal());
        tagCompound.setByte("m2", (byte) modes[2].ordinal());
        tagCompound.setByte("m3", (byte) modes[3].ordinal());
        tagCompound.setByte("m4", (byte) modes[4].ordinal());
        tagCompound.setByte("m5", (byte) modes[5].ordinal());
    }

    public Mode getMode(EnumFacing side) {
        return modes[side.ordinal()];
    }

    public void toggleMode(EnumFacing side) {
        switch (modes[side.ordinal()]) {
            case MODE_NONE:
                modes[side.ordinal()] = Mode.MODE_INPUT;
                break;
            case MODE_INPUT:
                modes[side.ordinal()] = Mode.MODE_OUTPUT;
                break;
            case MODE_OUTPUT:
                modes[side.ordinal()] = Mode.MODE_NONE;
                break;
        }
        markDirtyClient();
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

        for (EnumFacing face : EnumFacing.values()) {
            if (modes[face.ordinal()] == Mode.MODE_OUTPUT) {
                BlockPos pos = getPos().offset(face);
                TileEntity te = worldObj.getTileEntity(pos);
                if (EnergyTools.isEnergyTE(te)) {
                    // If the adjacent block is also a powercell then we only send energy if this cell is local or the other cell has a different id
                    if ((!(te instanceof PowerCellTileEntity)) || getNetworkId() == -1 || ((PowerCellTileEntity) te).getNetworkId() != getNetworkId()) {
                        IEnergyConnection connection = (IEnergyConnection) te;
                        EnumFacing opposite = face.getOpposite();
                        if (connection.canConnectEnergy(opposite)) {
                            float infusedFactor = getInfusedFactor();

                            float factor;
                            if (getNetworkId() == -1) {
                                factor = 1.0f; // Local energy
                            } else {
                                factor = getNetwork().calculateCostFactor(worldObj, getGlobalPos());
                                factor = (factor - 1) * (1-infusedFactor/2) + 1;
                            }

                            int rfPerTick = (int) (PowerCellConfiguration.rfPerTick * (infusedFactor*.5+1));

                            int rfToGive;
                            if (rfPerTick <= ((int) (energyStored / factor))) {
                                rfToGive = rfPerTick;
                            } else {
                                rfToGive = (int) (energyStored / factor);
                            }

                            int received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                            energyStored -= extractEnergy(EnumFacing.DOWN, (int) (received * factor), false);
                            if (energyStored <= 0) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleCardRemoval() {
        if (!worldObj.isRemote) {
            PowerCellNetwork.Network network = getNetwork();
            if (network != null) {
                energy = network.extractEnergySingleBlock();
                network.remove(getGlobalPos());
                PowerCellNetwork.getChannels(worldObj).save(worldObj);
            }
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
            network.add(getGlobalPos());
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
        if (modes[from.ordinal()] != Mode.MODE_INPUT) {
            return 0;
        }
        int networkId = getNetworkId();
        if (networkId == -1) {
            return receiveEnergyLocal(maxReceive, simulate);
        } else {
            return receiveEnergyMulti(maxReceive, simulate);
        }
    }

    private int receiveEnergyMulti(int maxReceive, boolean simulate) {
        PowerCellNetwork.Network network = getNetwork();
        int maxInsert = Math.min(PowerCellConfiguration.rfPerCell * network.getBlockCount() - network.getEnergy(), maxReceive);
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
        return network.getBlockCount() * PowerCellConfiguration.rfPerCell;
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

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNONE.equals(command)) {
            for (EnumFacing facing : EnumFacing.values()) {
                modes[facing.ordinal()] = Mode.MODE_NONE;
            }
            markDirtyClient();
            return true;
        } else if (CMD_SETINPUT.equals(command)) {
            for (EnumFacing facing : EnumFacing.values()) {
                modes[facing.ordinal()] = Mode.MODE_INPUT;
            }
            markDirtyClient();
            return true;
        } else if (CMD_SETOUTPUT.equals(command)) {
            for (EnumFacing facing : EnumFacing.values()) {
                modes[facing.ordinal()] = Mode.MODE_OUTPUT;
            }
            markDirtyClient();
            return true;
        }
        return false;
    }
}
