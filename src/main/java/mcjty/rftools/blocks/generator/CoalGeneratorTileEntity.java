package mcjty.rftools.blocks.generator;


import cofh.api.energy.IEnergyConnection;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyProviderTileEntity;
import mcjty.rftools.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
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

            boolean working = burning > 0;

            if (burning > 0) {
                burning--;
                modifyEnergyStored(CoalGeneratorConfiguration.rfPerTick);
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

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        boolean working = burning > 0;

        super.onDataPacket(net, packet);

        if (worldObj.isRemote) {
            // If needed send a render update.
            boolean newWorking = burning > 0;
            if (newWorking != working) {
                worldObj.markBlockRangeForRenderUpdate(getPos(), getPos());
                System.out.println("CLIENT UPDATE");
            }
        }
    }

    public boolean isWorking() {
        return burning > 0;
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
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { CoalGeneratorContainer.SLOT_COALINPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
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
}
