package mcjty.rftools.blocks.powercell;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyContainerItem;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import mcjty.lib.api.smartwrench.SmartWrenchSelector;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.CustomSidedInvWrapper;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.teleporter.TeleportationTools;
import mcjty.rftools.items.powercell.PowerCellCardItem;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.Map;
import java.util.Set;

public class PowerCellTileEntity extends GenericTileEntity implements IEnergyProvider, IEnergyReceiver, DefaultSidedInventory, ITickable, SmartWrenchSelector {

    public static String CMD_SETNONE = "setNone";
    public static String CMD_SETINPUT = "setInput";
    public static String CMD_SETOUTPUT = "setOutput";
    public static String CMD_CLEARSTATS = "clearStats";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PowerCellContainer.factory, 3);

    private int networkId = -1;

    // Only used when this block is not part of a network
    private int energy = 0;

    // Total amount of energy extracted from this block (local or not)
    private int totalExtracted = 0;
    // Total amount of energy inserted in this block (local or not)
    private int totalInserted = 0;

    public enum Mode implements IStringSerializable {
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
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
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
        totalInserted = tagCompound.getInteger("totIns");
        totalExtracted = tagCompound.getInteger("totExt");
        networkId = tagCompound.getInteger("networkId");
        modes[0] = Mode.values()[tagCompound.getByte("m0")];
        modes[1] = Mode.values()[tagCompound.getByte("m1")];
        modes[2] = Mode.values()[tagCompound.getByte("m2")];
        modes[3] = Mode.values()[tagCompound.getByte("m3")];
        modes[4] = Mode.values()[tagCompound.getByte("m4")];
        modes[5] = Mode.values()[tagCompound.getByte("m5")];
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
        tagCompound.setInteger("energy", energy);
        tagCompound.setInteger("totIns", totalInserted);
        tagCompound.setInteger("totExt", totalExtracted);
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
            if (isCreative()) {
                // A creative powercell automatically generates 1000000 RF/tick
                int gain = 1000000;
                int networkId = getNetworkId();
                if (networkId == -1) {
                    receiveEnergyLocal(gain, false);
                } else {
                    receiveEnergyMulti(gain, false);
                }
            }

            int energyStored = getEnergyStored(EnumFacing.DOWN);
            if (energyStored <= 0) {
                return;
            }

            handleChargingItem();
            sendOutEnergy();
        }
    }

    private void handleChargingItem() {
        ItemStack stack = inventoryHelper.getStackInSlot(PowerCellContainer.SLOT_CHARGEITEM);
        if (stack != null && stack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem energyContainerItem = (IEnergyContainerItem) stack.getItem();
            int energyStored = getEnergyStored(EnumFacing.DOWN);
            int rfToGive = PowerCellConfiguration.CHARGEITEMPERTICK <= energyStored ? PowerCellConfiguration.CHARGEITEMPERTICK : energyStored;
            int received = energyContainerItem.receiveEnergy(stack, rfToGive, false);
            extractEnergyInternal(received, false, PowerCellConfiguration.CHARGEITEMPERTICK);
        }
    }

    private void sendOutEnergy() {
        int energyStored = getEnergyStored(EnumFacing.DOWN);

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
                            float factor = getCostFactor();
                            int rfPerTick = getRfPerTickPerSide();

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

    public float getCostFactor() {
        float infusedFactor = getInfusedFactor();

        float factor;
        if (getNetworkId() == -1) {
            factor = 1.0f; // Local energy
        } else {
            factor = getNetwork().calculateCostFactor(worldObj, getGlobalPos());
            factor = (factor - 1) * (1-infusedFactor/2) + 1;
        }
        return factor;
    }

    public int getRfPerTickPerSide() {
        return (int) (PowerCellConfiguration.rfPerTick * getAdvancedFactor() * (getInfusedFactor()*.5+1));
    }

    private void handleCardRemoval() {
        if (!worldObj.isRemote) {
            PowerCellNetwork.Network network = getNetwork();
            if (network != null) {
                energy = network.extractEnergySingleBlock(isAdvanced());
                network.remove(worldObj, getGlobalPos(), isAdvanced());
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
            network.add(worldObj, getGlobalPos(), isAdvanced());
            network.setEnergy(network.getEnergy() + energy);
            channels.save(worldObj);
        } else {
            networkId = id;
        }
        markDirty();
    }

    private boolean isAdvanced() {
        return PowerCellBlock.isAdvanced(worldObj.getBlockState(getPos()).getBlock());
    }

    private boolean isCreative() {
        return PowerCellBlock.isCreative(worldObj.getBlockState(getPos()).getBlock());
    }

    private int getAdvancedFactor() {
        return isAdvanced() ? PowerCellConfiguration.advancedFactor : 1;
    }

    public int getEnergy() {
        return energy;
    }

    public GlobalCoordinate getGlobalPos() {
        return new GlobalCoordinate(getPos(), worldObj.provider.getDimension());
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

    public int getTotalExtracted() {
        return totalExtracted;
    }

    public int getTotalInserted() {
        return totalInserted;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if (modes[from.ordinal()] != Mode.MODE_INPUT) {
            return 0;
        }
        int networkId = getNetworkId();
        int received;
        if (networkId == -1) {
            received = receiveEnergyLocal(maxReceive, simulate);
        } else {
            received = receiveEnergyMulti(maxReceive, simulate);
        }
        if (!simulate) {
            totalInserted += received;
            markDirty();
        }
        return received;
    }

    private int receiveEnergyMulti(int maxReceive, boolean simulate) {
        PowerCellNetwork.Network network = getNetwork();
        int totEnergy = PowerCellConfiguration.rfPerCell * (network.getBlockCount() - network.getAdvancedBlockCount()) + PowerCellConfiguration.rfPerCell * PowerCellConfiguration.advancedFactor * network.getAdvancedBlockCount();
        int maxInsert = Math.min(totEnergy - network.getEnergy(), maxReceive);
        if (maxInsert > 0) {
            if (!simulate) {
                network.setEnergy(network.getEnergy() + maxInsert);
                PowerCellNetwork.getChannels(worldObj).save(worldObj);
            }
        }
        return maxInsert;
    }

    private int receiveEnergyLocal(int maxReceive, boolean simulate) {
        int maxInsert = Math.min(PowerCellConfiguration.rfPerCell * getAdvancedFactor() - energy, maxReceive);
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
        return extractEnergyInternal(maxExtract, simulate, PowerCellConfiguration.rfPerTick * getAdvancedFactor());
    }

    private int extractEnergyInternal(int maxExtract, boolean simulate, int maximum) {
        int networkId = getNetworkId();
        int extracted;
        if (networkId == -1) {
            extracted = extractEnergyLocal(maxExtract, simulate, maximum);
        } else {
            extracted =  extractEnergyMulti(maxExtract, simulate, maximum);
        }
        if (!simulate) {
            totalExtracted += extracted;
            markDirty();
        }
        return extracted;
    }

    private int extractEnergyMulti(int maxExtract, boolean simulate, int maximum) {
        PowerCellNetwork.Network network = getNetwork();
        int energy = network.getEnergy();
        if (maxExtract > energy) {
            maxExtract = energy;
        }
        if (maxExtract > maximum) {
            maxExtract = maximum;
        }
        if (!simulate) {
            network.setEnergy(energy - maxExtract);
            PowerCellNetwork.getChannels(worldObj).save(worldObj);
        }
        return maxExtract;
    }

    private int extractEnergyLocal(int maxExtract, boolean simulate, int maximum) {
        // We act as a single block
        if (maxExtract > energy) {
            maxExtract = energy;
        }
        if (maxExtract > maximum) {
            maxExtract = maximum;
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
            return PowerCellConfiguration.rfPerCell * getAdvancedFactor();
        }
        PowerCellNetwork.Network network = getNetwork();
        return (network.getBlockCount() - network.getAdvancedBlockCount()) * PowerCellConfiguration.rfPerCell +
                network.getAdvancedBlockCount() * PowerCellConfiguration.rfPerCell * PowerCellConfiguration.advancedFactor;
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
        } else if (CMD_CLEARSTATS.equals(command)) {
            totalExtracted = 0;
            totalInserted = 0;
            markDirty();
            return true;
        }
        return false;
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

    @Override
    public void selectBlock(EntityPlayer player, BlockPos pos) {
        dumpNetwork(player, this);
    }

    public static void dumpNetwork(EntityPlayer player, PowerCellTileEntity powerCellTileEntity) {
        PowerCellNetwork.Network network = powerCellTileEntity.getNetwork();
        Set<GlobalCoordinate> blocks = network.getBlocks();
        System.out.println("blocks.size() = " + blocks.size());
        blocks.forEach(b -> {
            String msg;
            World w = TeleportationTools.getWorldForDimension(player.worldObj, b.getDimension());
            if (w == null) {
                msg = "dimension missing!";
            } else {
                Block block = w.getBlockState(b.getCoordinate()).getBlock();
                if (block == PowerCellSetup.powerCellBlock) {
                    msg = "normal";
                } else if (block == PowerCellSetup.advancedPowerCellBlock) {
                    msg = "advanced";
                } else if (block == PowerCellSetup.creativePowerCellBlock) {
                    msg = "creative";
                } else {
                    msg = "not a powercell!";
                }
                TileEntity te = w.getTileEntity(b.getCoordinate());
                if (te instanceof PowerCellTileEntity) {
                    PowerCellTileEntity power = (PowerCellTileEntity) te;
                    msg += " (+:" + power.getTotalInserted() + ", -:" + power.getTotalExtracted() + ")";
                }
            }

            Logging.message(player, "Block: " + BlockPosTools.toString(b.getCoordinate()) + " (" + b.getDimension() + "): " + msg);
        });
    }

}
