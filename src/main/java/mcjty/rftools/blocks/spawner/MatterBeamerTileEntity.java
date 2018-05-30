package mcjty.rftools.blocks.spawner;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.bindings.IValue;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MatterBeamerTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final int TICKTIME = 20;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, MatterBeamerContainer.factory, 1);

    public static final Key<BlockPos> VALUE_DESTINATION = new Key<>("destination", Type.BLOCKPOS);

    @Override
    public IValue<?, ?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_DESTINATION, MatterBeamerTileEntity::getDestination, MatterBeamerTileEntity::setDestination)
        };
    }


    // The location of the destination spawner..
    private BlockPos destination = null;
    private boolean glowing = false;

    private int ticker = TICKTIME;

    public MatterBeamerTileEntity() {
        super(SpawnerConfiguration.BEAMER_MAXENERGY, SpawnerConfiguration.BEAMER_RECEIVEPERTICK);
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    public boolean isPowered() {
        return powerLevel != 0;
    }

    public boolean isGlowing() {
        return glowing;
    }

    private void checkStateServer() {
        if (powerLevel == 0) {
            disableBlockGlow();
            return;
        }

        ticker--;
        if (ticker > 0) {
            return;
        }
        ticker = TICKTIME;

        TileEntity te = null;
        if (destination != null) {
            te = getWorld().getTileEntity(destination);
            if (!(te instanceof SpawnerTileEntity)) {
                setDestination(null);
                return;
            }
        } else {
            return;
        }

        ItemStack itemStack = inventoryHelper.getStackInSlot(0);
        if (itemStack.isEmpty()) {
            disableBlockGlow();
            return;
        }

        SpawnerTileEntity spawnerTileEntity = (SpawnerTileEntity) te;

        int maxblocks = (int) (SpawnerConfiguration.beamBlocksPerSend * (1.01 + getInfusedFactor() * 2.0));
        int numblocks = Math.min(maxblocks, itemStack.getCount());

        int rf = (int) (SpawnerConfiguration.beamRfPerObject * numblocks * (4.0f - getInfusedFactor()) / 4.0f);
        if (getEnergyStored() < rf) {
            return;
        }
        consumeEnergy(rf);

        if (spawnerTileEntity.addMatter(itemStack, numblocks, getInfusedFactor())) {
            inventoryHelper.decrStackSize(0, numblocks);
            enableBlockGlow();
        }
    }

    private void disableBlockGlow() {
        if (glowing) {
            glowing = false;
            markDirtyClient();
        }
    }

    private void enableBlockGlow() {
        if (!glowing) {
            glowing = true;
            markDirtyClient();
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean oldglowing = glowing;

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            if (oldglowing != glowing) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }


    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int xCoord = getPos().getX();
        int yCoord = getPos().getY();
        int zCoord = getPos().getZ();
        return new AxisAlignedBB(xCoord - 4, yCoord - 4, zCoord - 4, xCoord + 5, yCoord + 5, zCoord + 5);
    }


    // Called from client side when a wrench is used.
    public void useWrench(EntityPlayer player) {
        BlockPos coord = RFTools.instance.clientInfo.getSelectedTE();
        TileEntity tileEntity = null;
        if (coord != null) {
            tileEntity = getWorld().getTileEntity(coord);
        }

        if (!(tileEntity instanceof MatterBeamerTileEntity)) {
            // None selected. Just select this one.
            RFTools.instance.clientInfo.setSelectedTE(getPos());
            SpawnerTileEntity destinationTE = getDestinationTE();
            if (destinationTE == null) {
                RFTools.instance.clientInfo.setDestinationTE(null);
            } else {
                RFTools.instance.clientInfo.setDestinationTE(destinationTE.getPos());
            }
            Logging.message(player, "Select a spawner as destination");
        } else if (coord.equals(getPos())) {
            // Unselect this one.
            RFTools.instance.clientInfo.setSelectedTE(null);
            RFTools.instance.clientInfo.setDestinationTE(null);
            setDestination(null);
            Logging.message(player, "Destination cleared!");
        }
    }

    public void setDestination(BlockPos destination) {
        this.destination = destination;
        disableBlockGlow();
        markDirty();

        if (getWorld().isRemote) {
            // We're on the client. Send change to server.
            valueToServer(RFToolsMessages.INSTANCE, VALUE_DESTINATION, destination);
        } else {
            markDirtyClient();
        }
    }

    public BlockPos getDestination() {
        return destination;
    }

    /**
     * Get the current destination. This function checks first if that destination is
     * still valid and if not it is reset to null (i.e. the destination was removed).
     * @return the destination TE or null if there is no valid one
     */
    private SpawnerTileEntity getDestinationTE() {
        if (destination == null) {
            return null;
        }
        TileEntity te = getWorld().getTileEntity(destination);
        if (te instanceof SpawnerTileEntity) {
            return (SpawnerTileEntity) te;
        } else {
            destination = null;
            markDirtyClient();
            return null;
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        destination = BlockPosTools.readFromNBT(tagCompound, "dest");
        glowing = tagCompound.getBoolean("glowing");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        BlockPosTools.writeToNBT(tagCompound, "dest", destination);
        tagCompound.setBoolean("glowing", glowing);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return MatterBeamerContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return MatterBeamerContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return MatterBeamerContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }
}
