package mcjty.rftools.blocks.generator;


import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyContainerItem;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyProviderTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Map;

public class CoalGeneratorTileEntity extends GenericEnergyProviderTileEntity implements ITickable, DefaultSidedInventory,
        IMachineInformation {

    public static final String CMD_RSMODE = "rsMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CoalGeneratorContainer.factory, 2);

    private int burning;

    public CoalGeneratorTileEntity() {
        super(CoalGeneratorConfiguration.MAXENERGY, CoalGeneratorConfiguration.SENDPERTICK);
    }

    @Override
    public int getEnergyDiffPerTick() {
        return burning > 0 ? getRfPerTick() : 0;
    }

    @Nullable
    @Override
    public String getEnergyUnitName() {
        return "RF";
    }

    @Override
    public boolean isMachineActive() {
        return isMachineEnabled();
    }

    @Override
    public boolean isMachineRunning() {
        return isMachineEnabled();
    }

    @Nullable
    @Override
    public String getMachineStatus() {
        return burning > 0 ? "generating power" : "idle";
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    public void setPowerInput(int powered) {
        boolean changed = powerLevel != powered;
        super.setPowerInput(powered);
        if (changed) {
            markDirtyClient();
        }
    }

    @Override
    public void update() {
        if (!worldObj.isRemote) {
            handleChargingItem();
            handleSendingEnergy();

            if (!isMachineEnabled()) {
                return;
            }

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
        boolean working = isWorking();

        super.onDataPacket(net, packet);

        if (worldObj.isRemote) {
            // If needed send a render update.
            boolean newWorking = isWorking();
            if (newWorking != working) {
                worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    public boolean isWorking() {
        return burning > 0 && isMachineEnabled();
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    private void handleChargingItem() {
        ItemStack stack = inventoryHelper.getStackInSlot(CoalGeneratorContainer.SLOT_CHARGEITEM);
        if (stack != null && stack.getItem() instanceof IEnergyContainerItem) {
            IEnergyContainerItem energyContainerItem = (IEnergyContainerItem) stack.getItem();
            int energyStored = getEnergyStored(EnumFacing.DOWN);
            int rfToGive = CoalGeneratorConfiguration.CHARGEITEMPERTICK <= energyStored ? CoalGeneratorConfiguration.CHARGEITEMPERTICK : energyStored;
            int received = energyContainerItem.receiveEnergy(stack, rfToGive, false);
            storage.extractEnergy(received, false);
        }
    }

    private void handleSendingEnergy() {
        int energyStored = getEnergyStored(EnumFacing.DOWN);

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos pos = getPos().offset(facing);
            TileEntity te = worldObj.getTileEntity(pos);
            if (EnergyTools.isEnergyTE(te)) {
                EnumFacing opposite = facing.getOpposite();
                int rfToGive = CoalGeneratorConfiguration.SENDPERTICK <= energyStored ? CoalGeneratorConfiguration.SENDPERTICK : energyStored;
                int received;

                if (te instanceof IEnergyConnection) {
                    IEnergyConnection connection = (IEnergyConnection) te;
                    if (connection.canConnectEnergy(opposite)) {
                        received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                    } else {
                        received = 0;
                    }
                } else {
                    // Forge unit
                    received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                }
                energyStored -= storage.extractEnergy(received, false);
                if (energyStored <= 0) {
                    break;
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
            return stack.getItem() == Items.COAL;
        }
        return true;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return false;
    }

    @SuppressWarnings("NullableProblems")
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

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            return true;
        }

        return false;
    }

}
